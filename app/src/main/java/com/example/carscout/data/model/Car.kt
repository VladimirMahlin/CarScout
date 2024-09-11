package com.example.carscout.data.model

import com.google.firebase.firestore.DocumentId

data class Car(
    @DocumentId val id: String = "",
    val manufacturer: String = "",
    val model: String = "",
    val year: Int = 0,
    val mileage: Int = 0,
    val condition: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrls: List<String> = listOf(),
    val ownerId: String = "",
    val createdAt: Long = 0
)