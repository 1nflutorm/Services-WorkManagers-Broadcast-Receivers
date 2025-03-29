package com.example.catfacts

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CatFactsViewModel(application: Application) : AndroidViewModel(application) {
    val context: Context = application.applicationContext

    private val _serviceFacts = MutableStateFlow<List<String>>(emptyList())
    val serviceFacts: StateFlow<List<String>> = _serviceFacts

    private val _workerFacts = MutableStateFlow<List<String>>(emptyList())
    val workerFacts: StateFlow<List<String>> = _workerFacts

    private val _serviceLoading = MutableStateFlow(false)
    val serviceLoading: StateFlow<Boolean> = _serviceLoading

    private val _workerLoading = MutableStateFlow(false)
    val workerLoading: StateFlow<Boolean> = _workerLoading

    fun addServiceFact(fact: String) {
        _serviceFacts.value = _serviceFacts.value + fact
        _serviceLoading.value = false
    }

    fun addWorkerFact(fact: String) {
        _workerFacts.value = _workerFacts.value + fact
        _workerLoading.value = false
    }

    fun setServiceLoading(isLoading: Boolean) {
        _serviceLoading.value = isLoading
    }

    fun setWorkerLoading(isLoading: Boolean) {
        _workerLoading.value = isLoading
    }
}