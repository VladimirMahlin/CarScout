package com.example.carscout.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.carscout.data.repository.DealershipRepository
import kotlinx.coroutines.launch

class DealershipViewModel(private val repository: DealershipRepository) : ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun addDealership(name: String, address: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val dealershipId = repository.addDealership(name, address)
                _error.value = "Dealership added successfully with ID: $dealershipId"
            } catch (e: Exception) {
                _error.value = "Failed to add dealership: ${e.message}"
            }
            _loading.value = false
        }
    }
}
