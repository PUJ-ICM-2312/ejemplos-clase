package com.icm2510.maps

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.icm2510.maps.sensors.LightSensor
import com.icm2510.maps.ui.theme.MapsTheme
import com.icm2510.maps.utils.LocationHelper
import com.icm2510.maps.utils.LocationPermissionHandler
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapsTheme {
                MapScreen()
            }
        }
    }
}

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    val coroutineScope = rememberCoroutineScope()

    // Estado para la Polyline (ruta del usuario)
    var path by remember { mutableStateOf(listOf<LatLng>()) }


    // Estado para la ubicación del usuario
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasPermission by remember { mutableStateOf(false) }


    // Estado para controlar la posición de la cámara del mapa
    val cameraPositionState = rememberCameraPositionState {
        // Se puede establecer una posición inicial por defecto si se desea,
        // pero se sobrescribirá cuando se obtenga la ubicación del usuario.
        position = CameraPosition.fromLatLngZoom(LatLng(4.60971, -74.08175), 5f) // Bogotá como fallback inicial lejano
    }
    // Estado para saber si ya se hizo el movimiento inicial de cámara
    var isInitialCameraMoveDone by remember { mutableStateOf(false) }

    LocationPermissionHandler {
        hasPermission = true
    }


    DisposableEffect(hasPermission) {
        if (hasPermission) {
            val locationCallback = locationHelper.registerLocationUpdates { latLng ->
                path = path + latLng
                userLocation = latLng
            }

            onDispose {
                locationHelper.fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        } else {
            onDispose {}
        }
    }


    // Efecto para mover la cámara a la ubicación inicial del usuario
    LaunchedEffect(userLocation) {
        // Se ejecutará cada vez que userLocation cambie de null a un valor
        if (userLocation != null && !isInitialCameraMoveDone) {
            // Anima la cámara a la nueva posición (ubicación actual) con zoom 15
            cameraPositionState.animate(
                update = CameraUpdateFactory.newLatLngZoom(userLocation!!, 15f),
                durationMs = 1000 // Duración de la animación en milisegundos (opcional)
            )
            isInitialCameraMoveDone = true // Marca que el movimiento inicial ya se hizo
        }
    }


    if (!hasPermission) {
        Text("Se requiere permiso de ubicación para usar el mapa.",
            modifier = Modifier.padding(16.dp).fillMaxSize().statusBarsPadding(),
            textAlign = TextAlign.Center
        )
        return
    }

    // Estado para la búsqueda de direcciones
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var searchLocation by remember { mutableStateOf<LatLng?>(null) }

    // Estado para los marcadores
    var markers by remember { mutableStateOf(listOf<MarkerData>()) }


    // Estado para el estilo del mapa:
    var isDarkMap by remember { mutableStateOf(false) }

    // Usar el sensor:
    LightSensor { isLowLight ->
        isDarkMap = isLowLight
    }

    // Configurar el estilo del mapa:
    val mapProperties by remember(isDarkMap) {
        mutableStateOf(
            MapProperties(
                mapStyleOptions = if (isDarkMap) {
                    MapStyleOptions.loadRawResourceStyle(context, R.raw.dark_map_style)
                } else {
                    null // Usa el estilo por defecto (claro)
                },
                isMyLocationEnabled = true
            )
        )
    }

    // Configuración de la UI del mapa
    val mapUiSettings = MapUiSettings(
        myLocationButtonEnabled = true,
        zoomControlsEnabled = true
    )


    // Estructura de la interfaz de usuario
    Box {
        // Mapa de Google
        GoogleMap(
            contentPadding = PaddingValues(
                top = 150.dp
            ),
            properties = mapProperties,
            uiSettings = mapUiSettings,
            cameraPositionState = cameraPositionState, // Usar el estado de la cámara
            onMapLongClick = { latLng ->
                coroutineScope.launch {
                    val address = locationHelper.getAddressFromLatLng(latLng.latitude, latLng.longitude)
                    markers = markers + MarkerData(latLng, address ?: "Ubicación desconocida")
                }
            }
        ) {
            // Muestra la ubicación actual con unicono personalizado
            userLocation?.let { currentLocation ->
                // Se crea el BitmapDescriptor a partir del recurso drawable (png)
                val userIcon: BitmapDescriptor = remember(context) { // Recordar para eficiencia
                    BitmapDescriptorFactory.fromResource(R.drawable.user)
                }

                Marker(
                    state = MarkerState(position = currentLocation),
                    title = "Tu ubicación",
                    // Se usa el icono personalizado
                    icon = userIcon
                )
            }


            // Muestra los marcadores agregados
            markers.forEach { marker ->
                Marker(
                    state = MarkerState(position = marker.position),
                    title = marker.title
                )
            }

            // Dibuja la ruta del usuario
            if (path.isNotEmpty()) {
                Polyline(
                    points = path,
                    color = androidx.compose.ui.graphics.Color.Blue,
                    width = 10f
                )
            }
        }

        Row (
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Campo de búsqueda de direcciones
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Buscar dirección") },
                modifier = Modifier.padding(10.dp).fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            val result = locationHelper.getLatLngFromAddress(searchQuery.text)
                            if (result != null) {
                                searchLocation = LatLng(result.first, result.second)
                                markers = markers + MarkerData(searchLocation!!, searchQuery.text)
                                // Mover cámara a la ubicación buscada
                                cameraPositionState.animate(
                                    update = CameraUpdateFactory.newLatLngZoom(searchLocation!!, 15f), // Zoom más cercano para búsquedas
                                    durationMs = 1000
                                )
                            } else {
                                Toast.makeText(context, "Dirección no encontrada", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Icon(painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "Buscar"
                        )
                    }
                }
            )

        }
    }
}

// Modelo de datos para los marcadores
data class MarkerData(val position: LatLng, val title: String)
