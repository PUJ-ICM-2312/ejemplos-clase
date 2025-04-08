package com.icm2510.location


import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavigationStack(
                        modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun NavigationStack(modifier: Modifier = Modifier) {
    // Crea un controlador de navegación que gestiona las pantallas.
    val navController = rememberNavController()

    // Define el host de navegación, que contiene las rutas y pantallas disponibles.
    NavHost(
        navController = navController, // Asocia el controlador de navegación.
        startDestination = "main_screen" // Define la pantalla inicial.
    ) {
        // Define la pantalla principal ("main_screen").
        composable(route = "main_screen") {
            // Muestra la pantalla principal, pasando el controlador de navegación.
            MainScreen(navController = navController, modifier = modifier)
        }

        // Define la pantalla de last location ("main_screen").
        composable(route = "location_single_use_screen") {
            // Muestra la pantalla principal, pasando el controlador de navegación.
            LocationSingleUseScreen(navController = navController, modifier = modifier)
        }

        // Define la pantalla de location updates ("main_screen").
        composable(route = "location_aware_screen") {
            // Muestra la pantalla principal, pasando el controlador de navegación.
            LocationAwareScreen(navController = navController, modifier = modifier)
        }
    }
}


@Composable
fun MainScreen(navController: NavController, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize().padding(40.dp)
    ) {
        Button(onClick = {
            navController.navigate(route = "location_single_use_screen") // Envía el valor como String
        },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Location Single Use")
        }
        Spacer(modifier = Modifier.size(8.dp))
        ElevatedButton(onClick = {
            navController.navigate(route = "location_aware_screen") // Envía el valor como String
        },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Location Updates")
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationSingleUseScreen(navController: NavController, modifier: Modifier = Modifier) {
    val location = remember { mutableStateOf<Location?>(null) }
    val permissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)

    if (!permissionState.status.isGranted) {
        LaunchedEffect(Unit) { permissionState.launchPermissionRequest() }
    } else {
        FetchSingleLocation { location.value = it }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            !permissionState.status.isGranted -> Text("Esperando permisos...")
            location.value == null -> Text("Obteniendo ubicación...")
            else -> {
                Text("Latitud: ${location.value?.latitude ?: "N/A"}")
                Text("Longitud: ${location.value?.longitude ?: "N/A"}")
            }
        }

        Button(onClick = { navController.popBackStack() }) {
            Text("Volver")
        }
    }
}

@Composable
fun FetchSingleLocation(onLocationFetched: (Location?) -> Unit) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    LaunchedEffect(Unit) {
        try {
            // Asegúrate de tener este import:
            // import kotlinx.coroutines.tasks.await
            val locationResult = fusedLocationClient.lastLocation.await()
            onLocationFetched(locationResult)
        } catch (e: SecurityException) {
            Log.e("Location", "Permiso denegado", e)
        } catch (e: Exception) {
            Log.e("Location", "Error al obtener ubicación", e)
        }
    }
}



@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationAwareScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val permissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    var location by remember { mutableStateOf<Location?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var isTracking by remember { mutableStateOf(false) }

    // Configuración de la solicitud de ubicación
    val locationRequest = remember {
        LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            10_000 // Intervalo de 10 segundos
        ).apply {
            setMinUpdateDistanceMeters(10f)
            setWaitForAccurateLocation(true)
        }.build()
    }

    // Callback para las actualizaciones
    val locationCallback = remember {
        object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                location = result.lastLocation
            }
        }
    }

    // Manejo de permisos
    if (!permissionState.status.isGranted) {
        LaunchedEffect(Unit) {
            permissionState.launchPermissionRequest()
        }
    }

    // Manejo del ciclo de vida
    DisposableEffect(isTracking) {
        if (isTracking && permissionState.status.isGranted) {
            try {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                Log.e("Location", "Error de permisos", e)
            }
        }

        onDispose {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    // UI
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            !permissionState.status.isGranted -> {
                Text("Se requieren permisos de ubicación")
                Button(onClick = { permissionState.launchPermissionRequest() }) {
                    Text("Solicitar permisos")
                }
            }
            !isTracking -> {
                Text("Presione el botón para comenzar el seguimiento")
                Button(onClick = { isTracking = true },
                    modifier = Modifier.width(200.dp)) {
                    Text("Iniciar seguimiento")
                }
            }
            location == null -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Obteniendo ubicación...")
            }
            else -> {
                LocationDataDisplay(location!!)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { isTracking = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.width(200.dp)
                ) {
                    Text("Detener seguimiento",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        FilledTonalButton(onClick = { navController.popBackStack() },
            modifier = Modifier.width(200.dp)) {
            Text("Volver al menú principal")
        }
    }
}

@Composable
fun LocationDataDisplay(location: Location) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Ubicación en tiempo real", style = MaterialTheme.typography.headlineSmall)
        HorizontalDivider()

        Text("Latitud: ${"%.6f".format(location.latitude)}")
        Text("Longitud: ${"%.6f".format(location.longitude)}")
        Text("Precisión: ${"%.1f".format(location.accuracy)} metros")

        if (location.hasAltitude()) {
            Text("Altitud: ${"%.1f".format(location.altitude)} metros")
        }

        if (location.hasSpeed()) {
            Text("Velocidad: ${"%.1f".format(location.speed * 3.6)} km/h")
        }

        Text("Actualizado: ${java.text.SimpleDateFormat("HH:mm:ss").format(java.util.Date())}")
    }
}