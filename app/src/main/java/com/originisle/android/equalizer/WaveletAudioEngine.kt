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
 * Features 9 standard ISO octave frequency bands matching Wavelet:
 * [62.5 Hz, 125 Hz, 250 Hz, 500 Hz, 1 kHz, 2 kHz, 4 kHz, 8 kHz, 16 kHz] (range -12.0 to +12.0 dB).
 * Smoothly interpolates and applies corrections to the underlying Android hardware Equalizer HAL.
 */
object WaveletAudioEngine {
    private const val TAG = "WaveletAudioEngine"
    private const val PREFS_NAME = "wavelet_eq_prefs"

    // 9 Wavelet ISO Octave Center Frequencies (in Hz) matching user's reference screenshot
    val WAVELET_FREQUENCIES = listOf(62.5f, 125f, 250f, 500f, 1000f, 2000f, 4000f, 8000f, 16000f)
    val WAVELET_LABELS = listOf("62,5", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")
    val WAVELET_FULL_LABELS = listOf("62,5 Гц", "125 Гц", "250 Гц", "500 Гц", "1 кГц", "2 кГц", "4 кГц", "8 кГц", "16 кГц")

    data class EqPreset(
        val name: String,
        val gains: List<Float>, // 9-band gains in dB (-12.0 to +12.0)
    )

    data class AutoEqProfile(
        val model: String,
        val manufacturer: String,
        val type: String, // In-Ear, Over-Ear, Earbuds
        val gains: List<Float>, // 9-band compensation gains in dB
    )

    // Accurate 9-band presets mapped to 62.5, 125, 250, 500, 1k, 2k, 4k, 8k, 16k
    val PRESETS = listOf(
        EqPreset("Flat", listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f)),
        EqPreset("Bass Boost", listOf(6.0f, 5.0f, 3.5f, 1.0f, 0f, 0f, 0.5f, 1.0f, 1.5f)),
        EqPreset("Rock", listOf(4.5f, 3.0f, 1.5f, -0.5f, 0.5f, 2.0f, 3.0f, 4.0f, 4.5f)),
        EqPreset("Pop", listOf(-1.0f, 0.5f, 2.0f, 3.5f, 2.5f, 1.5f, 1.0f, 0.5f, 0.5f)),
        EqPreset("Electronic", listOf(5.5f, 4.5f, 2.5f, 0f, 1.0f, 2.0f, 3.0f, 4.0f, 4.0f)),
        EqPreset("Jazz", listOf(3.5f, 2.5f, 1.0f, 1.5f, 1.5f, 2.0f, 2.5f, 2.5f, 2.0f)),
        EqPreset("Vocal Booster", listOf(-2.5f, -1.0f, 1.0f, 3.5f, 4.0f, 3.0f, 1.5f, 0.5f, 0f)),
        EqPreset("Classical", listOf(4.0f, 3.0f, 2.0f, 1.0f, 1.0f, 1.5f, 2.0f, 2.5f, 3.0f)),
        EqPreset("Acoustic", listOf(3.5f, 2.5f, 1.5f, 1.0f, 1.5f, 2.5f, 3.0f, 3.5f, 3.0f)),
        EqPreset("Gaming / Movie", listOf(5.5f, 4.0f, 1.5f, -0.5f, 0.5f, 2.0f, 3.0f, 4.5f, 5.0f)),
    )

    // AutoEq profiles mapped to 9 Wavelet bands
    val AUTO_EQ_PROFILES = listOf(
        // Apple
        AutoEqProfile("AirPods Pro 2", "Apple", "In-Ear", listOf(-0.5f, 0.2f, 0.8f, -0.8f, 0.0f, 0.5f, 1.2f, 1.5f, 1.0f)),
        AutoEqProfile("AirPods Pro", "Apple", "In-Ear", listOf(-0.2f, 0.3f, 0.5f, -0.6f, 0.2f, 0.8f, 1.0f, 1.2f, 0.8f)),
        AutoEqProfile("AirPods Max", "Apple", "Over-Ear", listOf(-1.2f, -0.8f, -0.5f, 0.8f, 1.0f, 1.5f, 2.0f, 2.3f, 1.5f)),
        AutoEqProfile("AirPods 3", "Apple", "Earbuds", listOf(1.5f, 1.2f, 1.0f, -0.6f, 0.0f, 0.2f, 0.8f, 1.0f, 0.5f)),
        AutoEqProfile("EarPods (3.5mm)", "Apple", "Earbuds", listOf(3.0f, 2.5f, 2.0f, -0.2f, 0.4f, 0.8f, 1.2f, 1.5f, 1.0f)),
        // Sony
        AutoEqProfile("WH-1000XM5", "Sony", "Over-Ear", listOf(-3.5f, -3.0f, -2.8f, 0.2f, 0.8f, 1.2f, 0.5f, -0.5f, -1.0f)),
        AutoEqProfile("WH-1000XM4", "Sony", "Over-Ear", listOf(-4.0f, -3.5f, -3.2f, 0.1f, 0.6f, 1.0f, 0.2f, -1.0f, -1.5f)),
        AutoEqProfile("WH-1000XM3", "Sony", "Over-Ear", listOf(-4.5f, -4.0f, -3.5f, 0.0f, 0.5f, 0.8f, 0.0f, -1.2f, -2.0f)),
        AutoEqProfile("WF-1000XM5", "Sony", "In-Ear", listOf(-2.0f, -1.8f, -1.5f, 0.5f, 0.8f, 1.0f, 1.2f, 1.2f, 0.5f)),
        AutoEqProfile("WF-1000XM4", "Sony", "In-Ear", listOf(-2.5f, -2.0f, -1.8f, 0.3f, 0.6f, 0.8f, 1.0f, 1.0f, 0.0f)),
        AutoEqProfile("LinkBuds S", "Sony", "In-Ear", listOf(-1.0f, -0.8f, -0.5f, 0.6f, 0.5f, 0.5f, 0.8f, 1.0f, 0.5f)),
        AutoEqProfile("MDR-7506", "Sony", "Over-Ear", listOf(1.5f, 1.0f, 0.8f, -0.8f, -0.2f, -0.5f, -1.5f, -2.0f, -2.5f)),
        // Samsung / AKG
        AutoEqProfile("Galaxy Buds 2 Pro", "Samsung", "In-Ear", listOf(0.5f, 0.2f, 0.0f, -0.5f, 0.2f, 0.5f, 0.8f, 1.0f, 0.5f)),
        AutoEqProfile("Galaxy Buds 2", "Samsung", "In-Ear", listOf(0.0f, -0.2f, -0.5f, 0.1f, 0.5f, 0.8f, 0.8f, 0.5f, 0.0f)),
        AutoEqProfile("Galaxy Buds FE", "Samsung", "In-Ear", listOf(-1.0f, -0.8f, -0.5f, 0.6f, 0.8f, 1.2f, 1.5f, 1.5f, 1.0f)),
        AutoEqProfile("Galaxy Buds Live", "Samsung", "Earbuds", listOf(3.5f, 3.0f, 2.5f, -0.4f, 0.0f, -0.2f, 0.8f, 1.2f, 0.5f)),
        AutoEqProfile("AKG K371", "AKG", "Over-Ear", listOf(-0.5f, -0.2f, 0.2f, -0.1f, 0.0f, 0.2f, 0.4f, 0.5f, 0.2f)),
        AutoEqProfile("AKG N700NC", "AKG", "Over-Ear", listOf(-1.5f, -1.2f, -1.0f, 0.4f, 0.6f, 0.8f, 1.0f, 1.0f, 0.5f)),
        // Sennheiser
        AutoEqProfile("Momentum 4", "Sennheiser", "Over-Ear", listOf(-2.5f, -2.0f, -1.8f, 0.2f, 0.6f, 1.0f, 0.8f, 0.0f, -0.5f)),
        AutoEqProfile("Momentum TW 3", "Sennheiser", "In-Ear", listOf(-2.0f, -1.5f, -1.2f, 0.4f, 0.6f, 0.8f, 0.8f, 0.5f, 0.0f)),
        AutoEqProfile("HD 600", "Sennheiser", "Over-Ear", listOf(4.5f, 3.8f, 3.2f, 0.5f, 0.0f, -0.2f, -0.5f, -0.5f, -1.0f)),
        AutoEqProfile("HD 650", "Sennheiser", "Over-Ear", listOf(4.0f, 3.5f, 2.8f, 0.4f, 0.0f, 0.0f, -0.2f, -0.2f, -0.8f)),
        AutoEqProfile("HD 560S", "Sennheiser", "Over-Ear", listOf(2.5f, 2.0f, 1.5f, 0.0f, -0.2f, -0.5f, 0.0f, 0.2f, 0.0f)),
        AutoEqProfile("IE 200", "Sennheiser", "In-Ear", listOf(1.0f, 0.8f, 0.5f, -0.3f, 0.0f, 0.2f, 0.8f, 1.2f, 1.0f)),
        // Beyerdynamic
        AutoEqProfile("DT 770 Pro (80Ω)", "Beyerdynamic", "Over-Ear", listOf(-1.5f, -1.0f, -0.8f, 0.8f, 0.6f, 0.5f, -1.5f, -3.8f, -4.0f)),
        AutoEqProfile("DT 990 Pro (250Ω)", "Beyerdynamic", "Over-Ear", listOf(3.0f, 2.5f, 2.0f, 0.0f, -0.5f, -0.8f, -2.5f, -4.5f, -5.0f)),
        AutoEqProfile("DT 1990 Pro", "Beyerdynamic", "Over-Ear", listOf(1.5f, 1.0f, 0.8f, -0.2f, -0.2f, -0.2f, -1.5f, -3.0f, -3.5f)),
        // Audio-Technica
        AutoEqProfile("ATH-M50x", "Audio-Technica", "Over-Ear", listOf(-2.0f, -1.8f, -1.5f, 0.5f, 0.8f, 1.0f, 1.2f, 1.5f, 1.0f)),
        AutoEqProfile("ATH-M40x", "Audio-Technica", "Over-Ear", listOf(-0.5f, -0.2f, 0.0f, 0.1f, 0.4f, 0.5f, 0.6f, 0.8f, 0.5f)),
        // Bose
        AutoEqProfile("QuietComfort 45", "Bose", "Over-Ear", listOf(-1.0f, -0.8f, -0.5f, 0.8f, 1.0f, 1.2f, 1.2f, 1.0f, 0.5f)),
        AutoEqProfile("QC Ultra", "Bose", "Over-Ear", listOf(-1.5f, -1.0f, -0.8f, 0.6f, 0.8f, 1.0f, 1.2f, 1.2f, 0.8f)),
        // Moondrop
        AutoEqProfile("Chu II", "Moondrop", "In-Ear", listOf(0.0f, 0.2f, 0.5f, -0.2f, 0.0f, 0.0f, 0.2f, 0.5f, 0.5f)),
        AutoEqProfile("Aria", "Moondrop", "In-Ear", listOf(-0.5f, 0.0f, 0.2f, -0.1f, 0.0f, 0.2f, 0.5f, 0.8f, 0.5f)),
        // Soundcore
        AutoEqProfile("Space Q45", "Soundcore", "Over-Ear", listOf(-3.0f, -2.5f, -2.2f, 0.4f, 0.8f, 1.0f, 1.0f, 0.8f, 0.0f)),
        AutoEqProfile("Liberty 4 NC", "Soundcore", "In-Ear", listOf(-2.5f, -2.0f, -1.8f, 0.6f, 1.0f, 1.2f, 1.2f, 1.0f, 0.5f)),
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
            applyAllGainsToHardware(getBandGains(context))
            isInitialized = true
            Log.d(TAG, "Wavelet 9-band audio effects engine initialized.")
        } catch (e: Exception) {
            Log.w(TAG, "Audio effects initialization note: ${e.message}")
        }
    }

    fun getBandCount(context: Context): Int = WAVELET_FREQUENCIES.size

    fun getBandFrequencies(context: Context): List<Float> = WAVELET_FREQUENCIES

    fun getBandLabels(context: Context): List<String> = WAVELET_LABELS

    fun getBandFullLabels(context: Context): List<String> = WAVELET_FULL_LABELS

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

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
        return (0 until WAVELET_FREQUENCIES.size).map { i ->
            prefs.getFloat("eq_wavelet_band_$i", 0f)
        }
    }

    fun setBandGain(context: Context, bandIndex: Int, gainDb: Float) {
        val clamped = gainDb.coerceIn(-12f, 12f)
        getPrefs(context).edit().putFloat("eq_wavelet_band_$bandIndex", clamped).apply()
        val currentGains = getBandGains(context).toMutableList()
        if (bandIndex < currentGains.size) {
            currentGains[bandIndex] = clamped
        }
        applyAllGainsToHardware(currentGains)
    }

    fun setAllBandGains(context: Context, gains: List<Float>) {
        val editor = getPrefs(context).edit()
        gains.forEachIndexed { i, g ->
            val clamped = g.coerceIn(-12f, 12f)
            editor.putFloat("eq_wavelet_band_$i", clamped)
        }
        editor.apply()
        applyAllGainsToHardware(gains)
    }

    /**
     * Interpolates 9 Wavelet octave bands into the device's hardware Equalizer HAL bands (typically 5 bands)
     * using logarithmic frequency mapping so adjustments are audibly and mathematically precise.
     */
    private fun applyAllGainsToHardware(waveletGains: List<Float>) {
        runCatching {
            equalizer?.let { eq ->
                val numHwBands = eq.numberOfBands.toInt()
                if (numHwBands <= 0) return@let
                val minLevel = eq.bandLevelRange[0]
                val maxLevel = eq.bandLevelRange[1]

                for (hwBand in 0 until numHwBands) {
                    val centerFreqHz = eq.getCenterFreq(hwBand.toShort()) / 1000.0f // in Hz
                    val interpolatedGainDb = interpolateGainAtFrequency(centerFreqHz, waveletGains)
                    val mb = (interpolatedGainDb * 100).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt()).toShort()
                    eq.setBandLevel(hwBand.toShort(), mb)
                }
            }
        }
    }

    private fun interpolateGainAtFrequency(targetFreqHz: Float, waveletGains: List<Float>): Float {
        if (waveletGains.isEmpty()) return 0f
        if (targetFreqHz <= WAVELET_FREQUENCIES.first()) return waveletGains.first()
        if (targetFreqHz >= WAVELET_FREQUENCIES.last()) return waveletGains.last()

        for (i in 0 until WAVELET_FREQUENCIES.size - 1) {
            val f0 = WAVELET_FREQUENCIES[i]
            val f1 = WAVELET_FREQUENCIES[i + 1]
            if (targetFreqHz in f0..f1) {
                val g0 = waveletGains.getOrElse(i) { 0f }
                val g1 = waveletGains.getOrElse(i + 1) { 0f }
                // Logarithmic interpolation
                val logTarget = Math.log(targetFreqHz.toDouble())
                val logF0 = Math.log(f0.toDouble())
                val logF1 = Math.log(f1.toDouble())
                val fraction = ((logTarget - logF0) / (logF1 - logF0)).toFloat().coerceIn(0f, 1f)
                return g0 + fraction * (g1 - g0)
            }
        }
        return waveletGains.first()
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
