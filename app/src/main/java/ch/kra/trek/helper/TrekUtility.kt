package ch.kra.trek.helper

import ch.kra.trek.R
import ch.kra.trek.database.Coordinate
import ch.kra.trek.database.TrekData
import ch.kra.trek.other.Constants.EARTH_RADIUS
import com.google.android.gms.maps.GoogleMap
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

object TrekUtility {

    private var totalPositiveDrop = 0.0
    private var totalNegativeDrop = 0.0

    fun getTrek(coordinates: List<Coordinate>, timeInMs: Long, trekName: String): TrekData {
        calculateDrops(coordinates)
        val km = getDistanceInMeter(coordinates)
        return TrekData(trekName = trekName, date = Date(System.currentTimeMillis()), time = timeInMs, km = km, totalPositiveDrop = totalPositiveDrop, totalNegativeDrop = totalNegativeDrop, coordinates = coordinates)
    }

    fun getDistanceInMeterBetweenTwoCoordinate(coordinate1: Coordinate, coordinate2: Coordinate): Double {
        val lat1InRad = coordinate1.latitude * PI / 180
        val lat2InRad = coordinate2.latitude * PI / 180
        val deltaLatInRad = (coordinate2.latitude - coordinate1.latitude) * PI / 180
        val deltaLonInRad = (coordinate2.longitude - coordinate1.longitude) * PI / 180

        val a = sin(deltaLatInRad / 2).pow(2) +
                cos(lat1InRad) * cos(lat2InRad) *
                sin(deltaLonInRad / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1-a))
        return EARTH_RADIUS * c
    }

    fun getMapTypeFromBtnId(id: Int): Int {
        return when (id) {
            R.id.road_map_type -> GoogleMap.MAP_TYPE_NORMAL
            R.id.satellite_map_type -> GoogleMap.MAP_TYPE_SATELLITE
            R.id.hybrid_map_type -> GoogleMap.MAP_TYPE_HYBRID
            R.id.terrain_map_type -> GoogleMap.MAP_TYPE_TERRAIN
            else -> GoogleMap.MAP_TYPE_NORMAL
        }
    }

    fun getTimeInStringFormat(time:Long): String {
        val timeFormat = SimpleDateFormat("HH:mm:ss")
        timeFormat.timeZone = TimeZone.getTimeZone("GMT+0") //needed on physical device for a chrono in order that it start at 0
        return timeFormat.format(time)
    }

    fun getDateInStringFormat(date: Date): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy")
        return dateFormat.format(date)
    }


    private fun getDistanceInMeter(coordinates: List<Coordinate>): Double {
        var total: Double = 0.0
        var lastLatLng: Coordinate? = null

        for (coordinate in coordinates) {
            if (lastLatLng != null) {
                total += getDistanceInMeterBetweenTwoCoordinate(lastLatLng, coordinate)
            }
            lastLatLng = coordinate
        }
        return total
    }

    private fun calculateDrops(coordinates: List<Coordinate>) {
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