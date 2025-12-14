package tn.rifq_android.viewmodel.vetsitter

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import tn.rifq_android.data.api.RetrofitInstance
import tn.rifq_android.data.model.auth.AppUser
import tn.rifq_android.data.model.vetsitter.*
import tn.rifq_android.data.storage.TokenManager
import tn.rifq_android.util.JwtDecoder

sealed class VetSitterUiState {
    object Idle : VetSitterUiState()
    object Loading : VetSitterUiState()
    data class Success(val user: AppUser, val message: String, val requiresVerification: Boolean = false) : VetSitterUiState()
    data class VetProfileLoaded(val vet: AppUser) : VetSitterUiState()
    data class SitterProfileLoaded(val sitter: AppUser) : VetSitterUiState()
    data class Error(val message: String) : VetSitterUiState()
}

class JoinVetSitterViewModel(
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<VetSitterUiState>(VetSitterUiState.Idle)
    val uiState: StateFlow<VetSitterUiState> = _uiState

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    val currentUser: StateFlow<AppUser?> = _currentUser

    private val api = RetrofitInstance.vetSitterApi

    companion object {
        private const val TAG = "JoinVetSitterViewModel"
    }

    init {
        loadCurrentUser()
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val token = tokenManager.getAccessToken().firstOrNull()
                if (!token.isNullOrEmpty()) {
                    val userId = JwtDecoder.getUserIdFromToken(token)
                    if (!userId.isNullOrEmpty()) {
                        // Fetch user profile to get current user data
                        val response = RetrofitInstance.profileApi.getProfile(userId)
                        if (response.isSuccessful) {
                            val user = response.body()
                            // Convert User to AppUser
                            if (user != null) {
                                _currentUser.value = AppUser(
                                    id = user.id ?: "",
                                    email = user.email,
                                    name = user.name,
                                    role = user.role,
                                    phone = user.phone,
                                    country = user.country,
                                    city = user.city,
                                    avatarUrl = user.profileImage
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load current user", e)
            }
        }
    }

    /**
     * Register new veterinarian or convert existing user
     */
    fun submitVet(
        fullName: String,
        email: String,
        phoneNumber: String?,
        licenseNumber: String,
        clinicName: String,
        clinicAddress: String,
        specializations: List<String>,
        yearsOfExperience: Int?,
        latitude: Double?,
        longitude: Double?,
        bio: String?,
        password: String? = null // Only for new registration
    ) {
        viewModelScope.launch {
            _uiState.value = VetSitterUiState.Loading
            try {
                val currentUser = _currentUser.value

                if (currentUser != null) {
                    // User is logged in - Convert existing user to vet
                    val convertRequest = ConvertVetRequest(
                        email = currentUser.email,
                        licenseNumber = licenseNumber.trim(),
                        clinicName = clinicName.trim(),
                        clinicAddress = clinicAddress.trim(),
                        specializations = specializations.ifEmpty { null },
                        yearsOfExperience = yearsOfExperience,
                        latitude = latitude ?: 0.0,
                        longitude = longitude ?: 0.0,
                        bio = bio?.trim()?.ifEmpty { null }
                    )

                    val response = api.convertUserToVet(currentUser.id, convertRequest)

                    if (response.isSuccessful) {
                        val updatedUser = response.body()
                        if (updatedUser != null) {
                            // Always require email verification when converting to vet (matches iOS)
                            _uiState.value = VetSitterUiState.Success(
                                updatedUser,
                                "Successfully converted to veterinarian! Please verify your email.",
                                requiresVerification = true
                            )
                        } else {
                            _uiState.value = VetSitterUiState.Error("No response from server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        _uiState.value = VetSitterUiState.Error(
                            errorBody ?: "Failed to convert to veterinarian"
                        )
                    }
                } else {
                    // User is not logged in - Register new vet account
                    if (password.isNullOrEmpty()) {
                        _uiState.value = VetSitterUiState.Error("Password is required")
                        return@launch
                    }

                    val createRequest = CreateVetRequest(
                        email = email.trim().lowercase(),
                        name = fullName.trim(),
                        password = password,
                        phoneNumber = phoneNumber?.trim()?.ifEmpty { null },
                        licenseNumber = licenseNumber.trim(),
                        clinicName = clinicName.trim(),
                        clinicAddress = clinicAddress.trim(),
                        specializations = specializations.ifEmpty { null },
                        yearsOfExperience = yearsOfExperience,
                        latitude = latitude,
                        longitude = longitude,
                        bio = bio?.trim()?.ifEmpty { null }
                    )

                    val response = api.registerVet(createRequest)
                    
                    if (response.isSuccessful) {
                        val newUser = response.body()
                        if (newUser != null) {
                            _uiState.value = VetSitterUiState.Success(
                                newUser,
                                "Vet account created! Please verify your email.",
                                requiresVerification = true
                            )
                        } else {
                            _uiState.value = VetSitterUiState.Error("No response from server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        _uiState.value = VetSitterUiState.Error(
                            errorBody ?: "Failed to create vet account"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error submitting vet form", e)
                _uiState.value = VetSitterUiState.Error(
                    e.message ?: "Network error. Please try again."
                )
            }
        }
    }

    /**
     * Register new sitter or convert existing user
     */
    fun submitSitter(
        fullName: String,
        email: String,
        phoneNumber: String?,
        hourlyRate: Double,
        sitterAddress: String,
        services: List<String>,
        yearsOfExperience: Int?,
        availableWeekends: Boolean?,
        canHostPets: Boolean?,
        availability: List<String>?,
        latitude: Double?,
        longitude: Double?,
        bio: String?,
        password: String? = null // Only for new registration
    ) {
        viewModelScope.launch {
            _uiState.value = VetSitterUiState.Loading
            try {
                val currentUser = _currentUser.value

                if (currentUser != null) {
                    // User is logged in - Convert existing user to sitter
                    val convertRequest = ConvertSitterRequest(
                        hourlyRate = hourlyRate,
                        sitterAddress = sitterAddress.trim(),
                        services = services.ifEmpty { null },
                        yearsOfExperience = yearsOfExperience,
                        availableWeekends = availableWeekends,
                        canHostPets = canHostPets,
                        availability = availability?.ifEmpty { null },
                        latitude = latitude,
                        longitude = longitude,
                        bio = bio?.trim()?.ifEmpty { null }
                    )

                    val response = api.convertUserToSitter(currentUser.id, convertRequest)

                    if (response.isSuccessful) {
                        val updatedUser = response.body()
                        if (updatedUser != null) {
                            _uiState.value = VetSitterUiState.Success(
                                updatedUser,
                                "Successfully converted to pet sitter! Please verify your email.",
                                requiresVerification = true
                            )
                        } else {
                            _uiState.value = VetSitterUiState.Error("No response from server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        _uiState.value = VetSitterUiState.Error(
                            errorBody ?: "Failed to convert to pet sitter"
                        )
                    }
                } else {
                    // User is not logged in - Register new sitter account
                    if (password.isNullOrEmpty()) {
                        _uiState.value = VetSitterUiState.Error("Password is required")
                        return@launch
                    }

                    val createRequest = CreateSitterRequest(
                        email = email.trim().lowercase(),
                        name = fullName.trim(),
                        password = password,
                        phoneNumber = phoneNumber?.trim()?.ifEmpty { null },
                        hourlyRate = hourlyRate,
                        sitterAddress = sitterAddress.trim(),
                        services = services.ifEmpty { null },
                        yearsOfExperience = yearsOfExperience,
                        availableWeekends = availableWeekends,
                        canHostPets = canHostPets,
                        availability = availability?.ifEmpty { null },
                        latitude = latitude,
                        longitude = longitude,
                        bio = bio?.trim()?.ifEmpty { null }
                    )

                    val response = api.registerSitter(createRequest)
                    
                    if (response.isSuccessful) {
                        val newUser = response.body()
                        if (newUser != null) {
                            _uiState.value = VetSitterUiState.Success(
                                newUser,
                                "Sitter account created! Please verify your email.",
                                requiresVerification = true
                            )
                        } else {
                            _uiState.value = VetSitterUiState.Error("No response from server")
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        _uiState.value = VetSitterUiState.Error(
                            errorBody ?: "Failed to create sitter account"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error submitting sitter form", e)
                _uiState.value = VetSitterUiState.Error(
                    e.message ?: "Network error. Please try again."
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = VetSitterUiState.Idle
    }
    
    /**
     * Fetch vet profile for editing
     */
    fun fetchVetProfile() {
        viewModelScope.launch {
            _uiState.value = VetSitterUiState.Loading
            try {
                val token = tokenManager.getAccessToken().firstOrNull()
                if (token.isNullOrEmpty()) {
                    _uiState.value = VetSitterUiState.Error("Not logged in")
                    return@launch
                }
                
                val userId = JwtDecoder.getUserIdFromToken(token)
                if (userId.isNullOrEmpty()) {
                    _uiState.value = VetSitterUiState.Error("Invalid session")
                    return@launch
                }
                
                val response = api.getVet(userId)
                if (response.isSuccessful) {
                    val vet = response.body()
                    if (vet != null) {
                        _uiState.value = VetSitterUiState.VetProfileLoaded(vet)
                    } else {
                        _uiState.value = VetSitterUiState.Error("Vet profile not found")
                    }
                } else {
                    _uiState.value = VetSitterUiState.Error("Failed to load vet profile")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching vet profile", e)
                _uiState.value = VetSitterUiState.Error(e.message ?: "Network error")
            }
        }
    }
    
    /**
     * Fetch sitter profile for editing
     */
    fun fetchSitterProfile() {
        viewModelScope.launch {
            _uiState.value = VetSitterUiState.Loading
            try {
                val token = tokenManager.getAccessToken().firstOrNull()
                if (token.isNullOrEmpty()) {
                    _uiState.value = VetSitterUiState.Error("Not logged in")
                    return@launch
                }
                
                val userId = JwtDecoder.getUserIdFromToken(token)
                if (userId.isNullOrEmpty()) {
                    _uiState.value = VetSitterUiState.Error("Invalid session")
                    return@launch
                }
                
                val response = api.getSitter(userId)
                if (response.isSuccessful) {
                    val sitter = response.body()
                    if (sitter != null) {
                        _uiState.value = VetSitterUiState.SitterProfileLoaded(sitter)
                    } else {
                        _uiState.value = VetSitterUiState.Error("Sitter profile not found")
                    }
                } else {
                    _uiState.value = VetSitterUiState.Error("Failed to load sitter profile")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching sitter profile", e)
                _uiState.value = VetSitterUiState.Error(e.message ?: "Network error")
            }
        }
    }
    
    /**
     * Update vet profile
     */
    fun updateVetProfile(
        licenseNumber: String,
        clinicName: String,
        phone: String,
        clinicAddress: String,
        yearsOfExperience: Int?,
        specialties: List<String>,
        bio: String,
        latitude: Double?,
        longitude: Double?
    ) {
        viewModelScope.launch {
            _uiState.value = VetSitterUiState.Loading
            try {
                val token = tokenManager.getAccessToken().firstOrNull()
                if (token.isNullOrEmpty()) {
                    _uiState.value = VetSitterUiState.Error("Not logged in")
                    return@launch
                }
                
                val userId = JwtDecoder.getUserIdFromToken(token)
                if (userId.isNullOrEmpty()) {
                    _uiState.value = VetSitterUiState.Error("Invalid session")
                    return@launch
                }
                
                val updateRequest = UpdateVetRequest(
                    licenseNumber = licenseNumber.trim().ifEmpty { null },
                    clinicName = clinicName.trim().ifEmpty { null },
                    phoneNumber = phone.trim().ifEmpty { null },
                    clinicAddress = clinicAddress.trim().ifEmpty { null },
                    yearsOfExperience = yearsOfExperience,
                    specializations = specialties.ifEmpty { null },
                    bio = bio.trim().ifEmpty { null },
                    latitude = latitude,
                    longitude = longitude
                )
                
                val response = api.updateVet(userId, updateRequest)
                if (response.isSuccessful) {
                    val updatedVet = response.body()
                    if (updatedVet != null) {
                        _uiState.value = VetSitterUiState.Success(
                            updatedVet,
                            "Profile updated successfully"
                        )
                    } else {
                        _uiState.value = VetSitterUiState.Error("No response from server")
                    }
                } else {
                    _uiState.value = VetSitterUiState.Error("Failed to update profile")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating vet profile", e)
                _uiState.value = VetSitterUiState.Error(e.message ?: "Network error")
            }
        }
    }
    
    /**
     * Update sitter profile
     */
    fun updateSitterProfile(
        phone: String,
        sitterAddress: String,
        yearsOfExperience: Int?,
        bio: String,
        hourlyRate: Double?,
        canHostPets: Boolean,
        latitude: Double?,
        longitude: Double?
    ) {
        viewModelScope.launch {
            _uiState.value = VetSitterUiState.Loading
            try {
                val token = tokenManager.getAccessToken().firstOrNull()
                if (token.isNullOrEmpty()) {
                    _uiState.value = VetSitterUiState.Error("Not logged in")
                    return@launch
                }
                
                val userId = JwtDecoder.getUserIdFromToken(token)
                if (userId.isNullOrEmpty()) {
                    _uiState.value = VetSitterUiState.Error("Invalid session")
                    return@launch
                }
                
                val updateRequest = UpdateSitterRequest(
                    phoneNumber = phone.trim().ifEmpty { null },
                    sitterAddress = sitterAddress.trim().ifEmpty { null },
                    yearsOfExperience = yearsOfExperience,
                    bio = bio.trim().ifEmpty { null },
                    hourlyRate = hourlyRate,
                    canHostPets = canHostPets,
                    latitude = latitude,
                    longitude = longitude
                )
                
                val response = api.updateSitter(userId, updateRequest)
                if (response.isSuccessful) {
                    val updatedSitter = response.body()
                    if (updatedSitter != null) {
                        _uiState.value = VetSitterUiState.Success(
                            updatedSitter,
                            "Profile updated successfully"
                        )
                    } else {
                        _uiState.value = VetSitterUiState.Error("No response from server")
                    }
                } else {
                    _uiState.value = VetSitterUiState.Error("Failed to update profile")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating sitter profile", e)
                _uiState.value = VetSitterUiState.Error(e.message ?: "Network error")
            }
        }
    }
}
