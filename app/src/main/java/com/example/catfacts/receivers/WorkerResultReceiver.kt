package com.example.catfacts.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.catfacts.CatFactsViewModel

class WorkerResultReceiver(private val viewModel: CatFactsViewModel) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "CAT_FACT_WORKER_RESULT") {
            val fact = intent.getStringExtra("CAT_FACT")
            fact?.let {
                viewModel.addWorkerFact(it)
            }
        }
    }
}