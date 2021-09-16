package ch.kra.trek.helper

import ch.kra.trek.other.Constants.EARTH_RADIUS
import com.google.android.gms.maps.model.LatLng
import kotlin.math.*

object TrekUtility {

    private var maxPositiveDrop = 0.0
    private var totalDrop = 0.0

    fun getTrek(pathPoints: List<LatLng>, altitudes: List<Double>, timeInMs: Long, trekName: String): Trek {
        calculateDrops(altitudes)
        val km = getKm(pathPoints)
        return Trek(trekName = trekName, time = timeInMs, km = km, maxDrop = maxPositiveDrop, totalDrop = totalDrop, listLatLng = pathPoints)
    }

    private fun getKm(listCoordinate: List<LatLng>): Double {
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

    private fun calculateDrops(listAltitude: List<Double>) {
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