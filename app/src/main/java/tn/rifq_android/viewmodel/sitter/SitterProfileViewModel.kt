package tn.rifq_android.viewmodel.sitter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.rifq_android.data.api.RetrofitInstance
import tn.rifq_android.data.model.auth.AppUser

class SitterProfileViewModel : ViewModel() {

    private val _sitter = MutableStateFlow<AppUser?>(null)
    val sitter: StateFlow<AppUser?> = _sitter.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val vetSitterApi = RetrofitInstance.vetSitterApi

    fun loadSitter(sitterId: String) {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val sitterData = vetSitterApi.getSitter(sitterId)
                _sitter.value = sitterData
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load sitter profile"
                println("❌ Failed to load sitter profile: $e")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retry(sitterId: String) {
        loadSitter(sitterId)
    }
}
