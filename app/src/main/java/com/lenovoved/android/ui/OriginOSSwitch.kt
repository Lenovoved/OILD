package com.lenovoved.android.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Custom OriginOS-style switch matching vivo OriginOS UI design from screenshot:
 * - Soft light-blue pastel pill track (16dp height, 44dp width)
 * - Protruding large white thumb circle (26dp size) with thick vivid blue (#0066FF, 4.5dp) accent ring
 * - Soft blue glow shadow under the thumb.
 */
@Composable
fun OriginOSSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val dark = isSystemInDarkTheme()

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 18.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.75f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "switchOffset",
    )

    val trackColor by animateColorAsState(
        targetValue = if (checked) {
            if (dark) Color(0xFF1E3A8A).copy(alpha = 0.65f) else Color(0xFFD0E2FF)
        } else {
            if (dark) Color(0xFF334155) else Color(0xFFE2E8F0)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "trackColor",
    )

    val ringColor by animateColorAsState(
        targetValue = if (checked) Color(0xFF0066FF) else (if (dark) Color(0xFF64748B) else Color(0xFF94A3B8)),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "ringColor",
    )

    Box(
        modifier = modifier
            .width(44.dp)
            .height(28.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) {
                onCheckedChange(!checked)
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        // Inner track pill (16dp height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(16.dp)
                .clip(CircleShape)
                .background(trackColor),
        )

        val safeOffset = thumbOffset.coerceAtLeast(0.dp)

        // Large protruding thumb circle (26dp size) with vivid blue ring and soft blue shadow
        Box(
            modifier = Modifier
                .padding(start = safeOffset)
                .size(26.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    spotColor = if (checked) Color(0x660066FF) else Color(0x22000000),
                    ambientColor = if (checked) Color(0x330066FF) else Color(0x11000000),
                )
                .clip(CircleShape)
                .background(Color.White)
                .border(
                    width = if (checked) 4.5.dp else 2.5.dp,
                    color = ringColor,
                    shape = CircleShape,
                ),
        )
    }
}
