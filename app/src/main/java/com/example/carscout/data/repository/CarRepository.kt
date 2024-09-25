package com.example.carscout.data.repository

import android.net.Uri
import android.util.Log
import com.example.carscout.data.model.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
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
        val imageUrls = uploadImages(imageUris)
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

        return firestore.collection("listings").add(car).await().id
    }

    suspend fun updateCar(
        carId: String,
        manufacturer: String,
        model: String,
        year: Int,
        mileage: Int,
        condition: String,
        description: String,
        price: Double,
        imageUris: List<Uri>
    ): Boolean {
        return try {
            val carRef = firestore.collection("listings").document(carId)
            val existingCar = carRef.get().await().toObject(Car::class.java)
                ?: throw Exception("Car not found")

            val currentImageUrls = existingCar.imageUrls.toMutableList()

            val newImageUris = imageUris.filter { uri -> uri.toString().startsWith("content://") }
            val keptImageUrls = imageUris.filter { uri -> !uri.toString().startsWith("content://") }
                .map { it.toString() }

            val newImageUrls = uploadImages(newImageUris)

            val removedImageUrls = currentImageUrls - keptImageUrls
            deleteImagesFromStorage(removedImageUrls)

            val updatedImageUrls = keptImageUrls + newImageUrls

            carRef.update(
                mapOf(
                    "manufacturer" to manufacturer,
                    "model" to model,
                    "year" to year,
                    "mileage" to mileage,
                    "condition" to condition,
                    "description" to description,
                    "price" to price,
                    "imageUrls" to updatedImageUrls
                )
            ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteCar(carId: String): Void? {
        return try {
            firestore.collection("listings").document(carId).delete().await()
        } catch (e: Exception) {
            throw e
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

    private suspend fun deleteImagesFromStorage(imageUrls: List<String>) {
        for (url in imageUrls) {
            try {
                val imageRef = storage.getReferenceFromUrl(url)
                imageRef.delete().await()
                Log.d("DeleteImages", "Successfully deleted image at URL: $url")
            } catch (e: Exception) {
                if (e is StorageException && e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    Log.w("DeleteImages", "Image at URL $url does not exist. Skipping deletion.")
                } else {
                    Log.e("DeleteImages", "Failed to delete image at $url: ${e.message}")
                }
            }
        }
    }

}
