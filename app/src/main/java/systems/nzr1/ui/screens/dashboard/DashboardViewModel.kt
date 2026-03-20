package systems.nzr1.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import systems.nzr1.data.preferences.UserPreferences
import systems.nzr1.data.repository.NetworkRepository
import systems.nzr1.data.repository.SystemRepository
import systems.nzr1.domain.model.SystemStats
import systems.nzr1.domain.model.WifiInfo
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val sysRepo: SystemRepository,
    private val netRepo: NetworkRepository,
    private val prefs: UserPreferences,
) : ViewModel() {

    val stats: StateFlow<SystemStats> = sysRepo.statsFlow(1000L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SystemStats())

    val wifiInfo: StateFlow<WifiInfo> = flow {
        while (true) {
            emit(netRepo.getWifiInfo())
            kotlinx.coroutines.delay(5000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WifiInfo())

    val internetAvailable: StateFlow<Boolean> = flow {
        while (true) {
            emit(netRepo.isInternetAvailable())
            kotlinx.coroutines.delay(3000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentTime: StateFlow<String> = flow {
        val fmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        while (true) {
            emit(fmt.format(Date()))
            kotlinx.coroutines.delay(1000)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val currentDate: StateFlow<String> = flow {
        val fmt = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        emit(fmt.format(Date()))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
}
