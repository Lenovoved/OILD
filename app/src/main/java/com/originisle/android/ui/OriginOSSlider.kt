package com.originisle.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
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
 * - Rounded 10dp track with vivid blue (#0066FF) active section
 * - Soft gray (#E2E8F0) inactive track
 * - White circular thumb with drop shadow and a vibrant blue (#0066FF) 3.5dp accent ring,
 *   matching the OriginOSSwitch tumbler design.
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
    inactiveColor: Color = Color(0xFFE2E8F0),
    thumbSize: Dp = 24.dp,
    trackHeight: Dp = 10.dp,
) {
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
                        elevation = 3.dp,
                        shape = CircleShape,
                        spotColor = Color(0x33000000),
                    )
                    .background(Color.White, CircleShape)
                    .border(
                        width = 3.5.dp,
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
                    inactiveTrackColor = inactiveColor,
                    disabledActiveTrackColor = activeColor.copy(alpha = 0.4f),
                    disabledInactiveTrackColor = inactiveColor.copy(alpha = 0.6f),
                ),
            )
        },
    )
}
