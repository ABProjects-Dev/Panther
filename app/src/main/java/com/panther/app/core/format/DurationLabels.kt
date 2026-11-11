package com.panther.app.core.format

import android.content.Context
import com.panther.app.R
import com.panther.app.domain.model.DurationOption

/**
 * Localized label for a duration target, built from `strings.xml` so the wording stays in one place
 * ([UI-03]). Numbers are rendered with Persian digits and the `mm:ss` key is shown in parentheses.
 */
fun durationLabel(context: Context, option: DurationOption): String {
    val compact = PantherDateFormat.persianDigits(option.key.padStart(5, '0'))
    return when {
        option.minutes == 0 -> context.getString(
            R.string.duration_label_seconds,
            PantherDateFormat.persianNumber(option.seconds),
            compact,
        )
        option.seconds == 0 -> context.getString(
            R.string.duration_label_minutes,
            PantherDateFormat.persianNumber(option.minutes),
            compact,
        )
        else -> context.getString(
            R.string.duration_label_minutes_seconds,
            PantherDateFormat.persianNumber(option.minutes),
            PantherDateFormat.persianNumber(option.seconds),
            compact,
        )
    }
}

/** Resolver form, safe to capture inside `remember` blocks. */
fun durationLabelResolver(context: Context): (DurationOption) -> String =
    { option -> durationLabel(context, option) }
