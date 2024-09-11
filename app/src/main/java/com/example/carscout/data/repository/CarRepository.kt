package com.example.carscout.data.repository

import android.net.Uri
import com.example.carscout.data.model.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class CarRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Fetch all cars from the listings
    suspend fun getCars(): List<Car> {
        return firestore.collection("listings").get().await().toObjects(Car::class.java)
    }

    // Fetch a specific car by its ID
    suspend fun getCarById(carId: String): Car? {
        return firestore.collection("listings").document(carId).get().await().toObject(Car::class.java)
    }

    // Add a new car listing to Firestore
    suspend fun addCar(
        manufacturer: String,
        model: String,
        year: Int,
        mileage: Int,
        condition: String,
        description: String,
        price: Double,
        imageUris: List<Uri>
    ): String {
        val currentUser = auth.currentUser ?: throw IllegalStateException("User must be logged in")

        // Upload the images to Firebase Storage and get their URLs
        val imageUrls = uploadImages(imageUris)

        // Create a new Car object with all required fields
        val car = Car(
            manufacturer = manufacturer,
            model = model,
            year = year,
            mileage = mileage,
            condition = condition,
            description = description,
            price = price,
            imageUrls = imageUrls,
            ownerId = currentUser.uid,
            createdAt = System.currentTimeMillis()
        )

        // Add the car object to Firestore
        return firestore.collection("listings").add(car).await().id
    }

    // Update a car listing in Firestore
    suspend fun updateCar(
        carId: String,
        manufacturer: String,
        model: String,
        year: Int,
        mileage: Int,
        condition: String,
        description: String,
        price: Double
    ): Boolean {
        return try {
            firestore.collection("listings").document(carId)
                .update(
                    mapOf(
                        "manufacturer" to manufacturer,
                        "model" to model,
                        "year" to year,
                        "mileage" to mileage,
                        "condition" to condition,
                        "description" to description,
                        "price" to price
                    )
                ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Helper function to upload images to Firebase Storage
    private suspend fun uploadImages(imageUris: List<Uri>): List<String> {
        val currentUser = auth.currentUser ?: throw IllegalStateException("User must be logged in")
        return imageUris.mapIndexed { index, uri ->
            val imageRef = storage.reference.child("car_images/${currentUser.uid}/${System.currentTimeMillis()}_${index}.jpg")
            imageRef.putFile(uri).await()
            imageRef.downloadUrl.await().toString()
        }
    }
}
