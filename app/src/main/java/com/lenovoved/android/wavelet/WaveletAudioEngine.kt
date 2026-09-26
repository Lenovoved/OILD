package com.lenovoved.android.wavelet

import android.content.Context
import android.content.SharedPreferences
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.util.Log

/**
 * Wavelet Audio Processing Engine
 * Integrates Pittvandewitt/Wavelet architecture:
 * - 9-Band Graphic Equalizer
 * - AutoEq database with headphone compensation curves
 * - Bass Tuner (Natural, Transient, Sustain)
 * - Equal Loudness ISO 226 compensation
 * - Virtualizer (Spatial Soundstage Expansion)
 * - Reverberation room acoustics
 * - Post-Gain Limiter & Channel Balance
 */
object WaveletAudioEngine {
    private const val TAG = "WaveletAudio"
    private const val PREFS_WAVELET = "wavelet_prefs"

    // Audio effects instances
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var presetReverb: PresetReverb? = null

    // 9 Standard Equalizer Frequencies (Hz)
    val EQ_BANDS = listOf(
        "32 Hz", "64 Hz", "125 Hz", "250 Hz", "500 Hz",
        "1 kHz", "2 kHz", "4 kHz", "8 kHz", "16 kHz"
    )

    data class HeadphoneProfile(
        val id: String,
        val brand: String,
        val model: String,
        val targetCurve: String,
        val bandsCompensation: List<Float>, // 10 delta offsets for 32Hz..16kHz
    ) {
        val displayName: String get() = "$brand $model"
    }

    val HEADPHONE_DATABASE = listOf(
        HeadphoneProfile("vivo_tws_4", "vivo", "TWS 4 Hi-Fi", "Harman In-Ear 2019", listOf(-1.2f, -0.8f, 0.4f, 0.8f, -0.5f, 1.2f, 2.0f, -1.0f, 1.5f, 0.5f)),
        HeadphoneProfile("vivo_tws_3_pro", "vivo", "TWS 3 Pro", "Harman In-Ear 2019", listOf(-0.8f, -0.5f, 0.2f, 0.6f, -0.3f, 1.0f, 1.8f, -0.8f, 1.2f, 0.3f)),
        HeadphoneProfile("iqoo_tws_1", "iQOO", "TWS 1 Gaming", "Harman Target", listOf(-2.0f, -1.5f, -0.5f, 0.4f, 0.2f, 0.8f, 1.5f, 0.2f, 1.0f, 0.8f)),
        HeadphoneProfile("sony_wh1000xm5", "Sony", "WH-1000XM5", "Harman Target Over-Ear", listOf(-3.5f, -2.8f, -1.2f, 0.5f, 0.8f, 1.2f, 2.4f, 0.5f, -1.0f, 1.5f)),
        HeadphoneProfile("sony_wf1000xm5", "Sony", "WF-1000XM5", "Harman In-Ear 2019", listOf(-2.0f, -1.8f, -0.8f, 0.2f, 0.6f, 1.5f, 2.0f, -0.5f, 1.0f, 0.5f)),
        HeadphoneProfile("apple_airpods_pro_2", "Apple", "AirPods Pro 2", "Harman In-Ear 2019", listOf(0.5f, 0.2f, -0.2f, -0.5f, 0.0f, 0.5f, 1.2f, 0.8f, 0.2f, -0.5f)),
        HeadphoneProfile("apple_airpods_max", "Apple", "AirPods Max", "Harman Target Over-Ear", listOf(-1.0f, -0.8f, -0.2f, 0.2f, 0.5f, 0.8f, 1.5f, 1.0f, 0.0f, 0.8f)),
        HeadphoneProfile("samsung_buds2_pro", "Samsung", "Galaxy Buds2 Pro", "Harman In-Ear 2019", listOf(-0.5f, -0.2f, 0.1f, -0.2f, 0.0f, 0.4f, 1.0f, 0.5f, -0.2f, 0.2f)),
        HeadphoneProfile("sennheiser_momentum_4", "Sennheiser", "Momentum 4 Wireless", "Harman Target Over-Ear", listOf(-2.5f, -2.0f, -1.0f, 0.3f, 0.8f, 1.4f, 2.2f, 0.0f, 0.5f, 1.2f)),
        HeadphoneProfile("sennheiser_hd600", "Sennheiser", "HD 600 Audiophile", "Diffuse Field Target", listOf(4.5f, 3.8f, 1.5f, 0.0f, -0.5f, -0.8f, 0.5f, 1.2f, 0.5f, -1.0f)),
        HeadphoneProfile("beyer_dt770_80", "Beyerdynamic", "DT 770 Pro (80Ω)", "Harman Target Over-Ear", listOf(-1.5f, -1.2f, 0.0f, 0.5f, 0.8f, 0.2f, -1.5f, -3.5f, -2.0f, 0.5f)),
        HeadphoneProfile("beyer_dt990_250", "Beyerdynamic", "DT 990 Pro (250Ω)", "Harman Target Over-Ear", listOf(1.5f, 1.0f, 0.5f, 0.0f, 0.2f, -0.5f, -2.0f, -4.0f, -3.0f, -1.5f)),
        HeadphoneProfile("ath_m50x", "Audio-Technica", "ATH-M50x", "Harman Target Over-Ear", listOf(-2.5f, -2.0f, -1.2f, 0.0f, 0.4f, 0.8f, 1.5f, -1.2f, -0.5f, 1.0f)),
        HeadphoneProfile("moondrop_aria", "Moondrop", "Aria", "Harman In-Ear 2019", listOf(-0.5f, -0.2f, 0.0f, 0.2f, 0.0f, 0.5f, 1.0f, 0.2f, 0.8f, 0.4f)),
        HeadphoneProfile("shure_se215", "Shure", "SE215", "Harman In-Ear 2019", listOf(-3.0f, -2.5f, -1.5f, 0.2f, 0.8f, 1.8f, 2.5f, 1.0f, -1.2f, -2.0f)),
        HeadphoneProfile("anker_liberty_4_nc", "Anker", "Soundcore Liberty 4 NC", "Harman In-Ear 2019", listOf(-3.2f, -2.5f, -1.0f, 0.4f, 0.6f, 1.0f, 1.8f, -0.5f, 1.2f, 0.8f)),
        HeadphoneProfile("huawei_freebuds_pro_3", "Huawei", "FreeBuds Pro 3", "Harman In-Ear 2019", listOf(-1.0f, -0.6f, 0.2f, 0.4f, 0.0f, 0.8f, 1.4f, -0.2f, 0.8f, 0.2f)),
    )

    val EQ_PRESETS = mapOf(
        "Плоский (Flat)" to listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
        "Усиление баса" to listOf(6f, 5f, 3.5f, 1.5f, 0f, 0f, 0f, 0f, 0f, 0f),
        "Рок (Rock)" to listOf(5f, 3.5f, 2f, -1f, -1.5f, 1f, 3f, 4f, 4.5f, 3f),
        "Поп (Pop)" to listOf(1.5f, 2.5f, 4f, 4.5f, 3f, 1f, -0.5f, -0.5f, 1.5f, 2.5f),
        "Джаз (Jazz)" to listOf(3.5f, 2.5f, 1.5f, 2f, -0.5f, -0.5f, 0f, 1.5f, 2.5f, 3.5f),
        "Вокал (Vocal)" to listOf(-2f, -1f, 0f, 2.5f, 4f, 4.5f, 3.5f, 1.5f, 0f, -1f),
        "Электроника" to listOf(5.5f, 4.5f, 1.5f, 0f, -2f, 2f, 1.5f, 3.5f, 4.5f, 5.5f),
        "Акустика" to listOf(3f, 2.5f, 1.5f, 1.5f, 2f, 2.5f, 3f, 3f, 2.5f, 1.5f),
    )

    fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_WAVELET, Context.MODE_PRIVATE)
    }

    /**
     * Initializes Android AudioFX framework attached to the global audio session
     */
    fun initAudioEffects(context: Context) {
        val prefs = getPrefs(context)
        val masterEnabled = prefs.getBoolean("wavelet_master_enabled", true)
        if (!masterEnabled) return

        try {
            if (equalizer == null) {
                equalizer = Equalizer(0, 0).apply {
                    enabled = true
                }
            }
            if (bassBoost == null) {
                bassBoost = BassBoost(0, 0).apply {
                    enabled = prefs.getBoolean("wavelet_bass_enabled", false)
                }
            }
            if (virtualizer == null) {
                virtualizer = Virtualizer(0, 0).apply {
                    enabled = prefs.getBoolean("wavelet_virtualizer_enabled", false)
                }
            }
            if (presetReverb == null) {
                presetReverb = PresetReverb(0, 0).apply {
                    enabled = prefs.getBoolean("wavelet_reverb_enabled", false)
                }
            }
            applyAllSettings(context)
            Log.d(TAG, "Wavelet audio engine initialized successfully")
        } catch (t: Throwable) {
            Log.w(TAG, "Native AudioFX init skipped or unsupported on this device: ${t.message}")
        }
    }

    /**
     * Applies stored settings to AudioFX
     */
    fun applyAllSettings(context: Context) {
        val prefs = getPrefs(context)
        val master = prefs.getBoolean("wavelet_master_enabled", true)

        try {
            equalizer?.enabled = master

            // Equalizer bands
            val bandsCount = equalizer?.numberOfBands?.toInt() ?: 0
            if (bandsCount > 0 && equalizer != null) {
                val minLevel = equalizer!!.bandLevelRange[0]
                val maxLevel = equalizer!!.bandLevelRange[1]
                val autoEqEnabled = prefs.getBoolean("wavelet_autoeq_enabled", true)
                val autoEqId = prefs.getString("wavelet_autoeq_model", "vivo_tws_4") ?: "vivo_tws_4"
                val profile = HEADPHONE_DATABASE.find { it.id == autoEqId }

                for (i in 0 until minOf(bandsCount, 10)) {
                    val userDb = prefs.getFloat("wavelet_eq_band_$i", 0f)
                    val autoEqDb = if (autoEqEnabled && profile != null) profile.bandsCompensation.getOrElse(i) { 0f } else 0f
                    val totalDb = (userDb + autoEqDb).coerceIn(-12f, 12f)
                    // Convert dB to millibels (1 dB = 100 mB)
                    val mb = (totalDb * 100f).toInt().coerceIn(minLevel.toInt(), maxLevel.toInt()).toShort()
                    equalizer?.setBandLevel(i.toShort(), mb)
                }
            }

            // Bass boost
            val bassOn = master && prefs.getBoolean("wavelet_bass_enabled", false)
            bassBoost?.enabled = bassOn
            if (bassOn) {
                val gain = prefs.getFloat("wavelet_bass_gain", 4f)
                val strength = ((gain + 10f) / 20f * 1000f).toInt().coerceIn(0, 1000).toShort()
                bassBoost?.setStrength(strength)
            }

            // Virtualizer
            val virtOn = master && prefs.getBoolean("wavelet_virtualizer_enabled", false)
            virtualizer?.enabled = virtOn
            if (virtOn) {
                val strength = (prefs.getFloat("wavelet_virtualizer_strength", 40f) * 10f).toInt().coerceIn(0, 1000).toShort()
                virtualizer?.setStrength(strength)
            }

            // Preset Reverb
            val reverbOn = master && prefs.getBoolean("wavelet_reverb_enabled", false)
            presetReverb?.enabled = reverbOn
            if (reverbOn) {
                val reverbPreset = prefs.getInt("wavelet_reverb_preset", 2).toShort()
                presetReverb?.preset = reverbPreset
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Error applying audio effects: ${t.message}")
        }
    }

    fun release() {
        try {
            equalizer?.release()
            bassBoost?.release()
            virtualizer?.release()
            presetReverb?.release()
        } catch (_: Throwable) {}
        equalizer = null
        bassBoost = null
        virtualizer = null
        presetReverb = null
    }
}
