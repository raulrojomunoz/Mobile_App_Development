package com.example.cityexplorerchallenge.data.remote

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface OverpassApiService {

    @POST("interpreter")
    suspend fun getNearbyPlaces(
        @Body query: RequestBody
    ): OverpassResponse
}