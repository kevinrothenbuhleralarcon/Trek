package ch.kra.trek.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TrekData::class], version = 2)
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
                    .addMigrations(MIGRATION_1_2)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE trekData ADD COLUMN date INTEGER NOT NULL DEFAULT 0")
            }

        }
    }
}