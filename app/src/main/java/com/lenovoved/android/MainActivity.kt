package com.lenovoved.android

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.magnifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.lenovoved.android.cards.SportsCard
import com.lenovoved.android.island.OriginIslandBuilder
import com.lenovoved.android.island.PlaygroundService
import com.lenovoved.android.service.NotificationCastListener
import com.lenovoved.android.ui.AppFilterSheet
import com.lenovoved.android.ui.OriginIsleTheme
import com.lenovoved.android.ui.PREFS_NAME
import com.lenovoved.android.ui.openAutoStartSettings
import com.lenovoved.android.ui.rememberResumeTick
import com.lenovoved.android.ui.samples.IslandSamples
import com.lenovoved.android.ui.settings.CategoriesSettingsScreen
import com.lenovoved.android.ui.settings.ColorsThemeSettingsScreen
import com.lenovoved.android.ui.settings.DurationSettingsScreen
import com.lenovoved.android.ui.settings.PermissionsSettingsScreen
import com.lenovoved.android.ui.settings.SurfacesSettingsScreen
import com.lenovoved.android.ui.settings.TypographySettingsScreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SettingsSubScreen {
    CATEGORIES,
    DURATION,
    SURFACES,
    TYPOGRAPHY,
    COLORS_THEME,
    PERMISSIONS,
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        when (intent?.getStringExtra("autofire")) {
            "football" -> SportsCard.post(this, "ARS", 1, "MAN", 1, "65'")
        }
        intent?.getIntExtra("autofire_sample", -1)?.takeIf { it >= 0 }?.let {
            IslandSamples.all.getOrNull(it)?.post(this)
        }

        setContent {
            OriginIsleTheme {
                OriginSpaceApp()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val hasAnyCasting = prefs.getBoolean("cast_media_sessions", false) ||
            prefs.getBoolean("cast_normal_notifications", false) ||
            prefs.getBoolean("cast_messenger_notifications", false) ||
            prefs.getBoolean("cast_nav_notifications", false) ||
            prefs.getBoolean("cast_sports_enabled", false) ||
            prefs.getBoolean("cast_payments_enabled", false) ||
            prefs.getBoolean("cast_notifications", false)

        if (hasAnyCasting) {
            NotificationCastListener.forceRebind(this)
        }
    }
}

@Composable
private fun OriginSpaceApp() {
    val context = LocalContext.current
    var activeSubScreen by remember { mutableStateOf<SettingsSubScreen?>(null) }

    // Grant Island scenes on launch
    remember { OriginIslandBuilder.grantScenes(context) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
        ) {
            AnimatedContent(
                targetState = activeSubScreen,
                transitionSpec = {
                    if (targetState != null) {
                        (slideInHorizontally(
                            animationSpec = tween(320, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> fullWidth },
                        ) + fadeIn(animationSpec = tween(300))).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(280, easing = FastOutSlowInEasing),
                                targetOffsetX = { fullWidth -> -fullWidth / 4 },
                            ) + fadeOut(animationSpec = tween(200)),
                        )
                    } else {
                        (slideInHorizontally(
                            animationSpec = tween(300, easing = FastOutSlowInEasing),
                            initialOffsetX = { fullWidth -> -fullWidth / 4 },
                        ) + fadeIn(animationSpec = tween(300))).togetherWith(
                            slideOutHorizontally(
                                animationSpec = tween(320, easing = FastOutSlowInEasing),
                                targetOffsetX = { fullWidth -> fullWidth },
                            ) + fadeOut(animationSpec = tween(200)),
                        )
                    }
                },
                label = "SubScreenTransition",
            ) { subScreen ->
                when (subScreen) {
                    SettingsSubScreen.CATEGORIES -> CategoriesSettingsScreen(onBack = { activeSubScreen = null })
                    SettingsSubScreen.DURATION -> DurationSettingsScreen(onBack = { activeSubScreen = null })
                    SettingsSubScreen.SURFACES -> SurfacesSettingsScreen(onBack = { activeSubScreen = null })
                    SettingsSubScreen.TYPOGRAPHY -> TypographySettingsScreen(onBack = { activeSubScreen = null })
                    SettingsSubScreen.COLORS_THEME -> ColorsThemeSettingsScreen(onBack = { activeSubScreen = null })
                    SettingsSubScreen.PERMISSIONS -> PermissionsSettingsScreen(onBack = { activeSubScreen = null })
                    null -> OriginSpaceMainHub(
                        onNavigateTo = { screen -> activeSubScreen = screen },
                    )
                }
            }
        }
    }
}

@Composable
private fun OriginSpaceMainHub(
    onNavigateTo: (SettingsSubScreen) -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    val tick = rememberResumeTick()

    var showInfoDialog by remember { mutableStateOf(false) }
    var showAppFilterSheet by remember { mutableStateOf(false) }
    var infoButtonCenter by remember { mutableStateOf(Offset.Unspecified) }

    // Spring animation for popout bounce
    val popoutProgress by animateFloatAsState(
        targetValue = if (showInfoDialog) 1f else 0f,
        animationSpec = spring(
            dampingRatio = 0.52f, // Spring bounce overshoot
            stiffness = Spring.StiffnessLow,
        ),
        label = "InfoPopoutSpring",
    )

    val dark = isSystemInDarkTheme()
    val mainHeaderTextColor = if (dark) Color(0xFFF8FAFC) else Color(0xFF1A1C1E)
    val cardContainerColor = if (dark) Color(0xFF1E293B) else Color.White
    val dividerColor = if (dark) Color(0xFF334155) else Color(0xFFF1F5F9)
    val modalGlassBg = if (dark) Color(0xFF1E293B).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.94f)
    val modalBorderColor = if (dark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val modalTitleColor = if (dark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val modalSubtitleColor = if (dark) Color(0xFFCBD5E1) else Color(0xFF334155)
    val modalDevBoxBg = if (dark) Color(0x223B82F6) else Color(0x0C2563EB)
    val modalDevTextColor = if (dark) Color(0xFF60A5FA) else Color(0xFF2563EB)
    val infoBtnBg = if (showInfoDialog) Color(0xFF2563EB) else (if (dark) Color(0xFF334155) else Color(0xFFE9ECF0))
    val infoBtnTint = if (showInfoDialog) Color.White else (if (dark) Color(0xFFF8FAFC) else Color(0xFF49454F))

    // Count active categories
    val normalOn = prefs.getBoolean("cast_normal_notifications", false) || prefs.getBoolean("cast_notifications", false)
    val messengerOn = prefs.getBoolean("cast_messenger_notifications", false)
    val navOn = prefs.getBoolean("cast_nav_notifications", false)
    val mediaOn = prefs.getBoolean("cast_media_sessions", false)
    val sportsOn = prefs.getBoolean("cast_sports_enabled", false)
    val paymentsOn = prefs.getBoolean("cast_payments_enabled", false)
    val activeCategoriesCount = listOf(normalOn, messengerOn, navOn, mediaOn, sportsOn, paymentsOn).count { it }

    val postNotifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { tick.intValue++ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 33) {
            val hasNotif = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (!hasNotif) {
                postNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (activeCategoriesCount > 0) {
            NotificationCastListener.forceRebind(context)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Main screen background content (remains normal size without whole-screen zoom)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 8.dp, start = 8.dp, end = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Пространство Origin",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = mainHeaderTextColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center),
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.CenterEnd)
                        .onGloballyPositioned { coordinates ->
                            infoButtonCenter = coordinates.positionInRoot() + Offset(coordinates.size.width / 2f, coordinates.size.height / 2f)
                        }
                        .clip(CircleShape)
                        .background(infoBtnBg)
                        .clickable { showInfoDialog = !showInfoDialog },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = "Информация",
                        tint = infoBtnTint,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Settings Navigation Menu Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 2.dp,
                        shape = RoundedCornerShape(28.dp),
                        spotColor = if (dark) Color(0x33000000) else Color(0x0F000000),
                    ),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = cardContainerColor),
            ) {
                Column {
                    // 1. Categories
                    SettingsHubRow(
                        icon = Icons.Default.Notifications,
                        iconColor = Color(0xFF2563EB),
                        title = "Категории уведомлений",
                        onClick = { onNavigateTo(SettingsSubScreen.CATEGORIES) },
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 58.dp),
                        color = dividerColor,
                        thickness = 0.8.dp,
                    )

                    // 2. Duration
                    SettingsHubRow(
                        icon = Icons.Default.Schedule,
                        iconColor = Color(0xFF8B5CF6),
                        title = "Время отображения",
                        onClick = { onNavigateTo(SettingsSubScreen.DURATION) },
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 58.dp),
                        color = dividerColor,
                        thickness = 0.8.dp,
                    )

                    // 3. Surfaces
                    SettingsHubRow(
                        icon = Icons.Default.Layers,
                        iconColor = Color(0xFF10B981),
                        title = "Поверхности отображения",
                        onClick = { onNavigateTo(SettingsSubScreen.SURFACES) },
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 58.dp),
                        color = dividerColor,
                        thickness = 0.8.dp,
                    )

                    // 4. Typography & Style
                    SettingsHubRow(
                        icon = Icons.Default.FormatPaint,
                        iconColor = Color(0xFF2563EB),
                        title = "Стиль и лимиты символов",
                        onClick = { onNavigateTo(SettingsSubScreen.TYPOGRAPHY) },
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 58.dp),
                        color = dividerColor,
                        thickness = 0.8.dp,
                    )

                    // 5. Colors & Theme
                    SettingsHubRow(
                        icon = Icons.Default.ColorLens,
                        iconColor = Color(0xFFF59E0B),
                        title = "Цветовая палитра и темы",
                        onClick = { onNavigateTo(SettingsSubScreen.COLORS_THEME) },
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 58.dp),
                        color = dividerColor,
                        thickness = 0.8.dp,
                    )

                    // 6. App Filter
                    SettingsHubRow(
                        icon = Icons.Default.FilterList,
                        iconColor = Color(0xFF10B981),
                        title = "Фильтр приложений",
                        onClick = { showAppFilterSheet = true },
                    )

                    HorizontalDivider(
                        modifier = Modifier.padding(start = 58.dp),
                        color = dividerColor,
                        thickness = 0.8.dp,
                    )

                    // 7. Permissions
                    SettingsHubRow(
                        icon = Icons.Default.Security,
                        iconColor = Color(0xFF0EA5E9),
                        title = "Системные разрешения",
                        onClick = { onNavigateTo(SettingsSubScreen.PERMISSIONS) },
                    )
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }

        // Anchored Bouncy Glass Modal Popout for Info
        if (popoutProgress > 0.01f) {
            // Transparent backdrop for tapping outside
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { showInfoDialog = false }
            )

            // Transparent glass popout window anchored to info button with spring bounce animation
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 44.dp, end = 12.dp)
                    .graphicsLayer {
                        scaleX = popoutProgress
                        scaleY = popoutProgress
                        alpha = popoutProgress.coerceIn(0f, 1f)
                        transformOrigin = TransformOrigin(0.92f, 0.0f)
                    }
                    .width(320.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(26.dp),
                        spotColor = Color(0x30000000),
                    )
                    .border(
                        width = 1.5.dp,
                        color = modalBorderColor,
                        shape = RoundedCornerShape(26.dp),
                    )
                    .clip(RoundedCornerShape(26.dp))
                    .background(modalGlassBg),
            ) {
                // Close button top-right
                IconButton(
                    onClick = { showInfoDialog = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // App Icon Badge
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color(0x252563EB))
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Animation,
                            contentDescription = "Иконка приложения",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Title
                    Text(
                        text = "Пространство Origin",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = modalTitleColor,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Версия 2.4 • SuperX Build",
                        fontSize = 12.sp,
                        color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // App Description
                    Text(
                        text = "Интеграция с динамическим островом OriginOS (OriginIsland) для трансляции уведомлений, мессенджеров и медиаплеера на смартфонах vivo.",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = modalSubtitleColor,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Developer Info Badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(modalDevBoxBg)
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Разработчик: Lenovoved",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = modalDevTextColor,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = "Оптимизировано под vivo OriginOS",
                                fontSize = 11.sp,
                                color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }

        if (showAppFilterSheet) {
            AppFilterSheet(
                context = context,
                prefs = prefs,
                onDismiss = { showAppFilterSheet = false },
            )
        }
    }
}

@Composable
private fun SettingsHubRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    onClick: () -> Unit,
) {
    val dark = isSystemInDarkTheme()
    val textColor = if (dark) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    val arrowColor = if (dark) Color(0xFF64748B) else Color(0xFFCBD5E1)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 17.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp),
            )

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = textColor,
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = "Открыть",
            tint = arrowColor,
            modifier = Modifier.size(15.dp),
        )
    }
}
