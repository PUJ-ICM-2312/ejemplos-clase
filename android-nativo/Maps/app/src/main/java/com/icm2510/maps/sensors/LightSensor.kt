package com.icm2510.maps.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Composable que escucha los cambios en el sensor de luz y activa o desactiva el modo oscuro
 * según la cantidad de luz detectada.
 *
 * @param onLightChanged Función que se llama cuando cambia la luz. Recibe un booleano que indica
 * si el modo oscuro debe activarse (true) o desactivarse (false).
 */
@Composable
fun LightSensor(onLightChanged: (Boolean) -> Unit) {
    val context = LocalContext.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val sensor = remember { sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) }

    DisposableEffect(sensorManager) {
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.values?.firstOrNull()?.let { lux ->
                    onLightChanged(lux < 50) // Si la luz es baja, activar modo oscuro
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        onDispose { sensorManager.unregisterListener(listener) }
    }
}
