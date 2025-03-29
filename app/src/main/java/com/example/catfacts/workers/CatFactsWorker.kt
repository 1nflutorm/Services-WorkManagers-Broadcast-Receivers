package com.example.catfacts.workers

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class CatFactsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
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
                val intent = Intent("CAT_FACT_WORKER_RESULT")
                intent.putExtra("CAT_FACT", fact)
                applicationContext.sendBroadcast(intent)

                return@withContext Result.success()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Broadcast error
            val intent = Intent("CAT_FACT_WORKER_RESULT")
            intent.putExtra("CAT_FACT", "Error fetching fact: ${e.message}")
            applicationContext.sendBroadcast(intent)

            return@withContext Result.failure()
        }
    }
}