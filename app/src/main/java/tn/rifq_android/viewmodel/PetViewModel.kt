package tn.rifq_android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import tn.rifq_android.data.model.pet.AddPetRequest
import tn.rifq_android.data.model.pet.Pet
import tn.rifq_android.data.repository.PetsRepository
import tn.rifq_android.data.storage.TokenManager
import tn.rifq_android.util.JwtDecoder

sealed class PetUiState {
    object Idle : PetUiState()
    object Loading : PetUiState()
    data class Success(val pet: Pet? = null, val message: String = "Success") : PetUiState()
    data class Error(val message: String) : PetUiState()
}

class PetViewModel(
    private val repository: PetsRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PetUiState>(PetUiState.Idle)
    val uiState: StateFlow<PetUiState> = _uiState

    private suspend fun getUserIdFromToken(): String? {
        val token = tokenManager.getAccessToken().firstOrNull()
        return if (token.isNullOrBlank()) {
            null
        } else {
            JwtDecoder.getUserIdFromToken(token)
        }
    }

    fun addPet(
        name: String,
        species: String,
        breed: String? = null,
        age: Int? = null,
        gender: String? = null,
        color: String? = null,
        weight: Double? = null,
        height: Double? = null,
        photo: String? = null,
        microchipId: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = PetUiState.Loading
            try {
                val userId = getUserIdFromToken()
                if (userId.isNullOrBlank()) {
                    _uiState.value = PetUiState.Error("User not authenticated. Please login again.")
                    return@launch
                }

                val request = AddPetRequest(
                    name = name,
                    species = species,
                    breed = breed,
                    age = age,
                    gender = gender,
                    color = color,
                    weight = weight,
                    height = height,
                    photo = photo,
                    microchipId = microchipId
                )

                val response = repository.addPet(userId, request)
                if (response.isSuccessful) {
                    val pet = response.body()
                    _uiState.value = PetUiState.Success(pet, "Pet added successfully!")
                } else {
                    _uiState.value = PetUiState.Error(
                        response.errorBody()?.string() ?: "Failed to add pet"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = PetUiState.Error(
                    e.message ?: "Network error. Please try again."
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = PetUiState.Idle
    }
}

