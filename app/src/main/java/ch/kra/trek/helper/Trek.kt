package ch.kra.trek.helper

import ch.kra.trek.database.Coordinate
import ch.kra.trek.database.TrekData
import ch.kra.trek.database.TrekWithCoordinates
import com.google.android.gms.maps.model.LatLng

class Trek(val trekId: Int = 0, var trekName: String = "newTrek", val time: Long, val km: Double, val maxDrop: Double, var totalDrop: Double, val listLatLng: List<LatLng> ) {
    companion object {
        fun fromTrekWithCoordinate(trekWithCoordinates: TrekWithCoordinates): Trek {
            val listLatLng = mutableListOf<LatLng>()
            for (coordinate in trekWithCoordinates.coordinates)
            {
                listLatLng.add(LatLng(coordinate.latitude, coordinate.longitude))
            }
            return Trek(
                trekWithCoordinates.trekData.id,
                trekWithCoordinates.trekData.trekName,
                trekWithCoordinates.trekData.time,
                trekWithCoordinates.trekData.km,
                trekWithCoordinates.trekData.maxDrop,
                trekWithCoordinates.trekData.totalDrop,
                listLatLng
            )
        }
    }

    fun getTrekData(): TrekData {
        return if (trekId == 0) {
            TrekData(trekName = trekName, time = time, km = km, maxDrop = maxDrop, totalDrop = totalDrop)
        } else {
            TrekData(id = trekId, trekName = trekName, time = time, km = km, maxDrop = maxDrop, totalDrop = totalDrop)
        }
    }

    fun getCoordinates(): MutableList<Coordinate> {
        val coordinates = mutableListOf<Coordinate>()
        for (latLng in listLatLng) {
            coordinates.add(Coordinate(trekId = trekId, latitude = latLng.latitude, longitude = latLng.longitude))
        }
        return coordinates
    }
}
