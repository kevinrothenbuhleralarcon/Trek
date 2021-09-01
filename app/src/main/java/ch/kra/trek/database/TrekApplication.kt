package ch.kra.trek.database

import android.app.Application

class TrekApplication(): Application() {
    val database: TrekDatabase by lazy { TrekDatabase.getDatabase(this) }
}