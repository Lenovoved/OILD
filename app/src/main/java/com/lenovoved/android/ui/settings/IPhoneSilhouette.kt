package com.lenovoved.android.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Silhouette of the top half of an iPhone with an animated Dynamic Island
 * that opens smoothly on screen launch with spring physics and supports interactive tap.
 */
@Composable
fun IPhoneHalfSilhouette(
    modifier: Modifier = Modifier,
) {
    val dark = isSystemInDarkTheme()
    val currentTime = remember {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date())
    }

    // State controlling island opening animation
    var isIslandExpanded by remember { mutableStateOf(false) }
    var isReadMarked by remember { mutableStateOf(false) }

    // Trigger island opening animation on appearance
    LaunchedEffect(Unit) {
        // Start closed, then expand with bouncy spring
        isIslandExpanded = false
        delay(350)
        isIslandExpanded = true
    }

    // Spring animation for island dimensions
    val islandWidth by animateDpAsState(
        targetValue = if (isIslandExpanded) 262.dp else 84.dp,
        animationSpec = spring(
            dampingRatio = 0.62f, // spring bounce overshoot
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "IslandWidth",
    )

    val islandHeight by animateDpAsState(
        targetValue = if (isIslandExpanded) 58.dp else 26.dp,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "IslandHeight",
    )

    val islandCornerRadius by animateDpAsState(
        targetValue = if (isIslandExpanded) 22.dp else 13.dp,
        animationSpec = spring(
            dampingRatio = 0.62f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "IslandCornerRadius",
    )

    // Animated glow pulse around the active island
    val infiniteTransition = rememberInfiniteTransition(label = "IslandPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "PulseAlpha",
    )

    // Titanium frame and bezel colors
    val frameBorderBrush = Brush.verticalGradient(
        colors = if (dark) {
            listOf(
                Color(0xFF52525B), // Titanium highlight
                Color(0xFF27272A),
                Color(0xFF18181B),
            )
        } else {
            listOf(
                Color(0xFFCBD5E1), // Light titanium highlight
                Color(0xFF94A3B8),
                Color(0xFF64748B),
            )
        },
    )

    val phoneBgBrush = Brush.verticalGradient(
        colors = if (dark) {
            listOf(
                Color(0xFF1E2024), // Dark grey but not pitch black
                Color(0xFF2A2D34),
                Color(0xFF1E2024),
            )
        } else {
            listOf(
                Color(0xFFF8FAFC), // Light grey/white screen background
                Color(0xFFE2E8F0),
                Color(0xFFF8FAFC),
            )
        },
    )

    val statusColor = if (dark) Color(0xFFF1F5F9) else Color(0xFF1E293B)

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // iPhone top half container
        Box(
            modifier = Modifier
                .width(312.dp)
                .height(182.dp)
                .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Black, Color.Transparent),
                            startY = size.height * 0.65f, // Start fading from 65% of height down to bottom
                            endY = size.height
                        ),
                        blendMode = BlendMode.DstIn
                    )
                }
                .shadow(
                    elevation = if (dark) 14.dp else 10.dp,
                    shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                    spotColor = if (dark) Color(0x60000000) else Color(0x25000000),
                )
                // Outer Titanium Frame
                .clip(RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
                .background(if (dark) Color(0xFF1C1C1E) else Color(0xFFE2E8F0))
                .border(
                    width = 2.dp,
                    brush = frameBorderBrush,
                    shape = RoundedCornerShape(topStart = 48.dp, topEnd = 48.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                )
                .padding(3.dp), // Inner bezel thickness
            contentAlignment = Alignment.TopCenter,
        ) {
            // Screen area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 44.dp, topEnd = 44.dp, bottomStart = 6.dp, bottomEnd = 6.dp))
                    .background(phoneBgBrush)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) {
                        // Tap to toggle / replay island opening
                        isIslandExpanded = !isIslandExpanded
                    },
            ) {
                // Subtle wallpaper radial glow inside the phone screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0x3538BDF8),
                                    Color(0x18818CF8),
                                    Color.Transparent,
                                ),
                                radius = 280f,
                            ),
                        ),
                )

                // Status Bar Row (Time on left, Signal/Wifi/Battery on right)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 11.dp, start = 24.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Current time
                    Text(
                        text = currentTime,
                        color = statusColor,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.2.sp,
                    )

                    // Right status icons (Cellular bars, Wi-Fi, Battery)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // 4-bar cellular signal
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(1.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.height(9.dp),
                        ) {
                            Box(modifier = Modifier.width(2.dp).height(3.dp).background(statusColor, RoundedCornerShape(0.5.dp)))
                            Box(modifier = Modifier.width(2.dp).height(5.dp).background(statusColor, RoundedCornerShape(0.5.dp)))
                            Box(modifier = Modifier.width(2.dp).height(7.dp).background(statusColor, RoundedCornerShape(0.5.dp)))
                            Box(modifier = Modifier.width(2.dp).height(9.dp).background(statusColor, RoundedCornerShape(0.5.dp)))
                        }

                        // Wi-Fi icon
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.size(11.dp),
                        )

                        // Battery pill with level
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height(9.dp)
                                .border(1.dp, statusColor, RoundedCornerShape(3.dp))
                                .padding(1.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.85f)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(Color(0xFF22C55E)), // Green battery charge
                            )
                        }
                    }
                }

                // Dynamic Island Container (Centered horizontally at top)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .width(islandWidth)
                        .height(islandHeight)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(islandCornerRadius),
                            spotColor = Color(0xAA000000),
                        )
                        .border(
                            width = 0.8.dp,
                            color = if (isIslandExpanded) Color(0x3038BDF8) else Color(0x18FFFFFF),
                            shape = RoundedCornerShape(islandCornerRadius),
                        )
                        .clip(RoundedCornerShape(islandCornerRadius))
                        .background(Color(0xFF000000)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isIslandExpanded) {
                        // Expanded Island Content
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            // Left badge: Origin dynamic icon with glowing accent
                            Box(
                                modifier = Modifier
                                    .size(33.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFFF5722), Color(0xFF0066FF)),
                                        ),
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp),
                                )
                            }

                            Spacer(modifier = Modifier.width(7.dp))

                            // Center Info
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    text = "Origin Island",
                                    color = Color.White,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Text(
                                    text = if (isReadMarked) "Прочитано" else "Остров активен",
                                    color = if (isReadMarked) Color(0xFF4ADE80) else Color(0xFF38BDF8),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                )
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            // Right side: Capsule button with "Пометить как прочитанное" in 2 lines (micro capsule)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .background(
                                        if (isReadMarked) Color(0xFF14532D)
                                        else Color(0xFF27272A)
                                    )
                                    .border(
                                        width = 0.6.dp,
                                        color = if (isReadMarked) Color(0xFF22C55E) else Color(0x40FFFFFF),
                                        shape = RoundedCornerShape(percent = 50),
                                    )
                                    .clickable {
                                        isReadMarked = !isReadMarked
                                    }
                                    .padding(horizontal = 5.dp, vertical = 2.5.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Пометить как\nпрочитанное",
                                    color = if (isReadMarked) Color(0xFF86EFAC) else Color(0xFFF4F4F5),
                                    fontSize = 5.5.sp,
                                    lineHeight = 6.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    } else {
                        // Compact Island: Camera cutout and FaceID dot
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Camera lens dot with subtle blue reflection
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16181F)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(3.5.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E3A8A)),
                                )
                            }
                            // FaceID dot
                            Box(
                                modifier = Modifier
                                    .size(7.5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF141416)),
                            )
                        }
                    }
                }

                // Lock screen date & hint text below the island
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Origin Dynamic Island",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B),
                    )
                }

                // Screen area elements finish
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Subtle hint showing user can tap to re-trigger opening animation
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { isIslandExpanded = !isIslandExpanded }
                .padding(horizontal = 8.dp, vertical = 2.dp),
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = if (dark) Color(0xFF71717A) else Color(0xFF94A3B8),
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = if (isIslandExpanded) "Нажмите на экран для сворачивания" else "Нажмите для анимации открытия",
                fontSize = 11.sp,
                color = if (dark) Color(0xFF71717A) else Color(0xFF94A3B8),
            )
        }
    }
}
