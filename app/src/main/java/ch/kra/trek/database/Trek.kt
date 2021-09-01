package ch.kra.trek.database

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Trek(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @NonNull @ColumnInfo(name = "trek_name") val trekName: String,
    @NonNull @ColumnInfo val time: Long,
    @NonNull @ColumnInfo val km: Double,
    @NonNull @ColumnInfo(name = "max_drop") val maxDrop: Double,
    @NonNull @ColumnInfo(name = "total_drop") val totalDrop: Double
)
