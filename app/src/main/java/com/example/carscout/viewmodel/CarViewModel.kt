package com.example.carscout.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.carscout.data.model.Car
import com.example.carscout.data.repository.CarRepository
import kotlinx.coroutines.launch

class CarViewModel(private val repository: CarRepository) : ViewModel() {
    private val _cars = MutableLiveData<List<Car>>()
    val cars: LiveData<List<Car>> = _cars

    private val _currentCar = MutableLiveData<Car>()
    val currentCar: LiveData<Car> = _currentCar

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadCars() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _cars.value = repository.getCars()
            } catch (e: Exception) {
                _error.value = "Failed to load cars: ${e.message}"
            }
            _loading.value = false
        }
    }

    fun loadCarById(carId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                _currentCar.value = repository.getCarById(carId)
            } catch (e: Exception) {
                _error.value = "Failed to load car details: ${e.message}"
            }
            _loading.value = false
        }
    }

    fun addCar(model: String, year: Int, price: Double, imageUris: List<Uri>) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val carId = repository.addCar(model, year, price, imageUris)
                _error.value = "Car added successfully with ID: $carId"
            } catch (e: Exception) {
                _error.value = "Failed to add car: ${e.message}"
            }
            _loading.value = false
        }
    }

    fun updateCar(carId: String, model: String, year: Int, price: Double) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val success = repository.updateCar(carId, model, year, price)
                if (success) {
                    _error.value = "Car updated successfully"
                    loadCarById(carId)
                } else {
                    _error.value = "Failed to update car"
                }
            } catch (e: Exception) {
                _error.value = "Failed to update car: ${e.message}"
            }
            _loading.value = false
        }
    }
}

class CarViewModelFactory(private val repository: CarRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}