package com.example.arzones

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class RestartReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val workRequest = PeriodicWorkRequestBuilder<LocationWorker>(
            10, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "LocationWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }
}
