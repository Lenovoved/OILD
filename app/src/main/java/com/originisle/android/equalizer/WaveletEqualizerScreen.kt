package com.originisle.android.equalizer

import android.content.Context
import android.media.audiofx.PresetReverb
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SpatialAudio
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import com.originisle.android.ui.OriginOSSlider
import com.originisle.android.ui.OriginOSSwitch
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Modern, harmonious Wavelet-inspired Audio Equalizer Screen.
 * Beautifully matches the OriginOS design system with soft rounded cards,
 * interactive 10-band spline curve canvas, AutoEq headphone profiles,
 * and high-fidelity DSP sound enhancement controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaveletEqualizerScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // Initialize audio effects engine
    LaunchedEffect(Unit) {
        WaveletAudioEngine.initAudioEffects(context)
    }

    var masterEnabled by remember { mutableStateOf(WaveletAudioEngine.getMasterEnabled(context)) }
    var selectedPreset by remember { mutableStateOf(WaveletAudioEngine.getSelectedPreset(context)) }

    val bandCount = WaveletAudioEngine.getBandCount(context)
    val bandLabels = WaveletAudioEngine.getBandLabels(context)

    // Band gains (5 real working bands: 60Hz, 230Hz, 910Hz, 3.6kHz, 14kHz)
    val bandGains = remember {
        mutableStateListOf<Float>().apply {
            addAll(WaveletAudioEngine.getBandGains(context))
            while (size < bandCount) add(0f)
        }
    }

    // AutoEq
    var autoEqEnabled by remember { mutableStateOf(WaveletAudioEngine.getAutoEqEnabled(context)) }
    var selectedHeadphoneModel by remember { mutableStateOf(WaveletAudioEngine.getSelectedAutoEqProfile(context)) }
    var showHeadphonePicker by remember { mutableStateOf(false) }

    // Bass Boost
    var bassBoostEnabled by remember { mutableStateOf(WaveletAudioEngine.getBassBoostEnabled(context)) }
    var bassBoostStrength by remember { mutableFloatStateOf(WaveletAudioEngine.getBassBoostStrength(context)) }

    // Virtualizer
    var virtualizerEnabled by remember { mutableStateOf(WaveletAudioEngine.getVirtualizerEnabled(context)) }
    var virtualizerStrength by remember { mutableFloatStateOf(WaveletAudioEngine.getVirtualizerStrength(context)) }

    // Reverb
    var reverbEnabled by remember { mutableStateOf(WaveletAudioEngine.getReverbEnabled(context)) }
    var reverbPreset by remember { mutableStateOf(WaveletAudioEngine.getReverbPreset(context)) }

    // Preamp / Limiter
    var preampGain by remember { mutableFloatStateOf(WaveletAudioEngine.getPreampGain(context)) }
    var limiterEnabled by remember { mutableStateOf(WaveletAudioEngine.getLimiterEnabled(context)) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(14.dp))

        // Category Tag Pill
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1677FF).copy(alpha = 0.10f))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (masterEnabled) Color(0xFF1677FF) else Color(0xFF94A3B8)),
                )
                Text(
                    text = if (masterEnabled) "Wavelet DSP Engine • Активен" else "Wavelet DSP Engine • Отключен",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (masterEnabled) Color(0xFF1677FF) else Color(0xFF64748B),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Screen Title & Subtitle
        Text(
            text = "Эквалайзер Wavelet",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1C1E),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "5-полосный эквалайзер с реальными рабочими частотами (60 Гц – 14 кГц), калибровка наушников AutoEq и эффекты DSP.",
            fontSize = 13.5.sp,
            lineHeight = 19.sp,
            color = Color(0xFF5F6368),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Master Switch Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x10000000)),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1677FF).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Wavelet Master",
                            tint = Color(0xFF1677FF),
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    Column {
                        Text(
                            text = "Главный процессор звука",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A1C1E),
                        )
                        Text(
                            text = if (masterEnabled) "Эквалайзер и эффекты применены" else "Обработка звука выключена",
                            fontSize = 12.sp,
                            color = if (masterEnabled) Color(0xFF1677FF) else Color(0xFF8C9098),
                        )
                    }
                }

                OriginOSSwitch(
                    checked = masterEnabled,
                    onCheckedChange = { checked ->
                        masterEnabled = checked
                        WaveletAudioEngine.setMasterEnabled(context, checked)
                        Toast.makeText(
                            context,
                            if (checked) "Эквалайзер Wavelet включен" else "Эквалайзер Wavelet выключен",
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                    modifier = Modifier.testTag("wavelet_master_switch"),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Graphical Equalizer & Frequency Curve Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x10000000)),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
                // Header with Reset button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1677FF).copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Graphic EQ",
                                tint = Color(0xFF1677FF),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column {
                            Text(
                                text = "Графический эквалайзер",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1C1E),
                            )
                            Text(
                                text = "$bandCount полос: 60 Гц – 14 кГц (±12 дБ)",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            val flatGains = List(bandCount) { 0f }
                            for (i in 0 until bandCount) {
                                if (i < bandGains.size) bandGains[i] = 0f
                            }
                            selectedPreset = "Flat"
                            WaveletAudioEngine.setAllBandGains(context, flatGains)
                            WaveletAudioEngine.setSelectedPreset(context, "Flat")
                            Toast.makeText(context, "Эквалайзер сброшен в Flat", Toast.LENGTH_SHORT).show()
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Сброс",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Presets Horizontal Scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    WaveletAudioEngine.PRESETS.forEach { preset ->
                        val isSelected = selectedPreset == preset.name
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedPreset = preset.name
                                WaveletAudioEngine.setSelectedPreset(context, preset.name)
                                preset.gains.forEachIndexed { idx, g ->
                                    if (idx < bandGains.size) bandGains[idx] = g
                                }
                                WaveletAudioEngine.setAllBandGains(context, preset.gains)
                            },
                            label = { Text(preset.name, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF1677FF),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF1F5F9),
                                labelColor = Color(0xFF334155),
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = null,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Harmonious Frequency Response Curve Visualizer
                OriginEqualizerCurveCanvas(
                    gains = bandGains,
                    onGainChanged = { bandIndex, newGain ->
                        bandGains[bandIndex] = newGain
                        selectedPreset = "Пользовательский"
                        WaveletAudioEngine.setBandGain(context, bandIndex, newGain)
                        WaveletAudioEngine.setSelectedPreset(context, "Пользовательский")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFFF6F8FB))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp)),
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Частотные полосы (дБ):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155),
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Working Band Gain Sliders
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (i in 0 until bandCount) {
                        val freqLabel = bandLabels.getOrElse(i) { "${i + 1}" }
                        val currentGain = bandGains.getOrElse(i) { 0f }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = freqLabel,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF475569),
                                modifier = Modifier.width(72.dp),
                            )

                            OriginOSSlider(
                                value = currentGain,
                                onValueChange = { newVal ->
                                    val rounded = (newVal * 2).roundToInt() / 2.0f
                                    bandGains[i] = rounded
                                    selectedPreset = "Пользовательский"
                                    WaveletAudioEngine.setBandGain(context, i, rounded)
                                    WaveletAudioEngine.setSelectedPreset(context, "Пользовательский")
                                },
                                valueRange = -12f..12f,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 4.dp),
                                activeColor = Color(0xFF0066FF),
                                inactiveColor = Color(0xFFE2E8F0),
                            )

                            Text(
                                text = String.format("%+.1f дБ", currentGain),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (currentGain > 0) Color(0xFF1677FF) else if (currentGain < 0) Color(0xFFEF4444) else Color(0xFF64748B),
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(62.dp),
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. AutoEq Headphone Calibration Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x10000000)),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1677FF).copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = "AutoEq",
                                tint = Color(0xFF1677FF),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column {
                            Text(
                                text = "AutoEq (Калибровка наушников)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1C1E),
                            )
                            Text(
                                text = "Компенсация АЧХ по целевой кривой Harman",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                            )
                        }
                    }

                    OriginOSSwitch(
                        checked = autoEqEnabled && masterEnabled,
                        onCheckedChange = { checked ->
                            if (!masterEnabled) return@OriginOSSwitch
                            autoEqEnabled = checked
                            WaveletAudioEngine.setAutoEqEnabled(context, checked)
                            if (checked) {
                                WaveletAudioEngine.setSelectedAutoEqProfile(context, selectedHeadphoneModel)
                                val profile = WaveletAudioEngine.AUTO_EQ_PROFILES.find { it.model == selectedHeadphoneModel }
                                profile?.gains?.let { g ->
                                    for (i in g.indices) {
                                        if (i < bandGains.size) bandGains[i] = g[i]
                                    }
                                    selectedPreset = "AutoEq: ${profile.model}"
                                }
                            }
                        },
                    )
                }

                AnimatedVisibility(
                    visible = autoEqEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF1F5F9))
                                .clickable { showHeadphonePicker = true }
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Выбранный профиль калибровки",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF64748B),
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = selectedHeadphoneModel,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F172A),
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1677FF).copy(alpha = 0.12f))
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                ) {
                                    Text(
                                        text = "Изменить",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1677FF),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Bass Boost Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x10000000)),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF2563EB).copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Waves,
                                contentDescription = "Bass Boost",
                                tint = Color(0xFF2563EB),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column {
                            Text(
                                text = "Усиление басов (Bass Boost)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1C1E),
                            )
                            Text(
                                text = "Глубокие суб-басы и динамический панч",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                            )
                        }
                    }

                    OriginOSSwitch(
                        checked = bassBoostEnabled && masterEnabled,
                        onCheckedChange = { checked ->
                            if (!masterEnabled) return@OriginOSSwitch
                            bassBoostEnabled = checked
                            WaveletAudioEngine.setBassBoostEnabled(context, checked)
                        },
                    )
                }

                AnimatedVisibility(
                    visible = bassBoostEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OriginOSSlider(
                                value = bassBoostStrength,
                                onValueChange = {
                                    bassBoostStrength = it
                                    WaveletAudioEngine.setBassBoostStrength(context, it)
                                },
                                valueRange = 0f..100f,
                                modifier = Modifier.weight(1f),
                                activeColor = Color(0xFF0066FF),
                                inactiveColor = Color(0xFFE2E8F0),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${bassBoostStrength.toInt()}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1677FF),
                                modifier = Modifier.width(44.dp),
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Virtualizer / 3D Spatial Audio Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x10000000)),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF7C3AED).copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.SurroundSound,
                                contentDescription = "Virtualizer",
                                tint = Color(0xFF7C3AED),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column {
                            Text(
                                text = "Виртуализатор (3D Звук)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1C1E),
                            )
                            Text(
                                text = "Расширение стереопанорамы и эффект сцены",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                            )
                        }
                    }

                    OriginOSSwitch(
                        checked = virtualizerEnabled && masterEnabled,
                        onCheckedChange = { checked ->
                            if (!masterEnabled) return@OriginOSSwitch
                            virtualizerEnabled = checked
                            WaveletAudioEngine.setVirtualizerEnabled(context, checked)
                        },
                    )
                }

                AnimatedVisibility(
                    visible = virtualizerEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OriginOSSlider(
                                value = virtualizerStrength,
                                onValueChange = {
                                    virtualizerStrength = it
                                    WaveletAudioEngine.setVirtualizerStrength(context, it)
                                },
                                valueRange = 0f..100f,
                                modifier = Modifier.weight(1f),
                                activeColor = Color(0xFF7C3AED),
                                inactiveColor = Color(0xFFE2E8F0),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${virtualizerStrength.toInt()}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7C3AED),
                                modifier = Modifier.width(44.dp),
                                textAlign = TextAlign.End,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Reverberation Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x10000000)),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFD97706).copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.SpatialAudio,
                                contentDescription = "Reverberation",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column {
                            Text(
                                text = "Реверберация (Акустика зала)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1C1E),
                            )
                            Text(
                                text = "Имитация акустического пространства",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                            )
                        }
                    }

                    OriginOSSwitch(
                        checked = reverbEnabled && masterEnabled,
                        onCheckedChange = { checked ->
                            if (!masterEnabled) return@OriginOSSwitch
                            reverbEnabled = checked
                            WaveletAudioEngine.setReverbEnabled(context, checked)
                        },
                    )
                }

                AnimatedVisibility(
                    visible = reverbEnabled,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically(),
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(14.dp))
                        val reverbOptions = listOf(
                            "Малая комната" to PresetReverb.PRESET_SMALLROOM.toInt(),
                            "Средняя комната" to PresetReverb.PRESET_MEDIUMROOM.toInt(),
                            "Большой зал" to PresetReverb.PRESET_LARGEHALL.toInt(),
                            "Концертная сцена" to PresetReverb.PRESET_LARGEROOM.toInt(),
                            "Пластина" to PresetReverb.PRESET_PLATE.toInt(),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            reverbOptions.forEach { (name, id) ->
                                val isSelected = reverbPreset == id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        reverbPreset = id
                                        WaveletAudioEngine.setReverbPreset(context, id)
                                    },
                                    label = { Text(name, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFD97706),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = Color(0xFF334155),
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    border = null,
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Preamp & Limiter Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(26.dp), spotColor = Color(0x10000000)),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF059669).copy(alpha = 0.10f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Limiter & Preamp",
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(20.dp),
                            )
                        }
                        Column {
                            Text(
                                text = "Предусиление и Лимитер",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1A1C1E),
                            )
                            Text(
                                text = "Защита от перегрузки и клиппинга звука",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                            )
                        }
                    }

                    OriginOSSwitch(
                        checked = limiterEnabled && masterEnabled,
                        onCheckedChange = { checked ->
                            if (!masterEnabled) return@OriginOSSwitch
                            limiterEnabled = checked
                            WaveletAudioEngine.setLimiterEnabled(context, checked)
                        },
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Preamp:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF475569),
                        modifier = Modifier.width(62.dp),
                    )
                    OriginOSSlider(
                        value = preampGain,
                        onValueChange = {
                            val rounded = (it * 2).roundToInt() / 2.0f
                            preampGain = rounded
                            WaveletAudioEngine.setPreampGain(context, rounded)
                        },
                        valueRange = -12f..12f,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        activeColor = Color(0xFF059669),
                        inactiveColor = Color(0xFFE2E8F0),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format("%+.1f дБ", preampGain),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF059669),
                        modifier = Modifier.width(62.dp),
                        textAlign = TextAlign.End,
                    )
                }
            }
        }

        // Padding for the bottom navigation pill dock
        Spacer(modifier = Modifier.height(110.dp))
    }

    // AutoEq Headphone Profile Selection Modal Bottom Sheet
    if (showHeadphonePicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var searchQuery by remember { mutableStateOf("") }
        val filteredProfiles = remember(searchQuery) {
            if (searchQuery.isBlank()) {
                WaveletAudioEngine.AUTO_EQ_PROFILES
            } else {
                WaveletAudioEngine.AUTO_EQ_PROFILES.filter {
                    it.model.contains(searchQuery, ignoreCase = true) ||
                        it.manufacturer.contains(searchQuery, ignoreCase = true)
                }
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showHeadphonePicker = false },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
            ) {
                Text(
                    text = "База калибровок AutoEq",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                )
                Text(
                    text = "Точные кривые компенсации Harman Target для 5,000+ наушников",
                    fontSize = 12.5.sp,
                    color = Color(0xFF64748B),
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Поиск модели (Sony, Apple, Bose, Sennheiser...)", fontSize = 13.5.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Поиск", tint = Color(0xFF64748B))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Очистить", tint = Color(0xFF64748B))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1677FF),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                    ),
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredProfiles, key = { it.model }) { profile ->
                        val isSelected = selectedHeadphoneModel == profile.model
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(0xFFEBF3FE) else Color(0xFFF8FAFC))
                                .clickable {
                                    selectedHeadphoneModel = profile.model
                                    autoEqEnabled = true
                                    WaveletAudioEngine.setAutoEqEnabled(context, true)
                                    WaveletAudioEngine.setSelectedAutoEqProfile(context, profile.model)
                                    profile.gains.forEachIndexed { i, g ->
                                        if (i < bandGains.size) bandGains[i] = g
                                    }
                                    selectedPreset = "AutoEq: ${profile.model}"
                                    showHeadphonePicker = false
                                    Toast.makeText(context, "Применен профиль ${profile.model}", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    text = profile.model,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSelected) Color(0xFF1677FF) else Color(0xFF0F172A),
                                )
                                Text(
                                    text = "${profile.manufacturer} • ${profile.type}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF64748B),
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Выбрано",
                                    tint = Color(0xFF1677FF),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws an interactive, harmonious Cubic Bezier Spline frequency curve on Canvas
 * representing the 10-band equalizer gains with smooth dragging support and clear dB/frequency axes.
 */
@Composable
private fun OriginEqualizerCurveCanvas(
    gains: List<Float>,
    onGainChanged: (Int, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier.pointerInput(gains) {
            detectDragGestures { change, _ ->
                change.consume()
                val width = size.width.toFloat()
                val height = size.height.toFloat()
                val paddingHorizontal = 24.dp.toPx()
                val paddingVertical = 16.dp.toPx()
                val drawWidth = width - (paddingHorizontal * 2)
                val drawHeight = height - (paddingVertical * 2)
                val midY = paddingVertical + (drawHeight / 2)

                val touchX = change.position.x
                val touchY = change.position.y

                val fractionX = ((touchX - paddingHorizontal) / drawWidth).coerceIn(0f, 1f)
                val bandIndex = (fractionX * (gains.size - 1)).roundToInt().coerceIn(0, gains.size - 1)

                val gainFraction = (midY - touchY) / (drawHeight / 2)
                val calculatedGain = (gainFraction * 12f).coerceIn(-12f, 12f)
                val roundedGain = (calculatedGain * 2).roundToInt() / 2.0f

                onGainChanged(bandIndex, roundedGain)
            }
        },
    ) {
        val width = size.width
        val height = size.height
        val paddingHorizontal = 24.dp.toPx()
        val paddingVertical = 16.dp.toPx()

        val drawWidth = width - (paddingHorizontal * 2)
        val drawHeight = height - (paddingVertical * 2)
        val midY = paddingVertical + (drawHeight / 2)

        // Draw grid lines
        val gridColor = Color(0xFFE2E8F0)
        val zeroLineColor = Color(0xFFCBD5E1)

        // Horizontal reference lines: +12, +6, 0, -6, -12 dB
        val dbLevels = listOf(12f, 6f, 0f, -6f, -12f)
        dbLevels.forEach { db ->
            val y = midY - (db / 12f) * (drawHeight / 2)
            drawLine(
                color = if (db == 0f) zeroLineColor else gridColor,
                start = Offset(paddingHorizontal, y),
                end = Offset(width - paddingHorizontal, y),
                strokeWidth = if (db == 0f) 1.5f else 0.8f,
            )
        }

        val pointCount = gains.size
        if (pointCount < 2) return@Canvas

        val points = mutableListOf<Offset>()
        for (i in 0 until pointCount) {
            val x = paddingHorizontal + (i.toFloat() / (pointCount - 1)) * drawWidth
            val gain = gains.getOrElse(i) { 0f }.coerceIn(-12f, 12f)
            val y = midY - (gain / 12f) * (drawHeight / 2)
            points.add(Offset(x, y))
        }

        // Build smooth Cubic Bezier Path through points
        val curvePath = Path().apply {
            moveTo(points[0].x, points[0].y)
            for (i in 0 until points.size - 1) {
                val p0 = if (i > 0) points[i - 1] else points[i]
                val p1 = points[i]
                val p2 = points[i + 1]
                val p3 = if (i < points.size - 2) points[i + 2] else p2

                val cp1x = p1.x + (p2.x - p0.x) / 6f
                val cp1y = p1.y + (p2.y - p0.y) / 6f
                val cp2x = p2.x - (p3.x - p1.x) / 6f
                val cp2y = p2.y - (p3.y - p1.y) / 6f

                cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
            }
        }

        // Fill area below curve with soft Origin Blue gradient
        val fillPath = Path().apply {
            addPath(curvePath)
            lineTo(width - paddingHorizontal, height - paddingVertical)
            lineTo(paddingHorizontal, height - paddingVertical)
            close()
        }

        val gradientBrush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF1677FF).copy(alpha = 0.35f),
                Color(0xFF1677FF).copy(alpha = 0.04f),
            ),
            startY = paddingVertical,
            endY = height - paddingVertical,
        )

        drawPath(fillPath, brush = gradientBrush)

        // Draw the main curve line
        drawPath(
            path = curvePath,
            color = Color(0xFF1677FF),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round),
        )

        // Draw node circles
        points.forEach { pt ->
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = pt,
            )
            drawCircle(
                color = Color(0xFF1677FF),
                radius = 3.5.dp.toPx(),
                center = pt,
            )
        }
    }
}
