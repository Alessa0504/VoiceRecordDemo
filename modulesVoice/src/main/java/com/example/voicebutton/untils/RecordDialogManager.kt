package com.example.voicebutton.untils

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.example.voicebutton.R

/**
 * @Description: 录音dialog管理类
 * @Author: zouji
 * @CreateDate: 2023/4/4 14:02
 */
class RecordDialogManager(private val mContext: Context) {
    private var mDialog: AlertDialog? = null
    private var mIcon: ImageView? = null  //录音麦克风标志
    private var mVoice: ImageView? = null  //录音音量标志
    private var mLabel: TextView? = null  //录音提示
    private var mProgressBar: ProgressBar? = null  //准备录音标志

    /**
     * 显示准备录音dialog
     */
    fun showPrepareDialog() {
        if (mDialog != null) return
        val builder = AlertDialog.Builder(mContext, R.style.Theme_RecordDialog)
        val view = LayoutInflater.from(mContext).inflate(R.layout.dialog_recorder, null)
        mIcon = view.findViewById(R.id.iv_recorder_icon)
        mVoice = view.findViewById(R.id.iv_recorder_voice)
        mLabel = view.findViewById(R.id.tv_recorder_label)
        mProgressBar = view.findViewById(R.id.progressBar)
        mIcon!!.visibility = View.GONE
        mVoice!!.visibility = View.GONE
        mLabel!!.visibility = View.GONE
        mProgressBar!!.visibility = View.VISIBLE
        builder.setView(view)
        mDialog = builder.create()
        mDialog!!.setCancelable(false)
        mDialog!!.setCanceledOnTouchOutside(false)
        mDialog!!.show()
    }

    /**
     * 显示录音dialog
     */
    fun showRecordingDialog() {
        if (mDialog != null && mDialog!!.isShowing) {
            mIcon!!.setImageResource(R.drawable.ic_recorder)
            mIcon!!.visibility = View.VISIBLE
            mVoice!!.visibility = View.VISIBLE
            mLabel!!.visibility = View.VISIBLE
            mProgressBar!!.visibility = View.GONE
            mLabel!!.setText(R.string.str_recorder_swip_cancel)
            mLabel!!.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    /**
     * 显示想要取消录音的dialog
     */
    fun showCancelRecording() {
        if (mDialog != null && mDialog!!.isShowing) {
            mIcon!!.setImageResource(R.drawable.record_cancel)
            mIcon!!.visibility = View.VISIBLE
            mVoice!!.visibility = View.GONE
            mLabel!!.setText(R.string.str_recorder_want_cancel)
            mLabel!!.setBackgroundResource(R.color.colorDarkOrange)
            mLabel!!.visibility = View.VISIBLE
            mProgressBar!!.visibility = View.INVISIBLE
        }
    }

    /**
     * 显示录音时间太短的dialog
     */
    fun showRecordTooShortDialog() {
        if (mDialog != null && mDialog!!.isShowing) {
            mIcon!!.setImageResource(R.drawable.voice_to_short)
            mLabel!!.setText(R.string.str_recorder_too_short)
            mIcon!!.visibility = View.VISIBLE
            mVoice!!.visibility = View.GONE
            mLabel!!.visibility = View.VISIBLE
        }
    }

    /**
     * 关闭dialog
     */
    fun dismissDialog() {
        if (mDialog != null && mDialog!!.isShowing) {
            mDialog!!.dismiss()
            mDialog = null
        }
    }

    /**
     * 通过level更新voice音量图片
     * @param level
     */
    fun updateVoiceLevel(level: Int) {
        if (mDialog != null && mDialog!!.isShowing) {
            //获取指定名称的drawable资源
            val resId =
                mContext.resources.getIdentifier("volume_$level", "drawable", mContext.packageName)
            mVoice!!.setImageResource(resId)
        }
    }

    /**
     * 开启倒计时通知
     * @param time
     */
    fun updateRemainingTime(time: Int) {
        if (mDialog != null && mDialog!!.isShowing) {
            mLabel!!.visibility = View.VISIBLE
            mLabel!!.text =
                String.format(mContext.resources.getString(R.string.time_remaining), time + 1) //%d
        }
    }
}