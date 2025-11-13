package tn.rifq_android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tn.rifq_android.data.model.pet.Pet
import tn.rifq_android.data.repository.PetsRepository

sealed class PetDetailUiState {
    object Idle : PetDetailUiState()
    object Loading : PetDetailUiState()
    data class Success(val pet: Pet) : PetDetailUiState()
    data class Error(val message: String) : PetDetailUiState()
}

class PetDetailViewModel(
    private val repository: PetsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PetDetailUiState>(PetDetailUiState.Idle)
    val uiState: StateFlow<PetDetailUiState> = _uiState

    fun loadPetDetails(petId: String) {
        viewModelScope.launch {
            _uiState.value = PetDetailUiState.Loading
            try {
                val response = repository.getPetById(petId)
                if (response.isSuccessful) {
                    val pet = response.body()
                    if (pet != null) {
                        _uiState.value = PetDetailUiState.Success(pet)
                    } else {
                        _uiState.value = PetDetailUiState.Error("Pet not found")
                    }
                } else {
                    _uiState.value = PetDetailUiState.Error(
                        response.errorBody()?.string() ?: "Failed to load pet details"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = PetDetailUiState.Error(
                    e.message ?: "Network error. Please try again."
                )
            }
        }
    }
}

