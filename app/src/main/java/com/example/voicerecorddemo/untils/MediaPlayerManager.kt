package com.example.voicerecorddemo.untils

import android.media.AudioAttributes
import android.media.MediaPlayer


/**
 * @Description: 音频播放管理类
 * @Author: zouji
 * @CreateDate: 2023/4/8 13:23
 */
object MediaPlayerManager {
    private var mediaPlayer: MediaPlayer? = null  //播放音频API类：MediaPlayer
    private var isPause = false  //是否暂停
    val isPlaying = mediaPlayer?.let {//是否正在播放
        it.isPlaying
    }

    /**
     * 播放声音
     *
     */
    fun playSound(filePath: String, completeListener: MediaPlayer.OnCompletionListener) {
        if (mediaPlayer == null) {
            //初始化mediaPlayer
            mediaPlayer = MediaPlayer()
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
            mediaPlayer?.setAudioAttributes(audioAttributes)
            mediaPlayer?.setOnErrorListener { _, _, _ ->
                mediaPlayer?.reset()
                false
            }
        } else {
            mediaPlayer?.reset()
        }
        mediaPlayer?.apply {
            //播放
            setDataSource(filePath)
            prepare()
            start()
            setOnCompletionListener(completeListener)  //播放完成后的回调
        }
    }

    /**
     * 暂停播放
     */
    fun pause() {
        mediaPlayer?.takeIf {
            it.isPlaying
        }?.let {
            it.pause()
            isPause = true
        }
    }

    /**
     * 恢复播放
     */
    fun resume() {
        mediaPlayer?.takeIf {
            isPause
        }?.let {
            it.start()
            isPause = false
        }
    }

    /**
     * 停止播放
     */
    fun stop() {
        mediaPlayer?.takeIf {
            it.isPlaying
        }?.let {
            it.stop()
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
    }
}