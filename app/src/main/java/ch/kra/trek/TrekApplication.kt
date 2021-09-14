package ch.kra.trek

import android.app.Application
import ch.kra.trek.database.TrekDatabase

class TrekApplication(): Application() {
    val database: TrekDatabase by lazy { TrekDatabase.getDatabase(this) }
}