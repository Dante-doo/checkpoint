package br.edu.checkpoint.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.checkpoint.data.settings.AppSettings
import br.edu.checkpoint.data.settings.MapTypeSetting
import br.edu.checkpoint.data.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repo: SettingsRepository
) : ViewModel() {

    val uiState: StateFlow<AppSettings> = repo.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AppSettings()
    )

    fun setDefaultZoom(zoom: Float) = viewModelScope.launch {
        repo.updateDefaultZoom(zoom)
    }

    fun setMapType(type: MapTypeSetting) = viewModelScope.launch {
        repo.updateMapType(type)
    }

    fun setUseDeviceGeocoder(use: Boolean) = viewModelScope.launch {
        repo.updateUseDeviceGeocoder(use)
    }
}
