package com.example.catfacts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.example.catfacts.services.CatFactService
import com.example.catfacts.ui.theme.CatFactsTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: CatFactsViewModel
    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[CatFactsViewModel::class.java]

        // Setup broadcast receiver
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "CAT_FACT_RECEIVED") {
                    val fact = intent.getStringExtra("CAT_FACT")
                    fact?.let {
                        viewModel.addServiceFact(it)
                    }
                }
            }
        }

        setContent {
            CatFactsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CatFactsApp(viewModel)
                }
            }
        }

        // Register for Service broadcasts
        LocalBroadcastManager.getInstance(this).registerReceiver(
            broadcastReceiver,
            IntentFilter("CAT_FACT_RECEIVED")
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver)
    }
}

@Composable
fun CatFactsApp(viewModel: CatFactsViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "screen1") {
        composable("screen1") {
            ServiceScreen(
                viewModel = viewModel,
                onNavigateToWorker = { navController.navigate("screen2") }
            )
        }
        composable("screen2") {
            WorkerScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.navigateUp() }
            )
        }
    }
}

@Composable
fun ServiceScreen(viewModel: CatFactsViewModel, onNavigateToWorker: () -> Unit) {
    val serviceFacts by viewModel.serviceFacts.collectAsState()
    val loading by viewModel.serviceLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Service Cat Facts",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                viewModel.setServiceLoading(true)
                val serviceIntent = Intent(viewModel.context, CatFactService::class.java)
                viewModel.context.startService(serviceIntent)
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Get Cat Fact via Service")
        }

        Button(
            onClick = onNavigateToWorker,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Go to WorkManager Screen")
        }

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(serviceFacts.size) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = serviceFacts[index],
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun WorkerScreen(viewModel: CatFactsViewModel, onNavigateBack: () -> Unit) {
    val workerFacts by viewModel.workerFacts.collectAsState()
    val loading by viewModel.workerLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "WorkManager Cat Facts",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                viewModel.setWorkerLoading(true)
                // Create a OneTime work request
                val catFactWorkRequest = OneTimeWorkRequestBuilder<CatFactsWorker>()
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()

                // Enqueue the work
                WorkManager.getInstance(viewModel.context)
                    .enqueue(catFactWorkRequest)
            },
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Get Cat Fact via WorkManager")
        }

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Text("Go Back to Service Screen")
        }

        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(workerFacts.size) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = workerFacts[index],
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}