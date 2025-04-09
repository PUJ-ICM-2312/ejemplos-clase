package com.icm2510.maps.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import java.util.Locale

class LocationHelper(private val context: Context) {

    internal val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Obtiene la ubicación actual del usuario de manera asíncrona.
     */
    @SuppressLint("MissingPermission") // Asegúrate de manejar permisos antes de llamar esta función
    fun registerLocationUpdates(callback: (LatLng) -> Unit): LocationCallback {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000 // Intervalo de actualización en ms
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.locations.forEach { location ->
                    callback(LatLng(location.latitude, location.longitude))
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        return locationCallback // Para poder detener las actualizaciones luego
    }

    /**
     * Convierte una dirección en texto a coordenadas (LatLng).
     */
    fun getLatLngFromAddress(address: String): Pair<Double, Double>? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses: List<Address> = geocoder.getFromLocationName(address, 1) ?: emptyList()
            if (addresses.isNotEmpty()) {
                val location = addresses[0]
                Pair(location.latitude, location.longitude)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Convierte coordenadas en dirección en texto.
     */
    fun getAddressFromLatLng(latitude: Double, longitude: Double): String? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return try {
            val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1) ?: emptyList()
            if (addresses.isNotEmpty()) {
                addresses[0].getAddressLine(0)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}