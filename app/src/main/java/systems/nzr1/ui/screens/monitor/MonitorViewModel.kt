package systems.nzr1.ui.screens.monitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import systems.nzr1.data.repository.SystemRepository
import systems.nzr1.domain.model.SystemStats
import javax.inject.Inject

@HiltViewModel
class MonitorViewModel @Inject constructor(repo: SystemRepository) : ViewModel() {
    val stats: StateFlow<SystemStats> = repo.statsFlow(500L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SystemStats())
}
