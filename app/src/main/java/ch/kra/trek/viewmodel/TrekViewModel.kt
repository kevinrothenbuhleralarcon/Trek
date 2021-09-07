package ch.kra.trek.viewmodel

import android.location.Location
import android.location.LocationListener
import androidx.lifecycle.*
import ch.kra.trek.database.TrekDao
import ch.kra.trek.database.TrekData
import ch.kra.trek.helper.Trek
import ch.kra.trek.helper.TrekManager
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class TrekViewModel(private val trekDao: TrekDao): ViewModel(), LocationListener {

    class TrekViewModelFactory(private val trekDao: TrekDao): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TrekViewModel::class.java)){
                @Suppress("UNCHECKED_CAST")
                return TrekViewModel(trekDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    private lateinit var trekManager: TrekManager

    val trekDataList: LiveData<List<TrekData>> = trekDao.getAllTrek().asLiveData() //Used to show all the trek stored
    private val _trekCoordinates = MutableLiveData<List<LatLng>>()
    val trekCoordinates: LiveData<List<LatLng>> get() = _trekCoordinates //Used to show the path

    fun saveTrek(trek: Trek) {
        viewModelScope.launch {
            val newTrekId = trekDao.insertTrekData(trek.getTrekData())
            for (coordinate in trek.getCoordinates()) {
                coordinate.trekId = newTrekId.toInt()
                trekDao.insertCoordonate(coordinate)
            }
        }
    }

    fun deleteTrek(trek: Trek) {
        viewModelScope.launch {
            trekDao.deleteTrekWithCoordinates(trek.getTrekData(), trek.getCoordinates())
        }
    }

    fun getTrek(id: Int): LiveData<Trek> {
        val result = MutableLiveData<Trek>()
        viewModelScope.launch {
            if (id != 0) { //in this case we're loading an existing trek
                result.postValue(Trek.fromTrekWithCoordinate(trekDao.getTrekWithCoordinates(id)))
            } else { //in this case the new trek has ended and we're retrieving it
                result.postValue(trekManager.getTrek())
            }
        }
        return result
    }

    fun startTrek() { trekManager = TrekManager() }

    fun endTrek() { trekManager.stop() }

    override fun onLocationChanged(location: Location) {
        trekManager.newLocation(location)
        _trekCoordinates.value = trekManager.listCoordinate
    }
}