package com.example.voicerecorddemo

import android.Manifest
import android.graphics.drawable.AnimationDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voicebutton.VoiceButton
import com.example.voicerecorddemo.adapter.RecorderAdapter
import com.example.voicerecorddemo.bean.RecorderInfo
import com.example.voicerecorddemo.untils.MediaPlayerManager
import permissions.dispatcher.PermissionRequest
import permissions.dispatcher.ktx.withPermissionsCheck

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = "MainActivity"
    }
    private lateinit var vbRecord: VoiceButton
    private lateinit var rvVoice: RecyclerView
    private lateinit var recorderAdapter: RecorderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkRecordAudioPermission()
        initView()
    }

    private fun initView() {
        vbRecord = findViewById(R.id.vb_record)
        rvVoice = findViewById(R.id.rv_voice)
        recorderAdapter = RecorderAdapter(this@MainActivity, ArrayList())
        vbRecord.setMaxRecordLength(20 * 1000)
        rvVoice.layoutManager = LinearLayoutManager(this@MainActivity)
        setClickListener()
        rvVoice.adapter = recorderAdapter
    }

    private fun setClickListener() {
        vbRecord.setRecorderListener(object : VoiceButton.RecorderListener {
            override fun onStart() {
                Log.d(TAG, "开始（触发）本次录音，可能会因为录音时间太短取消本次录音")
            }

            override fun onFinish(
                time: Long,
                filePath: String?
            ) {
                Log.d(TAG, "完成了本次录音")
                recorderAdapter.addData(RecorderInfo(time, filePath!!))
            }
        })
        recorderAdapter.setOnItemClickListener(object : RecorderAdapter.ItemClickListener {
            override fun onItemClick(view: View, position: Int) {
                //播放语音动画
                val voiceAnim = view.findViewById<View>(R.id.v_voice_anim)
                voiceAnim.setBackgroundResource(R.drawable.voice_anim)
                (voiceAnim.background as AnimationDrawable).start()
                //播放录音
                MediaPlayerManager.playSound(recorderAdapter.getItem(position).filePath) {
                    //播放完成后修改图片
                    voiceAnim.setBackgroundResource(R.drawable.voice)
                }
            }
        })
    }

    private fun checkRecordAudioPermission() = withPermissionsCheck(
        Manifest.permission.RECORD_AUDIO,
        onShowRationale = ::onRecordAudioShowRationale,
        onPermissionDenied = ::onRecordAudioDenied,
        onNeverAskAgain = ::onRecordAudioNeverAskAgain
    ) {
    }

    private fun onRecordAudioDenied() {
        Toast.makeText(this, "拒绝了录音权限", Toast.LENGTH_SHORT).show()
    }

    private fun onRecordAudioShowRationale(request: PermissionRequest) {
        request.proceed()
    }

    private fun onRecordAudioNeverAskAgain() {
        Toast.makeText(this, "拒绝了录音权限并且不再提醒", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}