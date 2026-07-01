package com.example.cityexplorerchallenge.data.remote

data class OverpassResponse(
    val elements: List<OverpassElement> = emptyList()
)

data class OverpassElement(
    val type: String? = null,
    val id: Long = 0L,
    val lat: Double? = null,
    val lon: Double? = null,
    val center: OverpassCenter? = null,
    val tags: Map<String, String>? = null
)

data class OverpassCenter(
    val lat: Double? = null,
    val lon: Double? = null
)