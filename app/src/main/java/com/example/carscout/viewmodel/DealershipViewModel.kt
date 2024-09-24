package com.example.carscout.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carscout.data.model.Dealership
import com.example.carscout.data.repository.DealershipRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class DealershipViewModel(private val repository: DealershipRepository) : ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _dealerships = MutableLiveData<List<Dealership>>()
    val dealerships: LiveData<List<Dealership>> = _dealerships

    private val _currentDealership = MutableLiveData<Dealership?>()
    val currentDealership: LiveData<Dealership?> = _currentDealership

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun loadDealerships() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _dealerships.value = repository.getDealerships()
            } catch (e: Exception) {
                _error.value = "Failed to load dealerships: ${e.message}"
            }
            _loading.value = false
        }
    }

    fun addDealership(name: String, address: String, phoneNumber: String, email: String, imageUris: List<Uri>) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val dealershipId = repository.addDealership(name, address, phoneNumber, email, imageUris)
                _error.value = "Dealership added successfully with ID: $dealershipId"
            } catch (e: Exception) {
                _error.value = "Failed to add dealership: ${e.message}"
            }
            _loading.value = false
        }
    }

    fun updateDealership(dealershipId: String, name: String, address: String, phoneNumber: String, email: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.editDealership(dealershipId, name, address, phoneNumber, email)
                _error.value = "Dealership updated successfully"
            } catch (e: Exception) {
                _error.value = "Failed to update dealership: ${e.message}"
            }
            _loading.value = false
        }
    }

    fun loadDealershipById(dealershipId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _currentDealership.value = repository.getDealershipById(dealershipId)
            } catch (e: Exception) {
                _error.value = "Failed to load dealership details: ${e.message}"
                //print the error
                e.printStackTrace()
            }
            _loading.value = false
        }
    }

    fun isCurrentUserAuthor(authorId: String): Boolean {
        val currentUserId = auth.currentUser?.uid
        return currentUserId == authorId
    }
}

class DealershipViewModelFactory(private val repository: DealershipRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DealershipViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DealershipViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
