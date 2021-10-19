package ch.kra.trek.repositories

import ch.kra.trek.database.TrekDao
import ch.kra.trek.database.TrekData

class TrekRepository(private val trekDao: TrekDao) {
    fun getAllTrek() =
         trekDao.getAllTrek()

    suspend fun getTrek(trekId: Int) =
        trekDao.getTrek(trekId)

    suspend fun insertTrekData(trekData: TrekData) =
        trekDao.insertTrekData(trekData)

    suspend fun deleteTrekData(trekData: TrekData) =
        trekDao.deleteTrekData(trekData)
}