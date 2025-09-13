package com.example.arzones

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LocationWorker(appContext: android.content.Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.i("LocationWorker", "▶️ Début d'une nouvelle exécution du Worker")

        // 🔒 Vérifier la permission avant d’accéder au GPS
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("LocationWorker", "❌ Permission localisation refusée")
            return Result.retry()
        }

        val fused = LocationServices.getFusedLocationProviderClient(applicationContext)

        return try {
            // ✅ Essayer de récupérer une localisation fraîche
            val freshLocation = fused.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            ).await()

            var latitude = freshLocation?.latitude
            var longitude = freshLocation?.longitude

            if (latitude == null || longitude == null || (latitude == 0.0 && longitude == 0.0)) {
                Log.w("LocationWorker", "⚠️ Localisation fraîche indisponible, tentative avec la dernière connue")
                val lastLocation = fused.lastLocation.await()
                latitude = lastLocation?.latitude
                longitude = lastLocation?.longitude
            }

            // Log pour debug
            Log.i("LocationWorker", "📍 Position obtenue : $latitude , $longitude")

            val deviceModel = "${Build.MANUFACTURER} ${Build.MODEL}"

            // Envoi des données
            sendToJsonBin(latitude, longitude, deviceModel)
            Result.success()
        } catch (e: Exception) {
            Log.e("LocationWorker", "❌ Erreur lors de la récupération de la localisation", e)
            Result.retry()
        }
    }

    private fun sendToJsonBin(lat: Double?, lon: Double?, device: String) {
        val client = OkHttpClient()
        val data = JSONObject().apply {
            put("device", device)
            put("latitude", lat ?: JSONObject.NULL)
            put("longitude", lon ?: JSONObject.NULL)
            put("timestamp", System.currentTimeMillis())
        }

        val body = data.toString().toRequestBody("application/json".toMediaType())

        val req = Request.Builder()
            .url("https://api.jsonbin.io/v3/b/68b22ed043b1c97be92fc808")   // <-- Ton Bin ID
            .addHeader("X-Master-Key", "\$2a\$10\$BOkgsvsfuUZHFXRDmlftwO2z1eXV/4lFloHWPo2Yg82zbY3Dp5GES") // <-- Ta clé
            .addHeader("Content-Type", "application/json")
            .put(body)
            .build()

        client.newCall(req).execute().use { res ->
            Log.i("LocationWorker", "📤 Données envoyées à JSONBin -> HTTP ${res.code}")
        }
    }
}
