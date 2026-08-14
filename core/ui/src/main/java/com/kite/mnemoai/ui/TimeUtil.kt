package com.kite.mnemoai.ui

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
public fun formatDuration(value: Float): String {
    val longValue = value.toLong()
    val duration = longValue.milliseconds
    return duration.toComponents { hours, minutes, seconds, _ ->
        buildList {
            if (hours > 0) add("${hours}小时")
            if (minutes > 0) add("${minutes}分钟")
            else {
                if (seconds > 0 || isEmpty()) add("${seconds}秒")
            }
        }.joinToString("")
    }
}