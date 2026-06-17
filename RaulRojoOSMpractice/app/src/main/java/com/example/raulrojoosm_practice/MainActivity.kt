package com.example.raulrojoosm_practice

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.example.raulrojoosm_practice.ui.theme.Place
import com.example.raulrojoosm_practice.ui.theme.RaulRojoOSMpracticeTheme
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OSMMap()
        }
    }
}

@Composable
fun OSMMap() {
    val context = LocalContext.current

    val places = remember {
        definePOIPlaces()
    }

    var selectedPlace by remember {
        mutableStateOf("No place selected")
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        topBar = {
            Surface(
                shadowElevation = 2.dp
            ) {
                Text(
                    text = selectedPlace,
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(
                            horizontal = 8.dp,
                            vertical = 6.dp
                        )
                )
            }
        }
    ) { innerPadding ->

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clipToBounds(),

            factory = {
                buildMVConfiguration(context)
                createMapView(
                    context = context,
                    places = places,
                    onPlaceSelected = { place ->
                        selectedPlace = "${place.name} - ${place.description}"
                    }
                )
            }
        )
    }
}

private fun createMapView(
    context: Context,
    places: List<Place>,
    onPlaceSelected: (Place) -> Unit
): MapView {

    return MapView(context).apply {

        setTileSource(TileSourceFactory.MAPNIK)
        setMultiTouchControls(true)

        controller.setZoom(15.0)

        controller.setCenter(
            GeoPoint(
                50.0619,
                19.9368
            )
        )

        val mapView = this

        var customMarkerNumber = 1

        val mapEventsReceiver = object : MapEventsReceiver {

            override fun singleTapConfirmedHelper(
                point: GeoPoint
            ): Boolean {

                val customPlace = Place(
                    name = "Custom Marker $customMarkerNumber",

                    description = String.format(
                        Locale.US,
                        "Lat: %.6f\nLon: %.6f",
                        point.latitude,
                        point.longitude
                    ),

                    latitude = point.latitude,
                    longitude = point.longitude
                )

                createCustomMarker(
                    place = customPlace,
                    mapView = mapView,
                    onPlaceSelected = onPlaceSelected
                )

                customMarkerNumber++

                return true
            }

            override fun longPressHelper(
                point: GeoPoint
            ): Boolean {
                return false
            }
        }

        val mapEventsOverlay = MapEventsOverlay(
            mapEventsReceiver
        )

        overlays.add(
            0,
            mapEventsOverlay
        )

        places.forEach { place ->

            createPOIMarkerWithToast(
                place = place,
                mapView = this,
                context = context,
                onPlaceSelected = onPlaceSelected
            )
        }
    }
}

fun buildMVConfiguration(context: Context) {
    Configuration.getInstance().load(
        context,
        context.getSharedPreferences(
            "osmdroid",
            Context.MODE_PRIVATE
        )
    )

    Configuration.getInstance().userAgentValue = context.packageName
}

private fun definePOIPlaces(): List<Place> {
    val places = listOf(
        Place(
            "AGH University",
            "Faculty of Space Technologies",
            50.0663,
            19.9137
        ),
        Place(
            name = "Main Square",
            description = "Historical city center",
            latitude = 50.0619,
            longitude = 19.9368
        ),
        Place(
            name = "Wawel Castle",
            description = "Historic royal castle in Krakow",
            latitude = 50.0540,
            longitude = 19.9350
        )
    )
    return places
}

private fun createPOIMarker(place: Place, mapView: MapView) {
    val marker = Marker(mapView)
    marker.position =
        GeoPoint(
            place.latitude,
            place.longitude
        )
    marker.title = place.name
    marker.snippet = place.description

    mapView.overlays.add(marker)
}

private fun createPOIMarkerWithToast(
    place: Place,
    mapView: MapView,
    context: Context,
    onPlaceSelected: (Place) -> Unit
) {
    val marker = Marker(mapView)

    marker.position = GeoPoint(
        place.latitude,
        place.longitude
    )

    marker.title = place.name
    marker.snippet = place.description

    marker.setOnMarkerClickListener { _, _ ->
        onPlaceSelected(place)

        Toast.makeText(
            context,
            place.name,
            Toast.LENGTH_SHORT
        ).show()

        true
    }

    mapView.overlays.add(marker)
}

private fun createCustomMarker(
    place: Place,
    mapView: MapView,
    onPlaceSelected: (Place) -> Unit
) {
    val marker = Marker(mapView).apply {

        position = GeoPoint(
            place.latitude,
            place.longitude
        )

        title = place.name
        snippet = place.description

        setAnchor(
            Marker.ANCHOR_CENTER,
            Marker.ANCHOR_BOTTOM
        )

        setOnMarkerClickListener { clickedMarker, _ ->

            onPlaceSelected(place)

            clickedMarker.showInfoWindow()

            true
        }
    }

    mapView.overlays.add(marker)

    onPlaceSelected(place)

    marker.showInfoWindow()

    mapView.invalidate()
}