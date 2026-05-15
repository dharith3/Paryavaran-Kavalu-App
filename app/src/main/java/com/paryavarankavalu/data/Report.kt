package com.paryavarankavalu.data

data class Report(
    val id: Long = 0,
    val reporterName: String,
    val category: String,
    val notes: String,
    val photoPath: String,
    val latitude: Double,
    val longitude: Double,
    val isCleaned: Boolean,
    val createdAt: Long,
    val cleanedAt: Long? = null,
    val cleanedBy: String? = null,
    val cleanedPhotoPath: String? = null
)
