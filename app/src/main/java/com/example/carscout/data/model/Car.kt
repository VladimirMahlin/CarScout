package com.example.carscout.data.model

import com.google.firebase.firestore.DocumentId

data class Car(
    @DocumentId val id: String = "",
    val model: String = "",
    val year: Int = 0,
    val price: Double = 0.0,
    val imageUrls: List<String> = listOf(),
    val ownerId: String = "",
    val createdAt: Long = 0
) {
    constructor() : this("", "", 0, 0.0, listOf(), "", 0)
}