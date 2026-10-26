package com.glassywidgets

import android.app.Application
import com.glassywidgets.work.WeatherWorker

class GlassyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WeatherWorker.schedule(this)
    }
}
