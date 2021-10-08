package ch.kra.trek.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [TrekData::class], version = 1)
@TypeConverters(DataConverter::class)
abstract class TrekDatabase: RoomDatabase() {
    abstract fun trekDao(): TrekDao

    companion object {
        @Volatile
        private var INSTANCE: TrekDatabase? = null

        fun getDatabase(context: Context): TrekDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context,
                    TrekDatabase::class.java,
                    "trek_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}