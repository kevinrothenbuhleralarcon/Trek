package ch.kra.trek.database

import androidx.room.Embedded
import androidx.room.Relation

data class TrekWithCoordinates(
    @Embedded val trek: Trek,
    @Relation(
        parentColumn = "id",
        entityColumn = "trek_id"
    )
    val coordinates: MutableList<Coordinate>
)
