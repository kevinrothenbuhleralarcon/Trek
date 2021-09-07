package ch.kra.trek.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrekDao {
    @Query("SELECT * FROM trekData")
    fun getAllTrek(): Flow<List<TrekData>>

    @Transaction
    @Query("SELECT * FROM trekData WHERE id = :trekId")
    suspend fun getTrekWithCoordinates(trekId: Int): TrekWithCoordinates


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrekData(trekData: TrekData): Long


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCoordonate(coordinate: Coordinate)

    @Transaction
    @Delete
    suspend fun deleteTrekWithCoordinates(trekData: TrekData, listCoordinates: List<Coordinate>)
}