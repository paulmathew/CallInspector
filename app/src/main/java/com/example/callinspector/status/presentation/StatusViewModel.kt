package com.example.callinspector.status.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.callinspector.status.data.model.ServiceStatus
import com.example.callinspector.status.data.repository.StatusRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatusViewModel @Inject constructor(
    private val repository: StatusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatusUiState>(StatusUiState.Loading)
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()

    init {
        loadStatuses()
    }

    fun loadStatuses() {
        viewModelScope.launch {
            _uiState.value = StatusUiState.Loading
            try {
                val data = repository.getStatuses()
                _uiState.value = StatusUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = StatusUiState.Error("Failed to load status")
            }
        }
    }
}

sealed class StatusUiState {
    data object Loading : StatusUiState()
    data class Success(val services: List<ServiceStatus>) : StatusUiState()
    data class Error(val message: String) : StatusUiState()
}