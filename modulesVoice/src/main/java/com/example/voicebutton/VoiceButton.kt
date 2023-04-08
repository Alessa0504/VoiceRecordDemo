package com.example.voicebutton

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.example.voicebutton.interfaces.MediaRecorderStateListener
import com.example.voicebutton.untils.RecordDialogManager
import com.example.voicebutton.untils.RecordManager
import java.io.File

/**
 * @Description:
 * @Author: zouji
 * @CreateDate: 2023/4/4 16:58
 */
//@JvmOverloads constructor: 在 Java 代码中使用默认参数时，Kotlin 编译器将会生成与每个可选参数组合的构造函数
class VoiceButton @JvmOverloads constructor(context: Context, attr: AttributeSet? = null) :
    AppCompatButton(context, attr), MediaRecorderStateListener {

    companion object {
        private const val TAG = "VoiceButton"

        private const val DISTANCE_Y_CANCEL = 100f  //手指滑动指定距离后判定取消录音

        private const val MAX_VOICE_LEVEL = 6  //最大声音音量等级

        private const val ACTION_NONE = 0  //MotionEvent Action默认值，标记没有Action event被触发
    }

    sealed class StateClass {
        data class StateNormal(val action: Int = ACTION_NONE) : StateClass()
        data class StatePreparing(val action: Int = ACTION_NONE) : StateClass()
        data class StateRecording(val action: Int = ACTION_NONE) : StateClass()
        data class StateWantCancel(val action: Int = ACTION_NONE) : StateClass()
    }

    private var recordDialogManager: RecordDialogManager
    private var recordManager: RecordManager
    private var audioManager: AudioManager
    private lateinit var mFocusRequest: AudioFocusRequest
    private lateinit var mAudioAttributes: AudioAttributes
    private var audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener

    private var isRecording = false  //录音中标识
    private var recordTime: Long = 0  //录音时间
    private val samplingInterval: Long = 200  //采样间隔时间
    private val minRecordTime: Long = 600  //最短录音时长
    private val mHandler = Handler()
    private val recordLiveData = MutableLiveData<StateClass>()


    init {
        initObserver()
        recordDialogManager = RecordDialogManager(context)
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioAttributes = AudioAttributes.Builder() //AudioAttributes
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            mFocusRequest =
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)  //AudioFocusRequest
                    .setAudioAttributes(mAudioAttributes)
                    .setOnAudioFocusChangeListener(audioFocusChangeListener)
                    .build()
        }
        val saveFile = initSaveFile()
        recordManager = RecordManager(saveFile.path)
        recordManager.setMediaRecorderStateListener(this@VoiceButton)
        setLongClickListener()
        setText(R.string.str_recorder_normal)
    }

    /**
     * 初始化监听
     */
    private fun initObserver() {
        recordLiveData.observe(context as LifecycleOwner) { state ->
            when (state) {
                is StateClass.StateNormal -> {
                    if (state.action == MotionEvent.ACTION_UP) {
                        //如果longclick(录音)操作没触发
                        Log.e(TAG, "未触发录音或者录音达到最大时长已经结束")
                        releaseAudioFocus()
                        setText(R.string.str_recorder_normal)
                        resetRecordState()
                    } else {
                        setText(R.string.str_recorder_normal)
                    }
                }
                is StateClass.StatePreparing -> {
                    if (state.action == MotionEvent.ACTION_UP) {
                        Log.e(TAG, "未初始化完成或者录音太短")
                        releaseAudioFocus()
                        recordDialogManager.showRecordTooShortDialog()
                        recordManager.cancel()
                        mHandler.postDelayed(dismissDialogRunnable, 1000)
                    } else {
                        recordDialogManager.showPrepareDialog()
                    }
                }
                is StateClass.StateRecording -> {
                    if (state.action == MotionEvent.ACTION_UP) {
                        Log.e(TAG, "正常录制结束")
                        releaseAudioFocus()
                        //正常录制结束
                        recordManager.release()
                        recordDialogManager.dismissDialog()
                        recorderListener?.onFinish(
                            recordTime,
                            recordManager.recordAbsoluteFileDir
                        )
                    } else {
                        setText(R.string.str_recorder_recording)
                        if (isRecording) {
                            recordDialogManager.showRecordingDialog()
                        }
                    }
                }
                is StateClass.StateWantCancel -> {
                    if (state.action == MotionEvent.ACTION_UP) {
                        Log.e(TAG, "滑动取消了录制")
                        releaseAudioFocus()
                        //想要取消录制
                        recordManager.cancel()
                        recordDialogManager.dismissDialog()
                    } else {
                        setText(R.string.str_recorder_want_cancel)
                        recordDialogManager.showCancelRecording()

                    }
                }
            }
        }
    }

    /**
     * 初始化音频存储文件目录
     */
    private fun initSaveFile(): File {
        val saveFile =
            if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {  //判断应用程序可以从外部存储(sdcard)读取和写入数据
                File(
                    context.getExternalFilesDir(null),
                    "recorder_audios"
                ) // getExternalFilesDir: sdcard/Android/data/你的应用的包名/files/目录
            } else {
                File(
                    context.filesDir,
                    "recorder_audios"
                )  // data/data/<package_name>/files/，只能当前应用程序访问
            }
        if (!saveFile.exists()) {
            saveFile.mkdirs()
        }
        return saveFile
    }

    /**
     * 设置长按事件监听
     */
    private fun setLongClickListener() {
        setOnLongClickListener {
            recordLiveData.value = StateClass.StatePreparing()
            recordManager.prepareAudio()
            false
        }
    }

    /**
     * 设置录制最长时间
     *
     * @param time
     */
    fun setMaxRecordLength(time: Int) {
        recordManager.maxRecordLength = time
    }

    /**
     * 更新音量显示Runnable
     */
    private val updateDialogVoiceRunnable = Runnable {
        recordDialogManager.updateVoiceLevel(recordManager.getVoiceLevel(MAX_VOICE_LEVEL))
    }

    /**
     * 更新剩余录音时间显示Runnable
     */
    private val updateRemainingTimeRunnable = Runnable {
        recordDialogManager.updateRemainingTime((recordManager.maxRecordLength - recordTime).toInt() / 1000)
    }

    /**
     * 关闭dialog Runnable
     */
    private val dismissDialogRunnable = Runnable {
        recordDialogManager.dismissDialog()
    }

    /**
     * 每隔一段时间就更新话筒状态Runnable
     */
    private val updateMicroStatusRunnable = Runnable {
        while (isRecording) {
            try {
                Thread.sleep(samplingInterval)
                recordTime += samplingInterval  //录制时间累加
                // 发送简单消息用 handler.post(runnable)， 发送复杂可序列化数据用 handler.sendMessage(message)
                handler.post(updateDialogVoiceRunnable)  //发送消息给在主线程(默认)中的handler执行
                if ((recordManager.maxRecordLength - recordTime < 10 * 1000) && (recordLiveData.value is StateClass.StateWantCancel)) {  //剩余录音时间小于10秒时显示剩余录制时间提示
                    handler.post(updateRemainingTimeRunnable)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onError(what: Int, extra: Int) {
        Log.e(TAG, "录音出错了=====>what:$what,extra:$extra")
    }

    override fun wellPrepared() {
        requestAudioFocus()
        recorderListener?.onStart()
        isRecording = true
        recordLiveData.value = StateClass.StateRecording()
        //开启线程实时更新音量
        Thread(updateMicroStatusRunnable).start()
    }

    override fun onStop(filePath: String?) {
        Log.e(TAG, "录音停止了=====>filePath:$filePath")
    }

    override fun onReachMaxRecordTime(filePath: String?) {
        recordManager.release()  //到达最大录制时间录制结束
        recordDialogManager.dismissDialog()

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val action = event.action  //获取手势类型及触摸位置坐标
        val y = event.y
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                recordLiveData.value = StateClass.StateNormal()  //MotionEvent.ACTION_DOWN是默认值所以不传
            }
            MotionEvent.ACTION_MOVE -> {
                if (isRecording) {
                    //根据x,y的坐标判断是否取消录音
                    if (wantToCancel(y)) {
                        recordLiveData.value = StateClass.StateWantCancel(MotionEvent.ACTION_MOVE)
                    } else {
                        recordLiveData.value = StateClass.StateRecording(MotionEvent.ACTION_MOVE)
                    }
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (recordLiveData.value is StateClass.StateNormal) {
                    recordLiveData.value = StateClass.StateNormal(MotionEvent.ACTION_UP)
                }
                if (recordLiveData.value is StateClass.StatePreparing || recordTime < minRecordTime) {
                    recordLiveData.value = StateClass.StatePreparing(MotionEvent.ACTION_UP)
                } else if (recordLiveData.value is StateClass.StateRecording) {
                    recordLiveData.value = StateClass.StateRecording(MotionEvent.ACTION_UP)
                } else if (recordLiveData.value is StateClass.StateWantCancel) {
                    recordLiveData.value = StateClass.StateWantCancel(MotionEvent.ACTION_UP)
                }
                resetRecordState()
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 恢复录音状态、标志位
     */
    private fun resetRecordState() {
        recordLiveData.value = StateClass.StateNormal()
        isRecording = false
        recordTime = 0
    }

    /**
     * 判断是否想取消录制
     * @param y
     * @return
     */
    private fun wantToCancel(y: Float): Boolean {
        return y < -DISTANCE_Y_CANCEL  //如果上下滑出自定义的距离
    }

    /**
     * 获得音频焦点
     */
    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(mFocusRequest)
        } else {
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
        }
    }

    /**
     * 释放所获得的音频焦点
     */
    private fun releaseAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.abandonAudioFocusRequest(mFocusRequest);
        } else {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }
    }

    /**
     * 录音完成后的回调接口
     */
    interface RecorderListener {
        fun onStart()  //开始录音

        fun onFinish(time: Long, filePath: String?)  //录音时长和文件保存路径
    }

    private var recorderListener: RecorderListener? = null

    /**
     * 设置录音监听
     * @param recorderListener
     */
    fun setRecorderListener(recorderListener: RecorderListener?) {
        this.recorderListener = recorderListener
    }
}