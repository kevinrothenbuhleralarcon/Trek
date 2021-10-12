package ch.kra.trek.helper

import ch.kra.trek.database.Coordinate
import ch.kra.trek.database.TrekData
import ch.kra.trek.other.Constants.EARTH_RADIUS
import kotlin.math.*

object TrekUtility {

    private var totalPositiveDrop = 0.0
    private var totalNegativeDrop = 0.0

    fun getTrek(coordinates: List<Coordinate>, timeInMs: Long, trekName: String): TrekData {
        calculateDrops(coordinates)
        val km = getDistanceInMeter(coordinates)
        return TrekData(trekName = trekName, time = timeInMs, km = km, totalPositiveDrop = totalPositiveDrop, totalNegativeDrop = totalNegativeDrop, coordinates = coordinates)
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