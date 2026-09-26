package com.lenovoved.android.wavelet

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.util.Log

/**
 * BroadcastReceiver for system audio effect sessions.
 * Music players broadcast OPEN_AUDIO_EFFECT_CONTROL_SESSION when they start playback
 * and CLOSE_AUDIO_EFFECT_CONTROL_SESSION when they stop playback.
 */
class AudioSessionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR_BAD_VALUE)
        val packageName = intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME) ?: "unknown"

        if (sessionId == AudioEffect.ERROR_BAD_VALUE || sessionId == 0) return

        when (action) {
            AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> {
                Log.d("WaveletAudio", "Attaching to audio session $sessionId for $packageName")
                WaveletAudioEngine.attachSession(context.applicationContext, sessionId, packageName)
            }
            AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> {
                Log.d("WaveletAudio", "Detaching audio session $sessionId for $packageName")
                WaveletAudioEngine.detachSession(sessionId)
            }
        }
    }
}
