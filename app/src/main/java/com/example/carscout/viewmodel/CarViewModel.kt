package com.example.carscout.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import com.example.carscout.data.model.Car
import com.example.carscout.data.repository.CarRepository
import com.google.firebase.auth.FirebaseAuth
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

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Load all cars
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

    // Load car by its ID
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

    // Add a car with all required fields
    fun addCar(
        manufacturer: String,
        model: String,
        year: Int,
        mileage: Int,
        condition: String,
        description: String,
        price: Double,
        imageUris: List<Uri>
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val carId = repository.addCar(
                    manufacturer,
                    model,
                    year,
                    mileage,
                    condition,
                    description,
                    price,
                    imageUris
                )
                _error.value = "Car added successfully with ID: $carId"
            } catch (e: Exception) {
                _error.value = "Failed to add car: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Update an existing car
    fun updateCar(
        carId: String,
        manufacturer: String,
        model: String,
        year: Int,
        mileage: Int,
        condition: String,
        description: String,
        price: Double
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val success = repository.updateCar(
                    carId,
                    manufacturer,
                    model,
                    year,
                    mileage,
                    condition,
                    description,
                    price
                )
                if (success) {
                    _error.value = "Car updated successfully"
                    loadCarById(carId)  // Reload the car to get updated details
                } else {
                    _error.value = "Failed to update car"
                }
            } catch (e: Exception) {
                _error.value = "Failed to update car: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Check if the current user is the author of the car listing
    fun isCurrentUserAuthor(authorId: String): Boolean {
        val currentUserId = auth.currentUser?.uid
        return currentUserId == authorId
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