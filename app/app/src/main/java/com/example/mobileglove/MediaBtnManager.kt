package com.example.mobileglove

import android.content.Context
import android.media.AudioManager
import android.os.SystemClock
import android.view.KeyEvent

class MediaBtnManager(context: Context) {
    private val eventTime = SystemClock.uptimeMillis()
    private val mAudioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    fun nextBtn() {
        val downEvent = KeyEvent(
            eventTime,
            eventTime,
            KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            0)
        mAudioManager.dispatchMediaKeyEvent(downEvent)

        val upEvent = KeyEvent(
            eventTime,
            eventTime,
            KeyEvent.ACTION_UP,
            KeyEvent.KEYCODE_MEDIA_NEXT,
            0
        )
        mAudioManager.dispatchMediaKeyEvent(upEvent)
    }

    fun previousBtn() {
        val downEvent = KeyEvent(
            eventTime,
            eventTime,
            KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            0
        )
        mAudioManager.dispatchMediaKeyEvent(downEvent)

        val upEvent = KeyEvent(
            eventTime,
            eventTime,
            KeyEvent.ACTION_UP,
            KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            0
        )
        mAudioManager.dispatchMediaKeyEvent(upEvent)
    }

    fun playPauseBtn() {
        val downEvent = KeyEvent(
            eventTime,
            eventTime,
            KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            0
        )
        mAudioManager.dispatchMediaKeyEvent(downEvent)

        val upEvent = KeyEvent(
            eventTime,
            eventTime,
            KeyEvent.ACTION_UP,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE,
            0
        )
        mAudioManager.dispatchMediaKeyEvent(upEvent)
    }

    fun volumeUpBtn() {
        val downEvent = KeyEvent(
            eventTime,
            eventTime,
            KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_VOLUME_UP,
            0)
        mAudioManager.dispatchMediaKeyEvent(downEvent)

        val upEvent = KeyEvent(
            eventTime,
            eventTime,
            KeyEvent.ACTION_UP,
            KeyEvent.KEYCODE_VOLUME_UP,
            0
        )
        mAudioManager.dispatchMediaKeyEvent(upEvent)
    }

    fun volumeDownBtn() {
        val downEvent = KeyEvent(
            eventTime,
            eventTime,
            KeyEvent.ACTION_DOWN,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            0)
        mAudioManager.dispatchMediaKeyEvent(downEvent)

        val upEvent = KeyEvent(
            eventTime,
            eventTime,
            KeyEvent.ACTION_UP,
            KeyEvent.KEYCODE_VOLUME_DOWN,
            0
        )
        mAudioManager.dispatchMediaKeyEvent(upEvent)
    }
}