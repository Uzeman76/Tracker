package com.example.arzones

import android.app.Application
import android.util.Log
import androidx.work.*

import java.util.concurrent.TimeUnit

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.i("MyApp", "🚀 Application démarrée, planification des Workers")

        val workManager = WorkManager.getInstance(this)

        // ✅ Exécution immédiate une seule fois au lancement
        val oneTimeWork = OneTimeWorkRequestBuilder<LocationWorker>().build()
        workManager.enqueueUniqueWork(
            "ImmediateLocationWork",
            ExistingWorkPolicy.REPLACE,
            oneTimeWork
        )

        // ✅ Exécution répétée toutes les 15 minutes
        val periodicWork = PeriodicWorkRequestBuilder<LocationWorker>(
            15, TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "LocationWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWork
        )
    }
}
