package com.glassywidgets.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.glassywidgets.data.Repository
import com.glassywidgets.data.WeatherApi
import com.glassywidgets.data.WeatherData
import com.glassywidgets.widget.WidgetUpdater
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

object WeatherSync {
    /** Descarga el clima, lo guarda y refresca todos los widgets. */
    suspend fun sync(context: Context): WeatherData {
        val repo = Repository.get(context)
        val s = repo.settings.first()
        val w = WeatherApi.fetch(s.lat, s.lon, s.fahrenheit, s.cityName)
        repo.saveWeather(w)
        WidgetUpdater.updateAll(context)
        return w
    }
}

class WeatherWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = try {
        WeatherSync.sync(applicationContext)
        Result.success()
    } catch (e: Exception) {
        if (runAttemptCount < 3) Result.retry() else Result.failure()
    }

    companion object {
        private val network = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WeatherWorker>(30, TimeUnit.MINUTES)
                .setConstraints(network)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork("weather_periodic", ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun runNow(context: Context) {
            val request = OneTimeWorkRequestBuilder<WeatherWorker>().setConstraints(network).build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork("weather_now", ExistingWorkPolicy.REPLACE, request)
        }
    }
}

/** Redibuja los widgets cada 15 min (saludo, progreso del día, batería). */
class WidgetTickWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        WidgetUpdater.updateAll(applicationContext)
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetTickWorker>(15, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork("widgets_tick", ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
