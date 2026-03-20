package systems.nzr1.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import systems.nzr1.data.preferences.UserPreferences
import javax.inject.Inject

data class PrefsState(
    val showGrid: Boolean      = true,
    val bootAnimation: Boolean = true,
    val haptics: Boolean       = true,
    val monitorInterval: Long  = 1000L,
    val apiKeySet: Boolean     = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferences,
) : ViewModel() {

    val prefs: StateFlow<PrefsState> = combine(
        prefs.showGrid,
        prefs.bootAnimation,
        prefs.haptics,
        prefs.monitorInterval,
        prefs.aiApiKey,
    ) { grid, boot, haptic, interval, key ->
        PrefsState(grid, boot, haptic, interval, key.isNotBlank())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PrefsState())

    fun setShowGrid(v: Boolean)         { viewModelScope.launch { this@SettingsViewModel.prefs.setShowGrid(v) } }
    fun setBootAnimation(v: Boolean)    { viewModelScope.launch { this@SettingsViewModel.prefs.setBootAnimation(v) } }
    fun setHaptics(v: Boolean)          { viewModelScope.launch { this@SettingsViewModel.prefs.setHaptics(v) } }
    fun setMonitorInterval(ms: Long)    { viewModelScope.launch { this@SettingsViewModel.prefs.setMonitorInterval(ms) } }
}
