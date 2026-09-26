package com.lenovoved.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * OriginOS-styled slider matching Vivo OriginOS:
 * - Clean thin rounded track (5dp) with vivid blue (#0066FF) active section
 * - Soft gray inactive track
 * - Protruding white circular thumb (26dp) with drop shadow and vibrant blue (#0066FF, 4.5dp) accent ring.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OriginOSSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    enabled: Boolean = true,
    activeColor: Color = Color(0xFF0066FF),
    inactiveColor: Color? = null,
    thumbSize: Dp = 26.dp,
    trackHeight: Dp = 5.dp,
) {
    val dark = isSystemInDarkTheme()
    val resolvedInactiveColor = inactiveColor ?: (if (dark) Color(0xFF334155) else Color(0xFFEAEAEE))
    val interactionSource = remember { MutableInteractionSource() }

    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        interactionSource = interactionSource,
        thumb = {
            Box(
                modifier = Modifier
                    .size(thumbSize)
                    .shadow(
                        elevation = 4.dp,
                        shape = CircleShape,
                        spotColor = if (enabled) Color(0x660066FF) else Color(0x22000000),
                        ambientColor = if (enabled) Color(0x330066FF) else Color(0x11000000),
                    )
                    .background(Color.White, CircleShape)
                    .border(
                        width = 4.5.dp,
                        color = if (enabled) activeColor else Color(0xFF94A3B8),
                        shape = CircleShape,
                    ),
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState = sliderState,
                modifier = Modifier.height(trackHeight),
                colors = SliderDefaults.colors(
                    activeTrackColor = activeColor,
                    inactiveTrackColor = resolvedInactiveColor,
                    disabledActiveTrackColor = activeColor.copy(alpha = 0.4f),
                    disabledInactiveTrackColor = resolvedInactiveColor.copy(alpha = 0.6f),
                ),
            )
        },
    )
}
