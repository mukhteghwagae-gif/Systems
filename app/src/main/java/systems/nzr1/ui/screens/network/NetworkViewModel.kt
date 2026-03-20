package systems.nzr1.ui.screens.network

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import systems.nzr1.data.repository.NetworkRepository
import systems.nzr1.domain.model.NetworkDevice
import systems.nzr1.domain.model.WifiInfo
import javax.inject.Inject

data class NetworkUiState(
    val wifiInfo: WifiInfo           = WifiInfo(),
    val devices: List<NetworkDevice> = emptyList(),
    val isScanning: Boolean          = false,
    val scanProgress: Int            = 0,
    val scanTotal: Int               = 254,
    val publicIp: String             = "—",
    val internetOk: Boolean          = false,
    val error: String?               = null,
)

@HiltViewModel
class NetworkViewModel @Inject constructor(
    private val repo: NetworkRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NetworkUiState())
    val state: StateFlow<NetworkUiState> = _state.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    wifiInfo    = repo.getWifiInfo(),
                    publicIp    = repo.getPublicIp(),
                    internetOk  = repo.isInternetAvailable(),
                )
            }
        }
    }

    fun startScan() {
        if (_state.value.isScanning) return
        viewModelScope.launch {
            _state.update { it.copy(isScanning = true, devices = emptyList(), scanProgress = 0) }
            try {
                val devices = repo.scanNetwork { done, total ->
                    _state.update { it.copy(scanProgress = done, scanTotal = total) }
                }
                _state.update { it.copy(devices = devices, isScanning = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isScanning = false, error = e.message) }
            }
        }
    }
}
