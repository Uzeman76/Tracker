package com.example.arzones

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat

class PermissionActivity : ComponentActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            var granted = true
            permissions.entries.forEach {
                Log.i("PermissionActivity", "Permission ${it.key} = ${it.value}")
                if (!it.value) granted = false
            }

            if (granted) {
                Log.i("PermissionActivity", "✅ Toutes les permissions accordées")
            } else {
                Log.w("PermissionActivity", "⚠️ Permissions manquantes, l'app risque de ne pas fonctionner")
            }

            // Fermer l'activité une fois terminé
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // ✅ Ajout spécial pour Android 10+ : permission en arrière-plan
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }

        // Vérifier si déjà accordées
        val missing = permissions.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            Log.i("PermissionActivity", "📌 Demande des permissions : $missing")
            requestPermissionLauncher.launch(missing.toTypedArray())
        } else {
            Log.i("PermissionActivity", "✅ Permissions déjà accordées")
            finish()
        }
    }
}