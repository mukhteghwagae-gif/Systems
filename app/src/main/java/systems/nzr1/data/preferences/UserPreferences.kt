package systems.nzr1.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "systems_prefs")

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME_MODE       = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR     = stringPreferencesKey("accent_color")
        val AI_API_KEY       = stringPreferencesKey("ai_api_key")
        val SCAN_INTERVAL    = intPreferencesKey("scan_interval_ms")
        val BOOT_ANIMATION   = booleanPreferencesKey("boot_animation")
        val HAPTICS          = booleanPreferencesKey("haptics")
        val SHOW_GRID        = booleanPreferencesKey("show_grid")
        val MONITOR_INTERVAL = longPreferencesKey("monitor_interval_ms")
    }

    val themeMode: Flow<String>        = pref(Keys.THEME_MODE, "dark")
    val accentColor: Flow<String>      = pref(Keys.ACCENT_COLOR, "cyan")
    val aiApiKey: Flow<String>         = pref(Keys.AI_API_KEY, "")
    val scanInterval: Flow<Int>        = pref(Keys.SCAN_INTERVAL, 1000)
    val bootAnimation: Flow<Boolean>   = pref(Keys.BOOT_ANIMATION, true)
    val haptics: Flow<Boolean>         = pref(Keys.HAPTICS, true)
    val showGrid: Flow<Boolean>        = pref(Keys.SHOW_GRID, true)
    val monitorInterval: Flow<Long>    = pref(Keys.MONITOR_INTERVAL, 1000L)

    suspend fun setAiApiKey(key: String)           = set(Keys.AI_API_KEY, key)
    suspend fun setBootAnimation(enabled: Boolean) = set(Keys.BOOT_ANIMATION, enabled)
    suspend fun setHaptics(enabled: Boolean)       = set(Keys.HAPTICS, enabled)
    suspend fun setShowGrid(enabled: Boolean)      = set(Keys.SHOW_GRID, enabled)
    suspend fun setAccentColor(color: String)      = set(Keys.ACCENT_COLOR, color)
    suspend fun setMonitorInterval(ms: Long)       = set(Keys.MONITOR_INTERVAL, ms)

    private fun <T> pref(key: Preferences.Key<T>, default: T): Flow<T> =
        context.dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { it[key] ?: default }

    private suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { it[key] = value }
    }
}
