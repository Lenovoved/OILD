package com.lenovoved.android

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.lenovoved.android.island.OriginIslandBuilder
import com.lenovoved.android.service.NotificationCastListener
import com.lenovoved.android.sports.SportsFeed
import com.lenovoved.android.wavelet.WaveletAudioEngine

class OriginIsleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        SportsFeed.init(this)
        OriginIslandBuilder.grantScenes(this)
        // Every process start, not just the UI's: a service restart is the likeliest way back in
        // after the kill that could have left the component half-toggled.
        NotificationCastListener.ensureEnabled(this)
        // Initialize global Wavelet audio engine so it never unloads
        WaveletAudioEngine.initAudioEffects(this)
    }
}
