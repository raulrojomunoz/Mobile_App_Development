package com.example.cityexplorerchallenge.data.remote

import android.util.Log
import com.example.cityexplorerchallenge.domain.model.Place
import com.example.cityexplorerchallenge.domain.model.PlaceCategory
import com.example.cityexplorerchallenge.domain.model.UserLocation
import kotlinx.coroutines.CancellationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class PlaceRemoteDataSource {

    private companion object {
        const val BASE_URL = "https://overpass-api.de/api/"
        const val USER_AGENT = "CityExplorerChallenge/1.0 Android Student Project"
        const val MAX_TOTAL_CANDIDATES = 120
        const val EARTH_RADIUS_METERS = 6371000.0
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(35, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .callTimeout(40, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .header("User-Agent", USER_AGENT)
                .header("Accept", "application/json")
                .header("Content-Type", "text/plain")
                .build()

            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .build()

    private val apiService: OverpassApiService = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OverpassApiService::class.java)

    suspend fun getNearbyPlaces(
        userLocation: UserLocation,
        radiusMeters: Int = 2500
    ): List<Place> {
        val query = buildNearbyPlacesQuery(
            latitude = userLocation.latitude,
            longitude = userLocation.longitude,
            radiusMeters = radiusMeters
        )

        Log.d("OverpassDebug", "Query: $query")

        return try {
            Log.d("OverpassDebug", "Requesting nearby places...")
            Log.d(
                "OverpassDebug",
                "User location: ${userLocation.latitude}, ${userLocation.longitude}"
            )
            Log.d("OverpassDebug", "Radius: $radiusMeters")

            val requestBody = query.toRequestBody("text/plain".toMediaType())
            val response = apiService.getNearbyPlaces(requestBody)

            Log.d("OverpassDebug", "Raw elements received: ${response.elements.size}")

            // Converts raw Overpass elements into app domain places.
            val rawPlaces = response.elements
                .mapNotNull { element ->
                    element.toPlaceOrNull(userLocation)
                }

            // Removes duplicated places returned by different OSM element types.
            val distinctPlaces = rawPlaces
                .distinctBy { place ->
                    "${place.category.name}_${place.name.trim().lowercase()}_${place.latitude}_${place.longitude}"
                }

            Log.d(
                "OverpassDebug",
                "Before category balance: ${distinctPlaces.groupingBy { it.category }.eachCount()}"
            )

            // Keeps the candidate pool diverse across categories.
            val balancedPlaces = balancePlacesByCategory(distinctPlaces)

            Log.d(
                "OverpassDebug",
                "After category balance: ${balancedPlaces.groupingBy { it.category }.eachCount()}"
            )

            Log.d("OverpassDebug", "Places after filtering: ${balancedPlaces.size}")

            balancedPlaces
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.e("OverpassDebug", "Error loading places from Overpass", exception)
            emptyList()
        }
    }

    private fun buildNearbyPlacesQuery(
        latitude: Double,
        longitude: Double,
        radiusMeters: Int
    ): String {
        return """
            [out:json][timeout:25];
            (
              node["amenity"="cafe"](around:$radiusMeters,$latitude,$longitude);
              way["amenity"="cafe"](around:$radiusMeters,$latitude,$longitude);
              relation["amenity"="cafe"](around:$radiusMeters,$latitude,$longitude);

              node["amenity"="restaurant"](around:$radiusMeters,$latitude,$longitude);
              way["amenity"="restaurant"](around:$radiusMeters,$latitude,$longitude);
              relation["amenity"="restaurant"](around:$radiusMeters,$latitude,$longitude);

              node["amenity"="fast_food"](around:$radiusMeters,$latitude,$longitude);
              way["amenity"="fast_food"](around:$radiusMeters,$latitude,$longitude);
              relation["amenity"="fast_food"](around:$radiusMeters,$latitude,$longitude);

              node["amenity"="food_court"](around:$radiusMeters,$latitude,$longitude);
              way["amenity"="food_court"](around:$radiusMeters,$latitude,$longitude);
              relation["amenity"="food_court"](around:$radiusMeters,$latitude,$longitude);

              node["tourism"="museum"](around:$radiusMeters,$latitude,$longitude);
              way["tourism"="museum"](around:$radiusMeters,$latitude,$longitude);
              relation["tourism"="museum"](around:$radiusMeters,$latitude,$longitude);

              node["tourism"="gallery"](around:$radiusMeters,$latitude,$longitude);
              way["tourism"="gallery"](around:$radiusMeters,$latitude,$longitude);
              relation["tourism"="gallery"](around:$radiusMeters,$latitude,$longitude);

              node["tourism"="artwork"](around:$radiusMeters,$latitude,$longitude);
              way["tourism"="artwork"](around:$radiusMeters,$latitude,$longitude);
              relation["tourism"="artwork"](around:$radiusMeters,$latitude,$longitude);

              node["tourism"="attraction"](around:$radiusMeters,$latitude,$longitude);
              way["tourism"="attraction"](around:$radiusMeters,$latitude,$longitude);
              relation["tourism"="attraction"](around:$radiusMeters,$latitude,$longitude);

              node["tourism"="viewpoint"](around:$radiusMeters,$latitude,$longitude);
              way["tourism"="viewpoint"](around:$radiusMeters,$latitude,$longitude);
              relation["tourism"="viewpoint"](around:$radiusMeters,$latitude,$longitude);

              node["historic"](around:$radiusMeters,$latitude,$longitude);
              way["historic"](around:$radiusMeters,$latitude,$longitude);
              relation["historic"](around:$radiusMeters,$latitude,$longitude);

              node["leisure"="park"](around:$radiusMeters,$latitude,$longitude);
              way["leisure"="park"](around:$radiusMeters,$latitude,$longitude);
              relation["leisure"="park"](around:$radiusMeters,$latitude,$longitude);

              node["leisure"="garden"](around:$radiusMeters,$latitude,$longitude);
              way["leisure"="garden"](around:$radiusMeters,$latitude,$longitude);
              relation["leisure"="garden"](around:$radiusMeters,$latitude,$longitude);

              node["leisure"="nature_reserve"](around:$radiusMeters,$latitude,$longitude);
              way["leisure"="nature_reserve"](around:$radiusMeters,$latitude,$longitude);
              relation["leisure"="nature_reserve"](around:$radiusMeters,$latitude,$longitude);

              node["natural"="wood"](around:$radiusMeters,$latitude,$longitude);
              way["natural"="wood"](around:$radiusMeters,$latitude,$longitude);
              relation["natural"="wood"](around:$radiusMeters,$latitude,$longitude);

              node["natural"="water"](around:$radiusMeters,$latitude,$longitude);
              way["natural"="water"](around:$radiusMeters,$latitude,$longitude);
              relation["natural"="water"](around:$radiusMeters,$latitude,$longitude);

              node["landuse"="forest"](around:$radiusMeters,$latitude,$longitude);
              way["landuse"="forest"](around:$radiusMeters,$latitude,$longitude);
              relation["landuse"="forest"](around:$radiusMeters,$latitude,$longitude);

              node["leisure"="sports_centre"](around:$radiusMeters,$latitude,$longitude);
              way["leisure"="sports_centre"](around:$radiusMeters,$latitude,$longitude);
              relation["leisure"="sports_centre"](around:$radiusMeters,$latitude,$longitude);

              node["leisure"="stadium"](around:$radiusMeters,$latitude,$longitude);
              way["leisure"="stadium"](around:$radiusMeters,$latitude,$longitude);
              relation["leisure"="stadium"](around:$radiusMeters,$latitude,$longitude);

              node["leisure"="pitch"](around:$radiusMeters,$latitude,$longitude);
              way["leisure"="pitch"](around:$radiusMeters,$latitude,$longitude);
              relation["leisure"="pitch"](around:$radiusMeters,$latitude,$longitude);

              node["leisure"="fitness_centre"](around:$radiusMeters,$latitude,$longitude);
              way["leisure"="fitness_centre"](around:$radiusMeters,$latitude,$longitude);
              relation["leisure"="fitness_centre"](around:$radiusMeters,$latitude,$longitude);

              node["sport"](around:$radiusMeters,$latitude,$longitude);
              way["sport"](around:$radiusMeters,$latitude,$longitude);
              relation["sport"](around:$radiusMeters,$latitude,$longitude);
            );
            out center;
        """.trimIndent()
    }

    private fun OverpassElement.toPlaceOrNull(
        userLocation: UserLocation
    ): Place? {
        val tags = tags ?: return null

        val latitude = lat ?: center?.lat ?: return null
        val longitude = lon ?: center?.lon ?: return null

        val name = tags["name"]
            ?: tags["name:en"]
            ?: return null

        val category = mapTagsToCategory(tags)

        if (category == PlaceCategory.UNKNOWN) {
            return null
        }

        val distance = calculateDistanceMeters(
            startLatitude = userLocation.latitude,
            startLongitude = userLocation.longitude,
            endLatitude = latitude,
            endLongitude = longitude
        )

        return Place(
            id = "${type ?: "element"}_$id",
            name = name,
            category = category,
            latitude = latitude,
            longitude = longitude,
            distanceMeters = distance
        )
    }

    private fun mapTagsToCategory(
        tags: Map<String, String>
    ): PlaceCategory {
        val tourism = tags["tourism"]
        val amenity = tags["amenity"]
        val leisure = tags["leisure"]
        val historic = tags["historic"]
        val natural = tags["natural"]
        val landuse = tags["landuse"]
        val sport = tags["sport"]

        return when {
            historic != null -> {
                PlaceCategory.HISTORY
            }

            tourism == "museum" ||
                    tourism == "gallery" ||
                    tourism == "artwork" ||
                    tourism == "attraction" -> {
                PlaceCategory.CULTURE
            }

            tourism == "viewpoint" -> {
                PlaceCategory.NATURE
            }

            amenity == "cafe" -> {
                PlaceCategory.COFFEE
            }

            amenity == "restaurant" ||
                    amenity == "fast_food" ||
                    amenity == "food_court" -> {
                PlaceCategory.FOOD
            }

            leisure == "park" ||
                    leisure == "garden" ||
                    leisure == "nature_reserve" ||
                    natural == "wood" ||
                    natural == "water" ||
                    landuse == "forest" -> {
                PlaceCategory.NATURE
            }

            leisure == "sports_centre" ||
                    leisure == "stadium" ||
                    leisure == "pitch" ||
                    leisure == "fitness_centre" ||
                    sport != null -> {
                PlaceCategory.SPORT
            }

            else -> {
                PlaceCategory.UNKNOWN
            }
        }
    }

    private fun balancePlacesByCategory(
        places: List<Place>
    ): List<Place> {
        val categoryOrder = listOf(
            PlaceCategory.CULTURE,
            PlaceCategory.HISTORY,
            PlaceCategory.NATURE,
            PlaceCategory.SPORT,
            PlaceCategory.COFFEE,
            PlaceCategory.FOOD
        )

        val groupedPlaces = places.groupBy { place ->
            place.category
        }

        val availableCategories = categoryOrder.filter { category ->
            groupedPlaces[category].orEmpty().isNotEmpty()
        }

        if (availableCategories.isEmpty()) {
            return places
                .sortedBy { place -> place.distanceMeters }
                .take(MAX_TOTAL_CANDIDATES)
        }

        val maxPlacesPerCategory =
            (MAX_TOTAL_CANDIDATES + availableCategories.size - 1) / availableCategories.size

        val limitedGroupedPlaces = availableCategories.associateWith { category ->
            groupedPlaces[category]
                .orEmpty()
                .sortedBy { place -> place.distanceMeters }
                .take(maxPlacesPerCategory)
        }

        val balancedPlaces = mutableListOf<Place>()

        for (index in 0 until maxPlacesPerCategory) {
            availableCategories.forEach { category ->
                limitedGroupedPlaces[category]
                    ?.getOrNull(index)
                    ?.let { place ->
                        balancedPlaces.add(place)
                    }
            }
        }

        return balancedPlaces
            .take(MAX_TOTAL_CANDIDATES)
            .ifEmpty {
                places
                    .sortedBy { place -> place.distanceMeters }
                    .take(MAX_TOTAL_CANDIDATES)
            }
    }

    private fun calculateDistanceMeters(
        startLatitude: Double,
        startLongitude: Double,
        endLatitude: Double,
        endLongitude: Double
    ): Double {
        val startLatRad = Math.toRadians(startLatitude)
        val endLatRad = Math.toRadians(endLatitude)

        val deltaLat = Math.toRadians(endLatitude - startLatitude)
        val deltaLon = Math.toRadians(endLongitude - startLongitude)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(startLatRad) * cos(endLatRad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_METERS * c
    }
}