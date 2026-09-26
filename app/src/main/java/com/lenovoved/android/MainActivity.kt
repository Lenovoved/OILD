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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material.icons.filled.Tune
import com.lenovoved.android.ui.settings.CategoriesSettingsScreen
import com.lenovoved.android.ui.settings.ColorsThemeSettingsScreen
import com.lenovoved.android.ui.settings.DurationSettingsScreen
import com.lenovoved.android.ui.settings.IPhoneHalfSilhouette
import com.lenovoved.android.ui.settings.opticalMagnifier
import com.lenovoved.android.ui.settings.PermissionsSettingsScreen
import com.lenovoved.android.ui.settings.SettingsCard
import com.lenovoved.android.ui.settings.SettingsSectionHeader
import com.lenovoved.android.ui.settings.SettingsToggleRow
import com.lenovoved.android.ui.settings.SurfacesSettingsScreen
import com.lenovoved.android.ui.settings.TypographySettingsScreen
import com.lenovoved.android.ui.settings.WaveletSettingsScreen
import com.lenovoved.android.wavelet.WaveletAudioEngine
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
    WAVELET,
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        WaveletAudioEngine.initAudioEffects(this)

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
                    SettingsSubScreen.WAVELET -> WaveletSettingsScreen(onBack = { activeSubScreen = null })
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
    val mainHeaderTextColor = if (dark) Color.White else Color(0xFF1C1C1E)
    val cardContainerColor = if (dark) Color(0xFF1C1C1E) else Color.White
    val dividerColor = if (dark) Color(0xFF2C2C2E) else Color(0xFFF0F0F2)
    val modalGlassBg = if (dark) Color(0xFF1C1C1E).copy(alpha = 0.45f) else Color.White.copy(alpha = 0.45f)
    val modalBorderColor = if (dark) Color(0xFF2C2C2E) else Color(0xFFE5E5EA)
    val modalTitleColor = if (dark) Color.White else Color(0xFF1C1C1E)
    val modalSubtitleColor = if (dark) Color(0xFFD1D5DB) else Color(0xFF1F2937)
    val modalDevBoxBg = if (dark) Color(0x220066FF) else Color(0x0C0066FF)
    val modalDevTextColor = if (dark) Color(0xFF60A5FA) else Color(0xFF0066FF)
    val infoBtnBg = if (showInfoDialog) Color(0xFF0066FF) else (if (dark) Color(0xFF1F1F21) else Color(0xFFEBEBEF))
    val infoBtnTint = if (showInfoDialog) Color.White else Color(0xFF8E8E93)

    // Active categories state
    var normalOn by remember {
        mutableStateOf(prefs.getBoolean("cast_normal_notifications", false) || prefs.getBoolean("cast_notifications", false))
    }
    var messengerOn by remember {
        mutableStateOf(prefs.getBoolean("cast_messenger_notifications", false))
    }
    var navOn by remember {
        mutableStateOf(prefs.getBoolean("cast_nav_notifications", false))
    }
    var mediaOn by remember {
        mutableStateOf(prefs.getBoolean("cast_media_sessions", false))
    }
    var sportsOn by remember {
        mutableStateOf(prefs.getBoolean("cast_sports_enabled", false))
    }
    var paymentsOn by remember {
        mutableStateOf(prefs.getBoolean("cast_payments_enabled", false))
    }
    var ignoreSilent by remember {
        mutableStateOf(prefs.getBoolean("cast_ignore_silent", false))
    }

    LaunchedEffect(tick.intValue) {
        normalOn = prefs.getBoolean("cast_normal_notifications", false) || prefs.getBoolean("cast_notifications", false)
        messengerOn = prefs.getBoolean("cast_messenger_notifications", false)
        navOn = prefs.getBoolean("cast_nav_notifications", false)
        mediaOn = prefs.getBoolean("cast_media_sessions", false)
        sportsOn = prefs.getBoolean("cast_sports_enabled", false)
        paymentsOn = prefs.getBoolean("cast_payments_enabled", false)
        ignoreSilent = prefs.getBoolean("cast_ignore_silent", false)
    }

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

    val group1Items = remember {
        listOf(
            HubItem(
                icon = Icons.Default.Schedule,
                iconColor = Color(0xFF8B5CF6),
                title = "Время отображения",
                onClick = { onNavigateTo(SettingsSubScreen.DURATION) },
            ),
            HubItem(
                icon = Icons.Default.Layers,
                iconColor = Color(0xFF10B981),
                title = "Область уведомлений",
                onClick = { onNavigateTo(SettingsSubScreen.SURFACES) },
            ),
            HubItem(
                icon = Icons.Default.FormatPaint,
                iconColor = Color(0xFF0EA5E9),
                title = "Стиль и лимиты символов",
                onClick = { onNavigateTo(SettingsSubScreen.TYPOGRAPHY) },
            ),
        )
    }

    val group2Items = remember {
        listOf(
            HubItem(
                icon = Icons.Default.ColorLens,
                iconColor = Color(0xFFF59E0B),
                title = "Цвет времени уведомления",
                onClick = { onNavigateTo(SettingsSubScreen.COLORS_THEME) },
            ),
            HubItem(
                icon = Icons.Default.GraphicEq,
                iconColor = Color(0xFF0066FF),
                title = "Эквалайзер Wavelet",
                onClick = { onNavigateTo(SettingsSubScreen.WAVELET) },
            ),
            HubItem(
                icon = Icons.Default.FilterList,
                iconColor = Color(0xFF10B981),
                title = "Фильтр приложений",
                onClick = { showAppFilterSheet = true },
            ),
            HubItem(
                icon = Icons.Default.Security,
                iconColor = Color(0xFF0066FF),
                title = "Системные разрешения",
                onClick = { onNavigateTo(SettingsSubScreen.PERMISSIONS) },
            ),
        )
    }

    var dialogRect by remember { mutableStateOf<android.graphics.RectF?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Main screen background content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .opticalMagnifier(
                    enabled = showInfoDialog,
                    rect = dialogRect,
                    zoom = 1.25f
                )
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 16.dp),
        ) {
            // Header Row: Centered title "Пространство Origin", info icon button on right
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 20.dp, start = 4.dp, end = 4.dp),
            ) {
                Text(
                    text = "Пространство Origin",
                    fontSize = 22.sp,
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

            // Silhouette of top half of iPhone with animated Dynamic Island opening
            IPhoneHalfSilhouette(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp),
            )

            // Main Notification Sources Card
            SettingsCard {
                SettingsSectionHeader("Основные источники", Icons.Default.Notifications)

                SettingsToggleRow(
                    title = "Обычные уведомления",
                    subtitle = "Системные оповещения, загрузки, будильники, таймеры и установленные приложения",
                    checked = normalOn,
                    onCheckedChange = { checked ->
                        normalOn = checked
                        prefs.edit()
                            .putBoolean("cast_normal_notifications", checked)
                            .putBoolean("cast_notifications", checked)
                            .apply()
                        NotificationCastListener.onCategorySettingsChanged(context, "normal", checked)
                        if (checked) {
                            NotificationCastListener.forceRebind(context)
                            NotificationCastListener.instance?.recastAll()
                        }
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Уведомления мессенджеров",
                    subtitle = "Telegram, WhatsApp, Viber, VK, SMS, диалоги и чаты",
                    checked = messengerOn,
                    onCheckedChange = { checked ->
                        messengerOn = checked
                        prefs.edit().putBoolean("cast_messenger_notifications", checked).apply()
                        NotificationCastListener.onCategorySettingsChanged(context, "messenger", checked)
                        if (checked) {
                            NotificationCastListener.forceRebind(context)
                            NotificationCastListener.instance?.recastAll()
                        }
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Уведомления навигаторов",
                    subtitle = "Google Карты, Яндекс Навигатор, Яндекс Карты, 2ГИС, дорожные подсказки",
                    checked = navOn,
                    onCheckedChange = { checked ->
                        navOn = checked
                        prefs.edit().putBoolean("cast_nav_notifications", checked).apply()
                        NotificationCastListener.onCategorySettingsChanged(context, "navigation", checked)
                        if (checked) {
                            NotificationCastListener.forceRebind(context)
                            NotificationCastListener.instance?.recastAll()
                        }
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Уведомления плеером",
                    subtitle = "Медиаплеер OriginOS, обложки треков, управление воспроизведением (пауза, след/пред)",
                    checked = mediaOn,
                    onCheckedChange = { checked ->
                        mediaOn = checked
                        prefs.edit().putBoolean("cast_media_sessions", checked).apply()
                        NotificationCastListener.onMediaSettingsChanged(context, checked)
                        if (checked) {
                            NotificationCastListener.forceRebind(context)
                            NotificationCastListener.instance?.recastAll()
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Specialized Events Card
            SettingsCard {
                SettingsSectionHeader("Специализированные события", Icons.Default.Tune)

                SettingsToggleRow(
                    title = "Спортивные события и Live Score",
                    subtitle = "Счета футбольных матчей в реальном времени, эмблемы клубов и минуты (FotMob, SofaScore, FlashScore)",
                    checked = sportsOn,
                    onCheckedChange = { checked ->
                        sportsOn = checked
                        prefs.edit().putBoolean("cast_sports_enabled", checked).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Банковские чеки и платежи",
                    subtitle = "Карточка успешной оплаты в стиле Wallet/Apple Pay, билеты на самолеты и поезда",
                    checked = paymentsOn,
                    onCheckedChange = { checked ->
                        paymentsOn = checked
                        prefs.edit().putBoolean("cast_payments_enabled", checked).apply()
                        NotificationCastListener.instance?.reload()
                        NotificationCastListener.instance?.recastAll()
                    },
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = dividerColor, thickness = 0.8.dp)

                SettingsToggleRow(
                    title = "Игнорировать беззвучные уведомления",
                    subtitle = "Не показывать на островке события из каналов, где отключены звук и вибрация",
                    checked = ignoreSilent,
                    onCheckedChange = { checked ->
                        ignoreSilent = checked
                        prefs.edit().putBoolean("cast_ignore_silent", checked).apply()
                        NotificationCastListener.instance?.reload()
                    },
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Group 1
            SettingsCardGroup(
                items = group1Items,
                containerColor = cardContainerColor,
                dividerColor = dividerColor,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Group 2
            SettingsCardGroup(
                items = group2Items,
                containerColor = cardContainerColor,
                dividerColor = dividerColor,
            )

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
                    .onGloballyPositioned { coords ->
                        val pos = coords.positionInRoot()
                        dialogRect = android.graphics.RectF(
                            pos.x,
                            pos.y,
                            pos.x + coords.size.width,
                            pos.y + coords.size.height
                        )
                    }
                    .graphicsLayer {
                        scaleX = popoutProgress
                        scaleY = popoutProgress
                        alpha = popoutProgress.coerceIn(0f, 1f)
                        transformOrigin = TransformOrigin(0.92f, 0.0f)
                    }
                    .width(250.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp),
                        spotColor = Color(0x30000000),
                    )
                    .border(
                        width = 1.2.dp,
                        color = modalBorderColor,
                        shape = RoundedCornerShape(24.dp),
                    )
                    .clip(RoundedCornerShape(24.dp))
                    .background(modalGlassBg),
            ) {
                // Close button top-right
                IconButton(
                    onClick = { showInfoDialog = false },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        modifier = Modifier.size(16.dp),
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    // App Icon Badge
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .shadow(4.dp, RoundedCornerShape(14.dp), spotColor = Color(0x252563EB))
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Animation,
                            contentDescription = "Иконка приложения",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Title
                    Text(
                        text = "Пространство Origin",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = modalTitleColor,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Версия 1.0.1 • SuperX Build",
                        fontSize = 11.5.sp,
                        color = if (dark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // App Description
                    Text(
                        text = "Интеграция с динамическим островом OriginOS (OriginIsland) для трансляции уведомлений, мессенджеров и медиаплеера на смартфонах vivo.",
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = modalSubtitleColor,
                        textAlign = TextAlign.Center,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Developer Info Badge
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(modalDevBoxBg)
                            .padding(vertical = 7.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Разработчик: Lenovoved",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = modalDevTextColor,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = "Оптимизировано под vivo OriginOS",
                                fontSize = 10.5.sp,
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

private data class HubItem(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val onClick: () -> Unit,
)

@Composable
private fun SettingsCardGroup(
    items: List<HubItem>,
    containerColor: Color,
    dividerColor: Color,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            items.forEachIndexed { index, item ->
                SettingsHubRow(
                    icon = item.icon,
                    iconColor = item.iconColor,
                    title = item.title,
                    onClick = item.onClick,
                )
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 58.dp),
                        color = dividerColor,
                        thickness = 0.8.dp,
                    )
                }
            }
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
    val textColor = if (dark) Color.White else Color(0xFF1C1C1E)
    val arrowColor = if (dark) Color(0xFF636366) else Color(0xFFC7C7CC)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 18.dp, vertical = 15.dp),
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
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Открыть",
            tint = arrowColor,
            modifier = Modifier.size(20.dp),
        )
    }
}
