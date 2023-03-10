package com.muhammed.sword.weather.ui.weather

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.muhammed.sword.weather.domain.location.LocationTracker
import com.muhammed.sword.weather.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherState())
    val uiState: StateFlow<WeatherState> = _uiState
    private var isLoading = false // to prevent concurrent api requests.

    fun getWeatherData() {
        if (!isLoading) {
            isLoading = true
            _uiState.value = WeatherState(isLoading = isLoading)
            viewModelScope.launch(Dispatchers.IO) {
                locationTracker.getCurrentLocation()?.let { location ->
                    val now = LocalDateTime.now()
                    val todayDate = now.toLocalDate().toString()
                    val plusSevenDays = now.plusDays(7).toLocalDate().toString()
                    repository.getWeatherData(
                        location.latitude,
                        location.longitude,
                        todayDate,
                        plusSevenDays
                    )
                        .catch {
                            _uiState.value = WeatherState(error = it.message, isLoading = false)
                            getOfflineData()
                        }
                        .collect {
                            if (it.data == null) {
                                getOfflineData()
                            } else {
                                _uiState.value = WeatherState(
                                    data = it.data,
                                    isOffline = false,
                                    isLoading = false
                                )
                            }
                        }
                }
                isLoading = false
            }
        }
    }

    fun getOfflineData() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getOfflineData().collect {
                it?.let { weatherInfo ->
                    _uiState.value =
                        WeatherState(data = weatherInfo, isOffline = true, isLoading = true)
                }
            }
        }
    }
}