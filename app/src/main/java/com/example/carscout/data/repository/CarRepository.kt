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

    suspend fun getCars(): List<Car> {
        return firestore.collection("listings").get().await().toObjects(Car::class.java)
    }

    suspend fun getCarById(carId: String): Car? {
        return firestore.collection("listings").document(carId).get().await().toObject(Car::class.java)
    }

    suspend fun addCar(model: String, year: Int, price: Double, imageUris: List<Uri>): String {
        val currentUser = auth.currentUser ?: throw IllegalStateException("User must be logged in")

        val imageUrls = uploadImages(imageUris)

        val car = Car(
            model = model,
            year = year,
            price = price,
            imageUrls = imageUrls,
            ownerId = currentUser.uid,
            createdAt = System.currentTimeMillis()
        )

        return firestore.collection("listings").add(car).await().id
    }

    suspend fun updateCar(carId: String, model: String, year: Int, price: Double): Boolean {
        return try {
            firestore.collection("listings").document(carId)
                .update(
                    mapOf(
                        "model" to model,
                        "year" to year,
                        "price" to price
                    )
                ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun uploadImages(imageUris: List<Uri>): List<String> {
        val currentUser = auth.currentUser ?: throw IllegalStateException("User must be logged in")
        return imageUris.mapIndexed { index, uri ->
            val imageRef = storage.reference.child("car_images/${currentUser.uid}/${System.currentTimeMillis()}_${index}.jpg")
            imageRef.putFile(uri).await()
            imageRef.downloadUrl.await().toString()
        }
    }
}
