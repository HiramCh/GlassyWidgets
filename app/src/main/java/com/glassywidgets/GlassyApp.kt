package com.glassywidgets

import android.app.Application
import com.glassywidgets.work.WeatherWorker
import com.glassywidgets.work.WidgetTickWorker

class GlassyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WeatherWorker.schedule(this)
        WidgetTickWorker.schedule(this)
    }
}
