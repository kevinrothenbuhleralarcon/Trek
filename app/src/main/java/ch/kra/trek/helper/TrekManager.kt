package ch.kra.trek.helper

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

class TrekManager() {
    private val EARTH_RADIUS = 6367445
    private val _listCoordinate = mutableListOf<LatLng>()
    val listCoordinate: List<LatLng> get() = _listCoordinate
    private val listAltitude = mutableListOf<Double>()
    private val chrono = Chrono() //initiating the chrono will start it
    private var maxPositiveDrop = 0.0
    private var totalDrop = 0.0

    fun newLocation(location: Location) {
        _listCoordinate.add(LatLng(location.latitude, location.longitude))
        listAltitude.add(location.altitude)
    }

    fun stop() {
        chrono.stop()
    }

    fun getTrek(): Trek {
        calculateDrops()
        val km = getKm()
        val time = chrono.deltaTime()
        return Trek(time = time, km = km, maxDrop = maxPositiveDrop, totalDrop = totalDrop, listLatLng = listCoordinate)
    }

    private fun getKm(): Double {
        var total: Double = 0.0
        var lastLatLng: LatLng? = null

        for (newLatLng in listCoordinate) {
            if (lastLatLng != null) {
                val newLat = newLatLng.latitude * PI / 180
                val oldLat = lastLatLng.latitude * PI / 180
                val newLng = newLatLng.longitude * PI / 180
                val oldLng = lastLatLng.longitude * PI / 180

                total += EARTH_RADIUS * acos((sin(oldLat) * sin(newLat)) + (cos(oldLat) * cos(newLat) * cos(oldLng - newLng)))
            }
            lastLatLng = newLatLng
        }
        return total
    }

    private fun calculateDrops() {
        //altitude set as 10 km as we cannot be at an altitude on 10 km on foot
        var lastAltitude = 10000.0
        var firstAltitude = 10000.0
        maxPositiveDrop = 0.0
        totalDrop = 0.0

        for (altitude in listAltitude) {
            //determine the total drop
            if (lastAltitude != 10000.0) {
                totalDrop += abs(altitude - lastAltitude)
            }

            //determine the max drop
            if (altitude < lastAltitude) { //in this case we're going down so we set this altitude as the new start
                firstAltitude = altitude
            }
            lastAltitude = altitude

            if (lastAltitude - firstAltitude > maxPositiveDrop) {
                maxPositiveDrop = lastAltitude - firstAltitude
            }
        }
    }
}