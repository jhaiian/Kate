package com.jhaiian.kate.ui.stats

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.jhaiian.kate.R
import com.jhaiian.kate.data.ListeningStats
import com.jhaiian.kate.data.StatsPeriod
import com.jhaiian.kate.data.StatsRange
import com.jhaiian.kate.ui.theme.ClintColors
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

internal fun minutesOf(ms: Long): Long = ms / 60_000L

internal fun formatNumber(value: Long): String = NumberFormat.getIntegerInstance(Locale.getDefault()).format(value)

@Composable
internal fun durationText(ms: Long): String {
    val totalMinutes = minutesOf(ms)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) {
        stringResource(R.string.stats_duration_hours_minutes, hours.toInt(), minutes.toInt())
    } else {
        stringResource(R.string.stats_duration_minutes, minutes.toInt())
    }
}

internal fun Long.toPluralCount(): Int = coerceAtMost(Int.MAX_VALUE.toLong()).toInt()

private fun LocalDate.millis(): Long = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

@Composable
internal fun periodLabel(period: StatsPeriod): String {
    val context = LocalContext.current
    if (period.range == StatsRange.ALL_TIME) return stringResource(R.string.stats_all_time)
    val today = LocalDate.now()
    val current = StatsPeriod.containing(period.range, today)
    val last = current.previous()
    return when {
        period == current -> stringResource(
            when (period.range) {
                StatsRange.WEEK -> R.string.stats_this_week
                StatsRange.MONTH -> R.string.stats_this_month
                StatsRange.YEAR, StatsRange.ALL_TIME -> R.string.stats_this_year
            }
        )
        period == last -> stringResource(
            when (period.range) {
                StatsRange.WEEK -> R.string.stats_last_week
                StatsRange.MONTH -> R.string.stats_last_month
                StatsRange.YEAR, StatsRange.ALL_TIME -> R.string.stats_last_year
            }
        )
        else -> periodDates(context, period)
    }
}

internal fun periodDates(context: Context, period: StatsPeriod): String {
    val sameYear = period.start.year == LocalDate.now().year
    return when (period.range) {
        StatsRange.WEEK -> {
            var flags = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH
            flags = flags or if (sameYear) DateUtils.FORMAT_NO_YEAR else DateUtils.FORMAT_SHOW_YEAR
            DateUtils.formatDateRange(context, period.start.millis(), period.end.minusDays(1).millis() + 1, flags)
        }
        StatsRange.MONTH -> DateUtils.formatDateTime(
            context,
            period.start.millis(),
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_MONTH_DAY or DateUtils.FORMAT_SHOW_YEAR
        )
        StatsRange.YEAR -> period.start.year.toString()
        StatsRange.ALL_TIME -> context.getString(R.string.stats_since, monthYear(context, period.start))
    }
}

internal fun posterDates(context: Context, period: StatsPeriod): String = when (period.range) {
    StatsRange.WEEK -> DateUtils.formatDateRange(
        context,
        period.start.millis(),
        period.end.minusDays(1).millis() + 1,
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_SHOW_YEAR
    )
    else -> periodDates(context, period)
}

internal fun shortDayLabel(context: Context, date: LocalDate): String =
    DateUtils.formatDateTime(
        context,
        date.millis(),
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_NO_YEAR
    )

private fun monthYear(context: Context, date: LocalDate): String =
    DateUtils.formatDateTime(
        context,
        date.millis(),
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_NO_MONTH_DAY or DateUtils.FORMAT_SHOW_YEAR
    )

internal fun bucketShortLabel(period: StatsPeriod, bucket: Int): String {
    val date = period.bucketStart(bucket)
    return when (period.range) {
        StatsRange.WEEK -> date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
        StatsRange.MONTH -> date.dayOfMonth.toString()
        StatsRange.YEAR -> date.month.getDisplayName(TextStyle.NARROW, Locale.getDefault())
        StatsRange.ALL_TIME -> if (period.groupsByYear) {
            date.year.toString()
        } else {
            date.month.getDisplayName(TextStyle.NARROW, Locale.getDefault())
        }
    }
}

internal fun bucketLongLabel(context: Context, period: StatsPeriod, bucket: Int): String {
    val date = period.bucketStart(bucket)
    return when (period.range) {
        StatsRange.WEEK, StatsRange.MONTH -> DateUtils.formatDateTime(
            context,
            date.millis(),
            DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_ABBREV_ALL or DateUtils.FORMAT_NO_YEAR
        )
        StatsRange.YEAR -> date.month.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault())
        StatsRange.ALL_TIME -> if (period.groupsByYear) date.year.toString() else monthYear(context, date)
    }
}

internal fun unlockDate(context: Context, date: LocalDate): String {
    var flags = DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_ABBREV_MONTH or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_ABBREV_WEEKDAY
    flags = flags or if (date.year == LocalDate.now().year) DateUtils.FORMAT_NO_YEAR else DateUtils.FORMAT_SHOW_YEAR
    return DateUtils.formatDateTime(context, date.millis(), flags)
}

internal fun dayLabel(context: Context, date: LocalDate): String =
    DateUtils.formatDateTime(
        context,
        date.millis(),
        DateUtils.FORMAT_SHOW_DATE or DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_NO_YEAR
    )

internal fun hourLabel(context: Context, hour: Int): String {
    val millis = LocalDate.now().atTime(hour, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    return DateUtils.formatDateTime(context, millis, DateUtils.FORMAT_SHOW_TIME or DateUtils.FORMAT_ABBREV_TIME)
}

internal fun personaRes(hour: Int): Int = when (hour) {
    in 5..11 -> R.string.stats_persona_morning
    in 12..16 -> R.string.stats_persona_afternoon
    in 17..20 -> R.string.stats_persona_evening
    else -> R.string.stats_persona_night
}

@Composable
internal fun changeText(stats: ListeningStats): String? {
    val change = stats.changePercent ?: return null
    return when {
        change >= 3 -> stringResource(R.string.stats_change_more, change)
        change <= -3 -> stringResource(R.string.stats_change_less, -change)
        else -> stringResource(R.string.stats_change_same)
    }
}

internal fun wrappedCardTitleRes(range: StatsRange): Int = when (range) {
    StatsRange.WEEK -> R.string.wrapped_card_title_week
    StatsRange.MONTH -> R.string.wrapped_card_title_month
    StatsRange.YEAR, StatsRange.ALL_TIME -> R.string.wrapped_card_title_year
}

internal fun wrappedGradient(colors: ClintColors, page: Int): List<Color> {
    val strength = listOf(0.34f, 0.26f, 0.42f, 0.30f)[page % 4] * if (colors.isLight) 0.7f else 1f
    return listOf(
        lerp(colors.background, colors.primary, strength),
        lerp(colors.background, colors.primary, strength * 0.45f),
        colors.background
    )
}

internal fun primaryShades(colors: ClintColors, count: Int): List<Color> =
    List(count) { index -> lerp(colors.primary, colors.cardBackground, (index * 0.15f).coerceAtMost(0.8f)) }
