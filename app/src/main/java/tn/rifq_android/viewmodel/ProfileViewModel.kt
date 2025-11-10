package tn.rifq_android.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import tn.rifq_android.data.repository.ProfileRepository
import tn.rifq_android.data.storage.TokenManager
import tn.rifq_android.data.model.*
import tn.rifq_android.util.JwtDecoder

sealed class ProfileUiState {
    object Idle : ProfileUiState()
    object Loading : ProfileUiState()
    data class Success(val user: User, val pets: List<Pet>) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

sealed class ProfileAction {
    object Idle : ProfileAction()
    object Loading : ProfileAction()
    data class Success(val message: String) : ProfileAction()
    data class Error(val message: String) : ProfileAction()
}

class ProfileViewModel(
    private val repository: ProfileRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Idle)
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val _actionState = MutableStateFlow<ProfileAction>(ProfileAction.Idle)
    val actionState: StateFlow<ProfileAction> = _actionState

    init {
        loadProfile()
    }

    private suspend fun getUserIdFromToken(): String? {
        val token = tokenManager.getAccessToken().firstOrNull()
        return if (token.isNullOrBlank()) {
            null
        } else {
            JwtDecoder.getUserIdFromToken(token)
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val userId = getUserIdFromToken()
                if (userId.isNullOrBlank()) {
                    _uiState.value = ProfileUiState.Error("User not authenticated. Please login again.")
                    return@launch
                }

                val response = repository.getProfile(userId)
                if (response.isSuccessful) {
                    val user = response.body()
                    if (user != null) {
                        _uiState.value = ProfileUiState.Success(
                            user = user,
                            pets = user.pets ?: emptyList()
                        )
                    } else {
                        _uiState.value = ProfileUiState.Error("No profile data received")
                    }
                } else {
                    _uiState.value = ProfileUiState.Error(
                        response.errorBody()?.string() ?: "Failed to load profile"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(
                    e.message ?: "Network error. Please try again."
                )
            }
        }
    }

    fun updateProfile(name: String, phone: String?) {
        viewModelScope.launch {
            _actionState.value = ProfileAction.Loading
            try {
                val userId = getUserIdFromToken()
                if (userId.isNullOrBlank()) {
                    _actionState.value = ProfileAction.Error("User not authenticated")
                    return@launch
                }

                val response = repository.updateProfile(
                    userId,
                    UpdateProfileRequest(name = name, phone = phone)
                )
                if (response.isSuccessful) {
                    _actionState.value = ProfileAction.Success("Profile updated successfully")
                    loadProfile()
                } else {
                    _actionState.value = ProfileAction.Error(
                        response.errorBody()?.string() ?: "Failed to update profile"
                    )
                }
            } catch (e: Exception) {
                _actionState.value = ProfileAction.Error(
                    e.message ?: "Network error. Please try again."
                )
            }
        }
    }

    fun addPet(name: String, type: String, breed: String, age: Int, description: String? = null) {
        viewModelScope.launch {
            _actionState.value = ProfileAction.Loading
            try {
                val userId = getUserIdFromToken()
                if (userId.isNullOrBlank()) {
                    _actionState.value = ProfileAction.Error("User not authenticated")
                    return@launch
                }

                val response = repository.addPet(
                    userId,
                    AddPetRequest(
                        name = name,
                        type = type,
                        breed = breed,
                        age = age,
                        description = description
                    )
                )
                if (response.isSuccessful) {
                    _actionState.value = ProfileAction.Success("Pet added successfully")
                    loadProfile()
                } else {
                    _actionState.value = ProfileAction.Error(
                        response.errorBody()?.string() ?: "Failed to add pet"
                    )
                }
            } catch (e: Exception) {
                _actionState.value = ProfileAction.Error(
                    e.message ?: "Network error. Please try again."
                )
            }
        }
    }

    fun updatePet(petId: String, name: String, type: String, breed: String, age: Int, description: String? = null) {
        viewModelScope.launch {
            _actionState.value = ProfileAction.Loading
            try {
                val response = repository.updatePet(
                    petId,
                    UpdatePetRequest(
                        name = name,
                        type = type,
                        breed = breed,
                        age = age,
                        description = description
                    )
                )
                if (response.isSuccessful) {
                    _actionState.value = ProfileAction.Success("Pet updated successfully")
                    loadProfile()
                } else {
                    _actionState.value = ProfileAction.Error(
                        response.errorBody()?.string() ?: "Failed to update pet"
                    )
                }
            } catch (e: Exception) {
                _actionState.value = ProfileAction.Error(
                    e.message ?: "Network error. Please try again."
                )
            }
        }
    }

    fun deletePet(petId: String) {
        viewModelScope.launch {
            _actionState.value = ProfileAction.Loading
            try {
                val userId = getUserIdFromToken()
                if (userId.isNullOrBlank()) {
                    _actionState.value = ProfileAction.Error("User not authenticated")
                    return@launch
                }

                val response = repository.deletePet(userId, petId)
                if (response.isSuccessful) {
                    _actionState.value = ProfileAction.Success("Pet deleted successfully")
                    loadProfile()
                } else {
                    _actionState.value = ProfileAction.Error(
                        response.errorBody()?.string() ?: "Failed to delete pet"
                    )
                }
            } catch (e: Exception) {
                _actionState.value = ProfileAction.Error(
                    e.message ?: "Network error. Please try again."
                )
            }
        }
    }

    fun resetActionState() {
        _actionState.value = ProfileAction.Idle
    }
}

