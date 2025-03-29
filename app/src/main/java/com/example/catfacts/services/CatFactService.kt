package com.example.catfacts.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class CatFactService : Service() {
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        fetchCatFact()
        return START_NOT_STICKY
    }

    private fun fetchCatFact() {
        serviceScope.launch {
            try {
                delay(2000)

                val client = OkHttpClient()
                val request = Request.Builder()
                    .url("https://catfact.ninja/fact")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val responseBody = response.body?.string()
                    val jsonObject = JSONObject(responseBody)
                    val fact = jsonObject.getString("fact")

                    // Send broadcast with the fetched fact
                    val broadcastIntent = Intent("CAT_FACT_RECEIVED")
                    broadcastIntent.putExtra("CAT_FACT", fact)
                    LocalBroadcastManager.getInstance(applicationContext)
                        .sendBroadcast(broadcastIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Broadcast error
                val broadcastIntent = Intent("CAT_FACT_RECEIVED")
                broadcastIntent.putExtra("CAT_FACT", "Error fetching fact: ${e.message}")
                LocalBroadcastManager.getInstance(applicationContext)
                    .sendBroadcast(broadcastIntent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}