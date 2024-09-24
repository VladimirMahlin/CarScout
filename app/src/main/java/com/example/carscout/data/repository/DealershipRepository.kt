package com.example.carscout.data.repository

import android.net.Uri
import com.example.carscout.data.model.Dealership
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class DealershipRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun getDealerships(): List<Dealership> {
        return firestore.collection("dealerships").get().await().toObjects(Dealership::class.java)
    }

    suspend fun getDealershipById(dealershipId: String): Dealership? {
        return firestore.collection("dealerships") // Ensure collection is referenced properly
            .document(dealershipId) // Reference the correct document inside the collection
            .get().await()
            .toObject(Dealership::class.java)
    }

    suspend fun addDealership(
        name: String,
        address: String,
        phoneNumber: String,
        email: String,
        imageUris: List<Uri>
    ): String {
        val currentUser = auth.currentUser ?: throw IllegalStateException("User must be logged in")
        val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
        val isBusiness = userDoc.getBoolean("isBusiness") ?: false
        val imageUrls = uploadImages(imageUris)

        if (!isBusiness) {
            throw IllegalStateException("Only business accounts can add dealerships")
        }

        val dealership = Dealership(
            name = name,
            address = address,
            phoneNumber = phoneNumber,
            email = email,
            imageUrls = imageUrls,
            ownerId = currentUser.uid,
            createdAt = System.currentTimeMillis()
        )
        return firestore.collection("dealerships").add(dealership).await().id
    }

    suspend fun editDealership(
        dealershipId: String,
        name: String,
        address: String,
        phoneNumber: String,
        email: String,
    ) {
        val currentUser = auth.currentUser ?: throw IllegalStateException("User must be logged in")
        val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
        val isBusiness = userDoc.getBoolean("isBusiness") ?: false

        if (!isBusiness) {
            throw IllegalStateException("Only business accounts can edit dealerships")
        }

        val dealership = Dealership(
            name = name,
            address = address,
            phoneNumber = phoneNumber,
            email = email,
            ownerId = currentUser.uid,
            createdAt = System.currentTimeMillis()
        )
        firestore.collection("dealerships").document(dealershipId).set(dealership).await()
    }

    private suspend fun uploadImages(imageUris: List<Uri>): List<String> {
        val currentUser = auth.currentUser ?: throw IllegalStateException("User must be logged in")
        return imageUris.mapIndexed { index, uri ->
            val imageRef =
                storage.reference.child("car_images/${currentUser.uid}/${System.currentTimeMillis()}_${index}.jpg")
            imageRef.putFile(uri).await()
            imageRef.downloadUrl.await().toString()
        }
    }
}
