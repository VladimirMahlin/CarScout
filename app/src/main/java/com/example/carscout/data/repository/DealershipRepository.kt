package com.example.carscout.data.repository

import com.example.carscout.data.model.Dealership
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class DealershipRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Fetch all dealerships
    suspend fun getDealerships(): List<Dealership> {
        return firestore.collection("dealerships").get().await().toObjects(Dealership::class.java)
    }

    // Fetch a dealership by its ID
    suspend fun getDealershipById(dealershipId: String): Dealership? {
        return firestore.collection("dealerships").document(dealershipId).get().await().toObject(Dealership::class.java)
    }

    // Add a dealership
    suspend fun addDealership(name: String, address: String): String {
        val currentUser = auth.currentUser ?: throw IllegalStateException("User must be logged in")
        val userDoc = firestore.collection("users").document(currentUser.uid).get().await()
        val isBusiness = userDoc.getBoolean("isBusiness") ?: false

        if (!isBusiness) {
            throw IllegalStateException("Only business accounts can add dealerships")
        }

        val dealership = Dealership(name = name, address = address, ownerId = currentUser.uid)
        return firestore.collection("dealerships").add(dealership).await().id
    }
}
