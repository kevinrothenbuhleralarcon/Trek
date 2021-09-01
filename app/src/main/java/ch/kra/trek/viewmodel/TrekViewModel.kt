package ch.kra.trek.viewmodel

import android.util.Log
import androidx.lifecycle.*
import ch.kra.trek.database.Coordinate
import ch.kra.trek.database.Trek
import ch.kra.trek.database.TrekDao
import ch.kra.trek.database.TrekWithCoordinates
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
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

    private val _trekId = MutableLiveData(0)
    private val trekId: LiveData<Int> get() = _trekId
    val trekList: LiveData<List<Trek>> = trekDao.getAllTrek().asLiveData() //Used to show all the trek stored
    val trekWithCoordinates: LiveData<TrekWithCoordinates> = Transformations.switchMap(_trekId) { _ -> getTrek() }

    private val _trekCoordinates = MutableLiveData<List<LatLng>>()
    val trekCoordinates: LiveData<List<LatLng>> get() = _trekCoordinates


    fun saveTrek() {
        viewModelScope.launch {
            val newTrekId = trekDao.insertTrek(trekWithCoordinates.value!!.trek)
            //needed otherwise the trekId is not filled
            for (coordinate in trekWithCoordinates.value!!.coordinates) {
                coordinate.trekId = newTrekId.toInt()
            }
            trekDao.insertCoordonates(trekWithCoordinates.value!!.coordinates)
        }
    }

    fun deleteTrek() {
        viewModelScope.launch {
            trekDao.deleteTrekWithCoordinates(trekWithCoordinates.value!!.trek, trekWithCoordinates.value!!.coordinates)
        }
    }

    fun setTrekToLoad(trekId: Int) {
        _trekId.value = trekId
    }

    //Test function, to be replaced
    fun startTrek() {
        val testDrawPath = listOf<LatLng>(
            LatLng( -35.016, 143.321),
            LatLng(-34.747, 145.592),
            LatLng( -34.364, 147.891),
            LatLng(-33.501, 150.217),
            LatLng(-32.306, 149.248),
            LatLng(-32.491,  147.309)
        )
        val coordinates = mutableListOf<LatLng>()
        viewModelScope.launch {
            for (point in testDrawPath){
                Log.d("line", "New coordinate added")
                coordinates.add(point)
                _trekCoordinates.value = coordinates
                delay(5000)
            }
        }
    }

    private fun getTrek(): LiveData<TrekWithCoordinates> {
        return if (trekId.value != 0) {
            trekDao.getTrekWithCoordinates(trekId.value!!).asLiveData()
        } else {
            //create a new TrekWithCoordinates that will be populate during the trek
            val trekInfo = Trek(trekName = "newTrek", time = 0, km = 0.0, maxDrop = 0.0, totalDrop = 0.0)
            val listCoordinate = mutableListOf<Coordinate>()
            MutableLiveData<TrekWithCoordinates>(TrekWithCoordinates(trekInfo, listCoordinate)) as LiveData<TrekWithCoordinates>
        }
    }
}