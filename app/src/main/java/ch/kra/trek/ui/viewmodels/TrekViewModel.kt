package ch.kra.trek.ui.viewmodels

import androidx.lifecycle.*
import ch.kra.trek.database.Coordinate
import ch.kra.trek.database.TrekDao
import ch.kra.trek.database.TrekData
import ch.kra.trek.helper.TrekUtility
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

    private var coordinates: List<Coordinate>? = null
    private var timeInMs: Long? = null
    private var trekName: String? = null

    fun saveTrek(trek: TrekData) {
        viewModelScope.launch {
            trekDao.insertTrekData(trek)
        }
    }

    fun deleteTrek(trek: TrekData) {
        viewModelScope.launch {
            trekDao.deleteTrekData(trek)
        }
    }

    fun getTrek(id: Int): LiveData<TrekData> {
        val result = MutableLiveData<TrekData>()
        viewModelScope.launch {
            if (id != 0) { //in this case we're loading an existing trek
                result.postValue(trekDao.getTrek(id))
            } else { //in this case the new trek has ended and we're retrieving it
                coordinates?.let { coordinates ->
                    timeInMs?.let { timeInMs ->
                        trekName?.let { trekName ->
                            result.postValue(TrekUtility.getTrek(coordinates, timeInMs, trekName))
                        }
                    }
                }
            }
        }
        return result
    }

    fun setCurrentTrekData(coordinates: List<Coordinate>, timeInMs: Long, trekName: String)
    {
        this.coordinates = coordinates
        this.timeInMs = timeInMs
        this.trekName = trekName
    }
}