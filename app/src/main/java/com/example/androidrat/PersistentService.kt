package com.example.androidrat

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class PersistentService : Service() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("PersistentService", "Service rodando em background!")

        // WorkManager já cuida das tarefas periódicas
        // Aqui só mantém vivo se necessário

        return START_STICKY // Reinicia se morto
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
