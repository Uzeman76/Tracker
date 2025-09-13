package com.example.arzones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i("BootReceiver", "📌 Téléphone redémarré → redémarrage du Worker")

            val workRequest = PeriodicWorkRequestBuilder<LocationWorker>(
                15, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "LocationWork",
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }
}
