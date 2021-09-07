package ch.kra.trek.database

import androidx.room.Embedded
import androidx.room.Relation

data class TrekWithCoordinates(
    @Embedded val trekData: TrekData,
    @Relation(
        parentColumn = "id",
        entityColumn = "trek_id"
    )
    val coordinates: MutableList<Coordinate>
)
