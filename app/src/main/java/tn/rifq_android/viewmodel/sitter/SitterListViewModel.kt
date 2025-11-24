package tn.rifq_android.viewmodel.sitter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.rifq_android.data.api.RetrofitInstance
import tn.rifq_android.data.model.auth.AppUser
import tn.rifq_android.data.model.sitter.SitterCard
import tn.rifq_android.ui.theme.VetCanyon

class SitterListViewModel : ViewModel() {

    private val _sitters = MutableStateFlow<List<SitterCard>>(emptyList())
    val sitters: StateFlow<List<SitterCard>> = _sitters.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val vetSitterApi = RetrofitInstance.vetSitterApi

    init {
        loadSitters()
    }

    fun loadSitters() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val appUsers = vetSitterApi.getAllSitters()
                _sitters.value = appUsers.map { mapToSitter(it) }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load sitters"
                println("❌ Failed to load sitters: $e")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun mapToSitter(user: AppUser): SitterCard {

        val displayName = user.name ?: user.email.split("@").firstOrNull() ?: "Pet Sitter"


        val service = user.services?.joinToString(" · ")?.takeIf { it.isNotEmpty() }
            ?: "Pet Sitting Services"


        val location = listOfNotNull(user.city, user.country)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(", ")

        val description = if (location.isEmpty()) {
            user.bio ?: "Professional pet sitter"
        } else {
            "Experienced pet sitter in $location"
        }


        val rating = 4.5

        return SitterCard(
            id = user.id,
            name = displayName,
            service = service,
            description = description,
            rating = rating,
            emoji = "🧑‍🍼",
            tint = VetCanyon.copy(alpha = 0.25f),
            userId = user.id,
            appUser = user
        )
    }
}
