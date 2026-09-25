package com.originisle.android.equalizer

import android.content.Context
import android.content.SharedPreferences
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.util.Log

/**
 * Open-source Wavelet-inspired Audio Engine for Android.
 * Reworked to target real, hardware-supported audio frequencies
 * (standard Android 5-band AudioFX architecture: 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz).
 */
object WaveletAudioEngine {
    private const val TAG = "WaveletAudioEngine"
    private const val PREFS_NAME = "wavelet_eq_prefs"

    // Real working frequencies supported by standard Android Equalizer HAL (in Hz)
    val DEFAULT_WORKING_FREQUENCIES = listOf(60, 230, 910, 3600, 14000)
    val DEFAULT_WORKING_LABELS = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")

    @Volatile
    private var detectedFrequencies: List<Int> = DEFAULT_WORKING_FREQUENCIES
    @Volatile
    private var detectedLabels: List<String> = DEFAULT_WORKING_LABELS

    data class EqPreset(
        val name: String,
        val gains: List<Float>, // 5-band gains in dB (-12.0 to +12.0)
    )

    data class AutoEqProfile(
        val model: String,
        val manufacturer: String,
        val type: String, // In-Ear, Over-Ear, Earbuds
        val gains: List<Float>, // 5-band compensation gains in dB
    )

    // Accurate 5-band presets mapped to real working frequencies (60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz)
    val PRESETS = listOf(
        EqPreset("Flat", listOf(0f, 0f, 0f, 0f, 0f)),
        EqPreset("Bass Boost", listOf(6.0f, 4.5f, 1.0f, 0f, 1.0f)),
        EqPreset("Rock", listOf(4.5f, 2.0f, -0.5f, 2.5f, 4.0f)),
        EqPreset("Pop", listOf(-1.0f, 2.0f, 3.5f, 1.5f, 0.5f)),
        EqPreset("Electronic", listOf(5.0f, 3.0f, 0f, 2.0f, 3.5f)),
        EqPreset("Jazz", listOf(3.5f, 1.5f, 1.5f, 2.0f, 2.5f)),
        EqPreset("Vocal Booster", listOf(-2.0f, 0.5f, 3.5f, 2.5f, 0.5f)),
        EqPreset("Classical", listOf(4.0f, 2.5f, 1.0f, 2.0f, 2.5f)),
        EqPreset("Acoustic", listOf(3.5f, 2.0f, 1.0f, 2.5f, 3.0f)),
        EqPreset("Gaming / Movie", listOf(5.5f, 2.5f, -0.5f, 2.0f, 4.0f)),
    )

    // AutoEq profiles mapped to 5 real working bands
    val AUTO_EQ_PROFILES = listOf(
        // Apple
        AutoEqProfile("AirPods Pro 2", "Apple", "In-Ear", listOf(-0.5f, 0.8f, -0.8f, 0.5f, 1.5f)),
        AutoEqProfile("AirPods Pro", "Apple", "In-Ear", listOf(-0.2f, 0.5f, -0.6f, 0.8f, 1.2f)),
        AutoEqProfile("AirPods Max", "Apple", "Over-Ear", listOf(-1.2f, -0.5f, 0.8f, 1.5f, 2.3f)),
        AutoEqProfile("AirPods 3", "Apple", "Earbuds", listOf(1.5f, 1.0f, -0.6f, 0.2f, 1.0f)),
        AutoEqProfile("EarPods (3.5mm)", "Apple", "Earbuds", listOf(3.0f, 2.0f, -0.2f, 0.8f, 1.5f)),
        // Sony
        AutoEqProfile("WH-1000XM5", "Sony", "Over-Ear", listOf(-3.5f, -2.8f, 0.2f, 1.2f, -0.5f)),
        AutoEqProfile("WH-1000XM4", "Sony", "Over-Ear", listOf(-4.0f, -3.2f, 0.1f, 1.0f, -1.0f)),
        AutoEqProfile("WH-1000XM3", "Sony", "Over-Ear", listOf(-4.5f, -3.5f, 0.0f, 0.8f, -1.2f)),
        AutoEqProfile("WF-1000XM5", "Sony", "In-Ear", listOf(-2.0f, -1.5f, 0.5f, 1.0f, 1.2f)),
        AutoEqProfile("WF-1000XM4", "Sony", "In-Ear", listOf(-2.5f, -1.8f, 0.3f, 0.8f, 1.0f)),
        AutoEqProfile("LinkBuds S", "Sony", "In-Ear", listOf(-1.0f, -0.5f, 0.6f, 0.5f, 1.0f)),
        AutoEqProfile("MDR-7506", "Sony", "Over-Ear", listOf(1.5f, 0.8f, -0.8f, -0.5f, -2.0f)),
        // Samsung / AKG
        AutoEqProfile("Galaxy Buds 2 Pro", "Samsung", "In-Ear", listOf(0.5f, 0.0f, -0.5f, 0.5f, 1.0f)),
        AutoEqProfile("Galaxy Buds 2", "Samsung", "In-Ear", listOf(0.0f, -0.5f, 0.1f, 0.8f, 0.5f)),
        AutoEqProfile("Galaxy Buds FE", "Samsung", "In-Ear", listOf(-1.0f, -0.5f, 0.6f, 1.2f, 1.5f)),
        AutoEqProfile("Galaxy Buds Live", "Samsung", "Earbuds", listOf(3.5f, 2.5f, -0.4f, -0.2f, 1.2f)),
        AutoEqProfile("AKG K371", "AKG", "Over-Ear", listOf(-0.5f, 0.2f, -0.1f, 0.2f, 0.5f)),
        AutoEqProfile("AKG N700NC", "AKG", "Over-Ear", listOf(-1.5f, -1.0f, 0.4f, 0.8f, 1.0f)),
        // Sennheiser
        AutoEqProfile("Momentum 4", "Sennheiser", "Over-Ear", listOf(-2.5f, -1.8f, 0.2f, 1.0f, 0.0f)),
        AutoEqProfile("Momentum TW 3", "Sennheiser", "In-Ear", listOf(-2.0f, -1.2f, 0.4f, 0.8f, 0.5f)),
        AutoEqProfile("HD 600", "Sennheiser", "Over-Ear", listOf(4.5f, 3.2f, 0.5f, -0.2f, -0.5f)),
        AutoEqProfile("HD 650", "Sennheiser", "Over-Ear", listOf(4.0f, 2.8f, 0.4f, 0.0f, -0.2f)),
        AutoEqProfile("HD 560S", "Sennheiser", "Over-Ear", listOf(2.5f, 1.5f, 0.0f, -0.5f, 0.2f)),
        AutoEqProfile("IE 200", "Sennheiser", "In-Ear", listOf(1.0f, 0.5f, -0.3f, 0.2f, 1.2f)),
        AutoEqProfile("HD 280 Pro", "Sennheiser", "Over-Ear", listOf(1.0f, 0.5f, -1.0f, -0.5f, 0.8f)),
        // Beyerdynamic
        AutoEqProfile("DT 770 Pro (80Ω)", "Beyerdynamic", "Over-Ear", listOf(-1.5f, -0.8f, 0.8f, 0.5f, -3.8f)),
        AutoEqProfile("DT 990 Pro (250Ω)", "Beyerdynamic", "Over-Ear", listOf(3.0f, 2.0f, 0.0f, -0.8f, -4.5f)),
        AutoEqProfile("DT 1990 Pro", "Beyerdynamic", "Over-Ear", listOf(1.5f, 0.8f, -0.2f, -0.2f, -3.0f)),
        AutoEqProfile("TYGR 300 R", "Beyerdynamic", "Over-Ear", listOf(0.5f, 0.0f, 0.4f, 0.2f, -1.5f)),
        // Audio-Technica
        AutoEqProfile("ATH-M50x", "Audio-Technica", "Over-Ear", listOf(-2.0f, -1.5f, 0.5f, 1.0f, 1.5f)),
        AutoEqProfile("ATH-M40x", "Audio-Technica", "Over-Ear", listOf(-0.5f, 0.0f, 0.1f, 0.5f, 0.8f)),
        AutoEqProfile("ATH-R70x", "Audio-Technica", "Over-Ear", listOf(2.0f, 1.2f, 0.1f, 0.0f, 0.5f)),
        // Bose
        AutoEqProfile("QuietComfort 45", "Bose", "Over-Ear", listOf(-1.0f, -0.5f, 0.8f, 1.2f, 1.0f)),
        AutoEqProfile("QC Ultra", "Bose", "Over-Ear", listOf(-1.5f, -0.8f, 0.6f, 1.0f, 1.2f)),
        AutoEqProfile("NC 700", "Bose", "Over-Ear", listOf(-0.5f, 0.0f, 0.6f, 0.5f, 0.8f)),
        // JBL
        AutoEqProfile("Tune 510BT", "JBL", "On-Ear", listOf(-3.0f, -2.0f, 0.2f, 1.0f, -1.5f)),
        AutoEqProfile("Tune 760NC", "JBL", "Over-Ear", listOf(-2.5f, -1.8f, 0.4f, 0.8f, -1.0f)),
        AutoEqProfile("Live 660NC", "JBL", "On-Ear", listOf(-2.0f, -1.2f, 0.3f, 0.5f, -0.5f)),
        AutoEqProfile("Wave Flex", "JBL", "Earbuds", listOf(2.0f, 1.5f, -0.2f, 0.2f, 1.0f)),
        // Moondrop
        AutoEqProfile("Chu II", "Moondrop", "In-Ear", listOf(0.0f, 0.5f, -0.2f, 0.0f, 0.5f)),
        AutoEqProfile("Aria", "Moondrop", "In-Ear", listOf(-0.5f, 0.2f, -0.1f, 0.2f, 0.8f)),
        AutoEqProfile("Blessing 3", "Moondrop", "In-Ear", listOf(0.2f, 0.5f, -0.4f, 0.0f, 0.5f)),
        AutoEqProfile("Space Travel", "Moondrop", "In-Ear", listOf(-0.8f, -0.2f, 0.4f, 0.8f, 1.0f)),
        AutoEqProfile("Kato", "Moondrop", "In-Ear", listOf(-0.2f, 0.2f, -0.1f, 0.2f, 0.5f)),
        // Soundcore / Anker
        AutoEqProfile("Space Q45", "Soundcore", "Over-Ear", listOf(-3.0f, -2.2f, 0.4f, 1.0f, 0.8f)),
        AutoEqProfile("Liberty 4 NC", "Soundcore", "In-Ear", listOf(-2.5f, -1.8f, 0.6f, 1.2f, 1.0f)),
        AutoEqProfile("Life Q30", "Soundcore", "Over-Ear", listOf(-4.5f, -3.5f, 0.2f, 0.8f, -0.5f)),
        // Xiaomi / Redmi
        AutoEqProfile("Redmi Buds 5 Pro", "Xiaomi", "In-Ear", listOf(-1.2f, -0.5f, 0.4f, 0.8f, 1.0f)),
        AutoEqProfile("Buds 4 Pro", "Xiaomi", "In-Ear", listOf(-1.0f, -0.2f, 0.3f, 0.5f, 0.8f)),
        AutoEqProfile("Poco Buds Pro", "Xiaomi", "In-Ear", listOf(-2.0f, -1.2f, 0.5f, 1.0f, 0.5f)),
        // Huawei
        AutoEqProfile("FreeBuds Pro 3", "Huawei", "In-Ear", listOf(-0.8f, 0.2f, 0.1f, 0.5f, 1.2f)),
        AutoEqProfile("FreeBuds 5i", "Huawei", "In-Ear", listOf(-1.5f, -0.8f, 0.5f, 1.0f, 0.8f)),
        // Beats
        AutoEqProfile("Beats Studio Pro", "Beats", "Over-Ear", listOf(-1.8f, -1.0f, 0.4f, 0.8f, 1.2f)),
        AutoEqProfile("Beats Fit Pro", "Beats", "In-Ear", listOf(-2.0f, -1.2f, 0.5f, 1.0f, 1.0f)),
        AutoEqProfile("Solo3 Wireless", "Beats", "On-Ear", listOf(-4.0f, -3.0f, 0.3f, 0.5f, 0.0f)),
        // Marshall
        AutoEqProfile("Major IV", "Marshall", "On-Ear", listOf(-3.5f, -2.5f, 0.4f, 1.2f, 0.0f)),
        AutoEqProfile("Motif II A.N.C.", "Marshall", "In-Ear", listOf(-2.0f, -1.2f, 0.5f, 1.0f, 0.8f)),
        // Shure
        AutoEqProfile("SE215", "Shure", "In-Ear", listOf(-3.0f, -2.0f, 0.6f, 1.5f, -1.0f)),
        AutoEqProfile("AONIC 50", "Shure", "Over-Ear", listOf(-0.5f, 0.0f, 0.4f, 0.2f, 0.8f)),
    )

    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null

    private var isInitialized = false

    fun initAudioEffects(context: Context) {
        if (isInitialized) return
        try {
            // Audio session 0 applies effect globally to media output
            equalizer = Equalizer(0, 0).apply {
                val numBands = numberOfBands.toInt()
                if (numBands > 0) {
                    val freqs = mutableListOf<Int>()
                    val labels = mutableListOf<String>()
                    for (i in 0 until numBands) {
                        val milliHz = getCenterFreq(i.toShort())
                        val hz = milliHz / 1000
                        freqs.add(hz)
                        labels.add(if (hz >= 1000) String.format("%.1f kHz", hz / 1000.0) else "$hz Hz")
                    }
                    detectedFrequencies = freqs
                    detectedLabels = labels
                    Log.d(TAG, "Hardware Equalizer probed: $numBands bands at $freqs")
                }
                enabled = getMasterEnabled(context)
            }
            bassBoost = BassBoost(0, 0).apply {
                enabled = getMasterEnabled(context) && getBassBoostEnabled(context)
                if (strengthSupported) {
                    setStrength((getBassBoostStrength(context) * 10).toInt().toShort())
                }
            }
            virtualizer = Virtualizer(0, 0).apply {
                enabled = getMasterEnabled(context) && getVirtualizerEnabled(context)
                if (strengthSupported) {
                    setStrength((getVirtualizerStrength(context) * 10).toInt().toShort())
                }
            }
            presetReverb = PresetReverb(0, 0).apply {
                enabled = getMasterEnabled(context) && getReverbEnabled(context)
                preset = getReverbPreset(context).toShort()
            }
            applyBandGains(context, getBandGains(context))
            isInitialized = true
            Log.d(TAG, "Wavelet audio effects initialized successfully.")
        } catch (e: Exception) {
            Log.w(TAG, "Hardware audio effects initialization note: ${e.message}")
        }
    }

    fun getBandCount(context: Context): Int {
        val num = equalizer?.numberOfBands?.toInt()
        return if (num != null && num > 0) num else detectedFrequencies.size
    }

    fun getBandFrequencies(context: Context): List<Int> = detectedFrequencies

    fun getBandLabels(context: Context): List<String> = detectedLabels

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Default to false: all toggles are OFF by default
    fun getMasterEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean("eq_master_enabled", false)

    fun setMasterEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("eq_master_enabled", enabled).apply()
        runCatching {
            equalizer?.enabled = enabled
            bassBoost?.enabled = enabled && getBassBoostEnabled(context)
            virtualizer?.enabled = enabled && getVirtualizerEnabled(context)
            presetReverb?.enabled = enabled && getReverbEnabled(context)
        }
    }

    fun getBandGains(context: Context): List<Float> {
        val prefs = getPrefs(context)
        val count = getBandCount(context)
        return (0 until count).map { i ->
            prefs.getFloat("eq_band_$i", 0f)
        }
    }

    fun setBandGain(context: Context, bandIndex: Int, gainDb: Float) {
        val clamped = gainDb.coerceIn(-12f, 12f)
        getPrefs(context).edit().putFloat("eq_band_$bandIndex", clamped).apply()
        applyBandGainToHardware(bandIndex, clamped)
    }

    fun setAllBandGains(context: Context, gains: List<Float>) {
        val editor = getPrefs(context).edit()
        gains.forEachIndexed { i, g ->
            val clamped = g.coerceIn(-12f, 12f)
            editor.putFloat("eq_band_$i", clamped)
            applyBandGainToHardware(i, clamped)
        }
        editor.apply()
    }

    private fun applyBandGainToHardware(bandIndex: Int, gainDb: Float) {
        runCatching {
            equalizer?.let { eq ->
                val numBands = eq.numberOfBands.toInt()
                if (bandIndex < numBands) {
                    val minLevel = eq.bandLevelRange[0]
                    val maxLevel = eq.bandLevelRange[1]
                    // Map -12dB..+12dB to millibels range (usually -1500..+1500 mB)
                    val mb = (gainDb * 100).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt()).toShort()
                    eq.setBandLevel(bandIndex.toShort(), mb)
                }
            }
        }
    }

    private fun applyBandGains(context: Context, gains: List<Float>) {
        gains.forEachIndexed { i, g -> applyBandGainToHardware(i, g) }
    }

    fun getSelectedPreset(context: Context): String =
        getPrefs(context).getString("eq_preset_name", "Flat") ?: "Flat"

    fun setSelectedPreset(context: Context, name: String) {
        getPrefs(context).edit().putString("eq_preset_name", name).apply()
    }

    // AutoEq
    fun getAutoEqEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean("auto_eq_enabled", false)

    fun setAutoEqEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("auto_eq_enabled", enabled).apply()
    }

    fun getSelectedAutoEqProfile(context: Context): String =
        getPrefs(context).getString("auto_eq_selected_model", "AirPods Pro 2") ?: "AirPods Pro 2"

    fun setSelectedAutoEqProfile(context: Context, model: String) {
        getPrefs(context).edit().putString("auto_eq_selected_model", model).apply()
        AUTO_EQ_PROFILES.find { it.model == model }?.let { profile ->
            setAllBandGains(context, profile.gains)
            setSelectedPreset(context, "AutoEq: ${profile.model}")
        }
    }

    // Bass Boost
    fun getBassBoostEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean("bass_boost_enabled", false)

    fun setBassBoostEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("bass_boost_enabled", enabled).apply()
        runCatching {
            bassBoost?.enabled = enabled && getMasterEnabled(context)
        }
    }

    fun getBassBoostStrength(context: Context): Float =
        getPrefs(context).getFloat("bass_boost_strength", 35f)

    fun setBassBoostStrength(context: Context, strength: Float) {
        val clamped = strength.coerceIn(0f, 100f)
        getPrefs(context).edit().putFloat("bass_boost_strength", clamped).apply()
        runCatching {
            if (bassBoost?.strengthSupported == true) {
                bassBoost?.setStrength((clamped * 10).toInt().toShort())
            }
        }
    }

    // Virtualizer / Spatial
    fun getVirtualizerEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean("virtualizer_enabled", false)

    fun setVirtualizerEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("virtualizer_enabled", enabled).apply()
        runCatching {
            virtualizer?.enabled = enabled && getMasterEnabled(context)
        }
    }

    fun getVirtualizerStrength(context: Context): Float =
        getPrefs(context).getFloat("virtualizer_strength", 40f)

    fun setVirtualizerStrength(context: Context, strength: Float) {
        val clamped = strength.coerceIn(0f, 100f)
        getPrefs(context).edit().putFloat("virtualizer_strength", clamped).apply()
        runCatching {
            if (virtualizer?.strengthSupported == true) {
                virtualizer?.setStrength((clamped * 10).toInt().toShort())
            }
        }
    }

    // Reverb
    fun getReverbEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean("reverb_enabled", false)

    fun setReverbEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("reverb_enabled", enabled).apply()
        runCatching {
            presetReverb?.enabled = enabled && getMasterEnabled(context)
        }
    }

    fun getReverbPreset(context: Context): Int =
        getPrefs(context).getInt("reverb_preset", PresetReverb.PRESET_SMALLROOM.toInt())

    fun setReverbPreset(context: Context, preset: Int) {
        getPrefs(context).edit().putInt("reverb_preset", preset).apply()
        runCatching {
            presetReverb?.preset = preset.toShort()
        }
    }

    // Limiter / Preamp gain
    fun getPreampGain(context: Context): Float =
        getPrefs(context).getFloat("preamp_gain", 0f)

    fun setPreampGain(context: Context, gain: Float) {
        val clamped = gain.coerceIn(-12f, 12f)
        getPrefs(context).edit().putFloat("preamp_gain", clamped).apply()
    }

    fun getLimiterEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean("limiter_enabled", false)

    fun setLimiterEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean("limiter_enabled", enabled).apply()
    }

    fun release() {
        runCatching {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            presetReverb?.release()
        }
        equalizer = null
        bassBoost = null
        virtualizer = null
        presetReverb = null
        isInitialized = false
    }
}
