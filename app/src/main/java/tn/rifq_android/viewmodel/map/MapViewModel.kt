package tn.rifq_android.viewmodel.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tn.rifq_android.data.model.auth.AppUser
import tn.rifq_android.data.model.map.VetLocation
import tn.rifq_android.data.model.map.SitterLocation

class MapViewModel : ViewModel() {

    private val _vetLocations = MutableStateFlow<List<VetLocation>>(emptyList())
    val vetLocations: StateFlow<List<VetLocation>> = _vetLocations.asStateFlow()

    private val _sitterLocations = MutableStateFlow<List<SitterLocation>>(emptyList())
    val sitterLocations: StateFlow<List<SitterLocation>> = _sitterLocations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadLocations()
    }

    fun loadLocations() {
        viewModelScope.launch {
            _isLoading.value = true



            _vetLocations.value = listOf(
                VetLocation(
                    id = "vet1",
                    userId = "user_vet1",
                    name = "Clinique Vétérinaire Centrale",
                    address = "Avenue Habib Bourguiba, Tunis",
                    latitude = 36.8065,
                    longitude = 10.1815,
                    isAvailable = true,
                    appUser = AppUser(
                        id = "user_vet1",
                        email = "vet1@vet.tn",
                        name = "Dr. Ahmed Ben Ali",
                        role = "vet",
                        avatarUrl = null
                    )
                ),
                VetLocation(
                    id = "vet2",
                    userId = "user_vet2",
                    name = "Cabinet Vétérinaire Carthage",
                    address = "La Marsa, Tunis",
                    latitude = 36.8780,
                    longitude = 10.3250,
                    isAvailable = true,
                    appUser = AppUser(
                        id = "user_vet2",
                        email = "vet2@vet.tn",
                        name = "Dr. Leila Mansour",
                        role = "vet",
                        avatarUrl = null
                    )
                ),
                VetLocation(
                    id = "vet3",
                    userId = "user_vet3",
                    name = "Clinique des Animaux",
                    address = "Ariana, Tunis",
                    latitude = 36.8625,
                    longitude = 10.1956,
                    isAvailable = false,
                    appUser = AppUser(
                        id = "user_vet3",
                        email = "vet3@vet.tn",
                        name = "Dr. Mohamed Trabelsi",
                        role = "vet",
                        avatarUrl = null
                    )
                )
            )

            _sitterLocations.value = listOf(
                SitterLocation(
                    id = "sitter1",
                    userId = "user_sitter1",
                    name = "Pet Care by Ines",
                    address = "Centre Ville, Tunis",
                    latitude = 36.8008,
                    longitude = 10.1862,
                    isAvailable = true,
                    appUser = AppUser(
                        id = "user_sitter1",
                        email = "sitter1@vet.tn",
                        name = "Ines Gharbi",
                        role = "sitter",
                        avatarUrl = null
                    )
                ),
                SitterLocation(
                    id = "sitter2",
                    userId = "user_sitter2",
                    name = "Happy Paws Pet Sitting",
                    address = "Menzah, Tunis",
                    latitude = 36.8380,
                    longitude = 10.1750,
                    isAvailable = true,
                    appUser = AppUser(
                        id = "user_sitter2",
                        email = "sitter2@vet.tn",
                        name = "Youssef Kamel",
                        role = "sitter",
                        avatarUrl = null
                    )
                ),
                SitterLocation(
                    id = "sitter3",
                    userId = "user_sitter3",
                    name = "Pet Angels",
                    address = "Lac 2, Tunis",
                    latitude = 36.8420,
                    longitude = 10.2280,
                    isAvailable = false,
                    appUser = AppUser(
                        id = "user_sitter3",
                        email = "sitter3@vet.tn",
                        name = "Sarah Khediri",
                        role = "sitter",
                        avatarUrl = null
                    )
                )
            )

            _isLoading.value = false
        }
    }

    fun refreshLocations() {
        loadLocations()
    }
}
