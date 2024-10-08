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

    fun updateCar(
        carId: String,
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
                val success = repository.updateCar(
                    carId,
                    manufacturer,
                    model,
                    year,
                    mileage,
                    condition,
                    description,
                    price,
                    imageUris
                )
                if (success) {
                    _error.value = "Car updated successfully"
                    loadCarById(carId)
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

    fun deleteCar(carId: String) {
        viewModelScope.launch {
            _loading.value = true
            try {
                repository.deleteCar(carId)
                _error.value = "Car deleted successfully"
            } catch (e: Exception) {
                _error.value = "Failed to delete car: ${e.message}"
            }
            _loading.value = false
        }
    }

    fun isCurrentUserAuthor(authorId: String): Boolean {
        val currentUserId = auth.currentUser?.uid
        return currentUserId == authorId
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun filterCars(manufacturer: String, minPrice: Double?, maxPrice: Double?) {
        viewModelScope.launch {
            _loading.value = true
            try {
                var filteredCars = repository.getCars()

                if (manufacturer != "All") {
                    filteredCars = filteredCars.filter { it.manufacturer == manufacturer }
                }

                if (minPrice != null) {
                    filteredCars = filteredCars.filter { it.price >= minPrice }
                }

                if (maxPrice != null) {
                    filteredCars = filteredCars.filter { it.price <= maxPrice }
                }

                _cars.value = filteredCars
            } catch (e: Exception) {
                _error.value = "Failed to filter cars: ${e.message}"
            }
            _loading.value = false
        }
    }

    fun filterCarsByUser(userId: String?) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val allCars = repository.getCars()

                val userCars = allCars.filter { it.ownerId == userId }

                _cars.value = userCars
            } catch (e: Exception) {
                _error.value = "Failed to filter cars by user: ${e.message}"
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