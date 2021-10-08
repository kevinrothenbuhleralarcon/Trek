package ch.kra.trek.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrekDao {
    @Query("SELECT * FROM trekData")
    fun getAllTrek(): Flow<List<TrekData>>

    @Transaction
    @Query("SELECT * FROM trekData WHERE id = :trekId")
    suspend fun getTrek(trekId: Int): TrekData


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrekData(trekData: TrekData): Long

    @Delete
    suspend fun deleteTrekData(trekData: TrekData)
}