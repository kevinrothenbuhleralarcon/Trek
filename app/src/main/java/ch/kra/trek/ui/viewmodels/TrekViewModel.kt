package ch.kra.trek.ui.viewmodels

import androidx.lifecycle.*
import ch.kra.trek.database.Coordinate
import ch.kra.trek.database.TrekData
import ch.kra.trek.helper.TrekUtility
import ch.kra.trek.repositories.TrekRepository
import kotlinx.coroutines.launch

class TrekViewModel(private val trekRepository: TrekRepository): ViewModel() {

    class TrekViewModelFactory(private val trekRepository: TrekRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TrekViewModel::class.java)){
                @Suppress("UNCHECKED_CAST")
                return TrekViewModel(trekRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    val trekDataList: LiveData<List<TrekData>> = trekRepository.getAllTrek().asLiveData() //Used to show all the trek stored

    private var coordinates: List<Coordinate>? = null
    private var timeInMs: Long? = null
    private var trekName: String? = null

    fun saveTrek(trek: TrekData) {
        viewModelScope.launch {
            trekRepository.insertTrekData(trek)
        }
    }

    fun deleteTrek(trek: TrekData) {
        viewModelScope.launch {
            trekRepository.deleteTrekData(trek)
        }
    }

    fun getTrek(id: Int): LiveData<TrekData> {
        val result = MutableLiveData<TrekData>()
        viewModelScope.launch {
            if (id != 0) { //in this case we're loading an existing trek
                result.postValue(trekRepository.getTrek(id))
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