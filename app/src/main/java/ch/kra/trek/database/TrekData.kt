package ch.kra.trek.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class TrekData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @NonNull @ColumnInfo(name = "trek_name") var trekName: String,
    @NonNull @ColumnInfo val time: Long,
    @NonNull @ColumnInfo val km: Double,
    @NonNull @ColumnInfo(name = "total_positive_drop") val totalPositiveDrop: Double,
    @NonNull @ColumnInfo(name = "total_negative_drop") val totalNegativeDrop: Double,
    @NonNull @ColumnInfo(name = "coordinates") val coordinates: List<Coordinate>,
    @NonNull @ColumnInfo(name = "date") var date: Date
)
