package com.lenovoved.android.ui.samples

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import com.lenovoved.android.MainActivity
import com.lenovoved.android.R
import com.lenovoved.android.cards.PaymentCard
import com.lenovoved.android.cards.SportsCard
import com.lenovoved.android.island.OriginIslandConstants
import com.lenovoved.android.island.PlaygroundService

/**
 * Каталог образцов карточек для проверки работы OriginIsland.
 */
object IslandSamples {

    val all: List<OriginSample> = listOf(
        OriginSample("⚽ Футбольный матч", "Табло · ARS 1 – 1 MAN · 65'") { ctx ->
            val ball = Icon.createWithResource(ctx, R.drawable.ic_soccer)
            SportsCard.post(
                ctx, home = "ARS", homeScore = 1, away = "MAN", awayScore = 1, clock = "65'",
                homeLogo = ball, awayLogo = ball,
            )
        },
        OriginSample("📥 Загрузка файла", "Карточка загрузки с круговым прогрессом · 75%") { ctx ->
            start(ctx, 40001) {
                putExtra("oi_scene", "NAVIGATION")
                putExtra("oi_template", OriginIslandConstants.TEMPLATE_PROGRESS_VISUAL)
                putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_PROGRESS)
                putExtra("title", "Загрузка файла")
                putExtra("text", "app-release.apk")
                putExtra("oi_left_content", "Загрузка")
                putExtra("show_progress", true)
                putExtra("progress", 75)
                putExtra("progress_max", 100)
                putExtra("status_chip_text", "75%")
                putExtra("icon_res", R.drawable.ic_alert)
            }
        },
        OriginSample("💳 Оплата", "Стиль оплаты: Обработка… → Оплачено ✓") { ctx ->
            PaymentCard.post(
                context = ctx, id = 40002, appIcon = null,
                amount = "450 ₽", merchant = "Кофейня", appLabel = "Mir Pay",
                clickResp = openApp(ctx),
            )
        },
        OriginSample("🍕 Доставка еды", "Курьер в пути · 12 мин") { ctx ->
            start(ctx, 40003) {
                putExtra("oi_scene", "NAVIGATION")
                putExtra("oi_template", OriginIslandConstants.TEMPLATE_BASE)
                putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_CAPSULE_TEXT)
                putExtra("title", "Курьер в пути")
                putExtra("text", "Заказ скоро будет доставлен")
                putExtra("oi_left_content", "Доставка")
                putExtra("status_chip_text", "12 мин")
                putExtra("icon_res", R.drawable.ic_alert)
            }
        },
        OriginSample("🚖 Вызов такси", "Водитель подъезжает · 3 мин") { ctx ->
            start(ctx, 40004) {
                putExtra("oi_scene", "NAVIGATION")
                putExtra("oi_template", OriginIslandConstants.TEMPLATE_BASE)
                putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_CAPSULE_TEXT)
                putExtra("title", "Водитель подъезжает")
                putExtra("text", "Toyota Camry · Белый · А123ВС")
                putExtra("oi_left_content", "Такси")
                putExtra("status_chip_text", "3 мин")
                putExtra("icon_res", R.drawable.ic_alert)
            }
        },
        OriginSample("✈️ Посадка на рейс", "Маршрут рейса: SVO → AER") { ctx ->
            start(ctx, 40005) {
                putExtra("oi_scene", "NAVIGATION")
                putExtra("oi_template", OriginIslandConstants.TEMPLATE_TEXT_SYMMETRY)
                putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_CAPSULE_TEXT)
                putExtra("title", "SU1122")
                putExtra("text", "Посадка")
                putExtra("oi_extra1", "SVO"); putExtra("oi_extra2", "12:40")
                putExtra("oi_extra3", "AER"); putExtra("oi_extra4", "16:15")
                putExtra("oi_left_content", "SVO → AER")
                putExtra("status_chip_text", "Выход 21")
                putExtra("icon_res", R.drawable.ic_alert)
            }
        },
        OriginSample("⏱️ Таймер", "Осталось 6:00 (прогресс 40%)") { ctx ->
            start(ctx, 40006) {
                putExtra("oi_scene", "NAVIGATION")
                putExtra("oi_template", OriginIslandConstants.TEMPLATE_PROGRESS_VISUAL)
                putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_PROGRESS)
                putExtra("title", "Таймер")
                putExtra("text", "Осталось 6:00")
                putExtra("oi_left_content", "Таймер")
                putExtra("show_progress", true)
                putExtra("progress", 40)
                putExtra("progress_max", 100)
                putExtra("status_chip_text", "6:00")
                putExtra("icon_res", R.drawable.ic_timer)
            }
        },
        OriginSample("⏳ Загрузка / Ожидание", "Анимация точек в островке") { ctx ->
            start(ctx, 40007) {
                putExtra("oi_scene", "NAVIGATION")
                putExtra("oi_template", OriginIslandConstants.TEMPLATE_BASE)
                putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_LOADING)
                putExtra("title", "Обработка…")
                putExtra("text", "Пожалуйста, подождите")
                putExtra("oi_left_content", "Работает")
                putExtra("icon_res", R.drawable.ic_alert)
            }
        },
        OriginSample("🎵 Музыкальный плеер", "Волна в островке · карточка медиа") { ctx ->
            start(ctx, 40008) {
                putExtra("oi_scene", "NAVIGATION")
                putExtra("oi_template", OriginIslandConstants.TEMPLATE_BASE)
                putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_WAVE)
                putExtra("title", "Midnight City")
                putExtra("text", "M83")
                putExtra("oi_left_content", "Midnight City")
                putExtra("oi_wave_state", 1)
                putExtra("status_chip_text", "♪")
                putExtra("icon_res", R.drawable.ic_media_play)
            }
        },
        OriginSample("📞 Входящий звонок", "Кнопки: Отклонить / Ответить") { ctx ->
            start(ctx, 40009) {
                putExtra("oi_scene", "NAVIGATION")
                putExtra("oi_template", OriginIslandConstants.TEMPLATE_BUTTONS)
                putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_ICON_TEXT)
                putExtra("title", "Входящий вызов")
                putExtra("text", "Алексей")
                putExtra("oi_left_content", "Алексей")
                putExtra("oi_right_content", "вызов")
                putStringArrayListExtra("oi_button_titles", arrayListOf("Отклонить", "Ответить"))
                putExtra("click_resp", openApp(ctx))
                putExtra("icon_res", R.drawable.ic_call)
            }
        },
        OriginSample("🧭 Навигация (маневр)", "Поворот через 350 м · время прибытия") { ctx ->
            start(ctx, 40011) {
                putExtra("oi_scene", "NAVIGATION")
                putExtra("oi_template", OriginIslandConstants.TEMPLATE_DRIVING_NAVI)
                putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_CAPSULE_TEXT)
                putExtra("title", "Через 350 м")
                putExtra("text", "Невский проспект")
                putExtra("oi_nav_msg", "12 мин · 4,5 км · 14:32")
                putExtra("oi_nav_assist", "Держитесь левее")
                putExtra("oi_left_content", "350 м")
                putExtra("status_chip_text", "12 мин")
                putExtra("icon_res", R.drawable.ic_navigation)
            }
        },
        OriginSample("🧭 Сообщение навигатора", "Поворот направо · 2 мин") { ctx ->
            start(ctx, 40012) {
                putExtra("oi_scene", "NAVIGATION")
                putExtra("oi_template", OriginIslandConstants.TEMPLATE_NAVIGATION)
                putExtra("oi_right_template", OriginIslandConstants.TEMPLATE_RIGHT_ISLAND_CAPSULE_TEXT)
                putExtra("title", "Поверните направо")
                putExtra("text", "на набережную")
                putExtra("oi_nav_msg", "Поверните направо на набережную · 2 мин")
                putExtra("oi_left_content", "Поворот")
                putExtra("status_chip_text", "2 мин")
                putExtra("icon_res", R.drawable.ic_navigation)
            }
        },
    )

    private fun start(context: Context, id: Int, configure: Intent.() -> Unit) {
        val intent = Intent(context, PlaygroundService::class.java).apply {
            action = PlaygroundService.ACTION_START
            putExtra("id", id)
            putExtra("click_resp", openApp(context))
            putExtra("oi_force_show", true)
            putExtra("oi_dismiss_when_kill", true)
            configure()
        }
        context.startService(intent)
    }

    private fun openApp(context: Context): PendingIntent = PendingIntent.getActivity(
        context, 0, Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )
}
