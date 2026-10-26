package com.glassywidgets.data

import android.app.AlarmManager
import android.content.Context
import android.os.BatteryManager

/** Datos del teléfono que pueden mostrar los widgets. */
data class SystemInfo(val battery: Int, val charging: Boolean, val nextAlarm: Long?) {
    companion object {
        val PREVIEW = SystemInfo(battery = 76, charging = false, nextAlarm = null)

        fun read(context: Context): SystemInfo {
            val bm = context.getSystemService(BatteryManager::class.java)
            val level = runCatching { bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) }.getOrNull() ?: -1
            val charging = runCatching { bm?.isCharging }.getOrNull() ?: false
            val am = context.getSystemService(AlarmManager::class.java)
            val alarm = runCatching { am?.nextAlarmClock?.triggerTime }.getOrNull()
            return SystemInfo(if (level in 0..100) level else 0, charging, alarm)
        }
    }
}
