package ch.kra.trek.ui.viewmodels

import androidx.lifecycle.*
import ch.kra.trek.database.TrekDao
import ch.kra.trek.database.TrekData
import ch.kra.trek.helper.Trek
import ch.kra.trek.helper.TrekUtility
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

class TrekViewModel(private val trekDao: TrekDao): ViewModel() {

    class TrekViewModelFactory(private val trekDao: TrekDao): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TrekViewModel::class.java)){
                @Suppress("UNCHECKED_CAST")
                return TrekViewModel(trekDao) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    val trekDataList: LiveData<List<TrekData>> = trekDao.getAllTrek().asLiveData() //Used to show all the trek stored

    private var pathPoints: List<LatLng>? = null
    private var altitudes: List<Double>? = null
    private var timeInMs: Long? = null
    private var trekName: String? = null

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
                pathPoints?.let { pathPoints ->
                    altitudes?.let { altitudes ->
                        timeInMs?.let { timeInMs ->
                            trekName?.let { trekName ->
                                result.postValue(TrekUtility.getTrek(pathPoints, altitudes, timeInMs, trekName))
                            }
                        }
                    }
                }

            }
        }
        return result
    }

    fun setCurrentTrekData(pathPoints: List<LatLng>, altitudes: List<Double>, timeInMs: Long, trekName: String)
    {
        this.pathPoints = pathPoints
        this.altitudes = altitudes
        this.timeInMs = timeInMs
        this.trekName = trekName
    }
}