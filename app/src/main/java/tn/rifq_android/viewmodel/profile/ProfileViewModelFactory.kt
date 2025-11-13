package tn.rifq_android.viewmodel.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tn.rifq_android.data.api.RetrofitInstance
import tn.rifq_android.data.repository.ProfileRepository
import tn.rifq_android.data.repository.PetsRepository
import tn.rifq_android.data.repository.UserRepository
import tn.rifq_android.data.storage.TokenManager
import tn.rifq_android.data.storage.UserManager

class ProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    private val profileRepository = ProfileRepository(RetrofitInstance.profileApi)
    private val petsRepository = PetsRepository(RetrofitInstance.petsApi)
    private val userRepository = UserRepository(RetrofitInstance.userApi)
    private val tokenManager = TokenManager(context.applicationContext)
    private val userManager = UserManager(context.applicationContext)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                profileRepository,
                petsRepository,
                userRepository,
                tokenManager,
                userManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

