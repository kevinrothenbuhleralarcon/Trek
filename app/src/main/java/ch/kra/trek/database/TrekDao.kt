package ch.kra.trek.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrekDao {
    @Query("SELECT * FROM trek")
    fun getAllTrek(): Flow<List<Trek>>

    @Transaction
    @Query("SELECT * FROM trek WHERE id = :trekId")
    fun getTrekWithCoordinates(trekId: Int): Flow<TrekWithCoordinates>

    /*@Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrekWithCoordinates(trek: Trek, listCoordinate: List<Coordinate>)*/

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrek(trek: Trek): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCoordonates(listCoordinate: List<Coordinate>)

    @Transaction
    @Delete
    suspend fun deleteTrekWithCoordinates(trek: Trek, listCoordinates: List<Coordinate>)
}