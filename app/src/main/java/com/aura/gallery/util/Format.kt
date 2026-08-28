package com.aura.gallery.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Format {
    private val full = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
    private val timeFmt = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dayFmt = SimpleDateFormat("EEEE", Locale.getDefault())

    fun date(ms: Long): String = full.format(Date(ms))
    fun time(ms: Long): String = timeFmt.format(Date(ms))
    fun weekday(ms: Long): String = dayFmt.format(Date(ms))

    fun duration(ms: Long): String {
        val totalSec = ms / 1000
        return "%d:%02d".format(totalSec / 60, totalSec % 60)
    }
}
