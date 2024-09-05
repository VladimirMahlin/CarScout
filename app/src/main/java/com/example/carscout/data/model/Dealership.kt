package com.example.carscout.data.model

import com.google.firebase.firestore.DocumentId

data class Dealership(
    @DocumentId val id: String = "",
    val name: String = "",
    val address: String = "",
    val ownerId: String = ""
)
