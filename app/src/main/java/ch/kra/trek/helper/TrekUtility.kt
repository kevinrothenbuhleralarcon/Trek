package ch.kra.trek.helper

import ch.kra.trek.database.Coordinate
import ch.kra.trek.database.TrekData
import ch.kra.trek.other.Constants.EARTH_RADIUS
import com.google.android.gms.maps.model.LatLng
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

object TrekUtility {

    private var totalPositiveDrop = 0.0
    private var totalNegativeDrop = 0.0

    fun getTrek(coordinates: List<Coordinate>, timeInMs: Long, trekName: String): TrekData {
        calculateDrops(coordinates)
        val km = getKm(coordinates)
        return TrekData(trekName = trekName, time = timeInMs, km = km, totalPositiveDrop = totalPositiveDrop, totalNegativeDrop = totalNegativeDrop, coordinates = coordinates)
    }

    private fun getKm(coordinates: List<Coordinate>): Double {
        var total: Double = 0.0
        var lastLatLng: LatLng? = null

        for (location in coordinates) {
            val newLatLng = LatLng(location.latitude, location.longitude)
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

    fun calculateDrops(coordinates: List<Coordinate>) {
        //altitude set as 10 km as we cannot be at an altitude on 10 km on foot
        var lastAltitude: Double? = null
        totalPositiveDrop = 0.0
        totalNegativeDrop = 0.0

        for (coordinate in coordinates) {
            val altitude = coordinate.altitude
            lastAltitude?.let {
                if (it < altitude) {
                    totalPositiveDrop += altitude - it
                } else if (it > altitude) {
                    totalNegativeDrop += altitude - it
                }
            }
            lastAltitude = altitude
        }
    }
}