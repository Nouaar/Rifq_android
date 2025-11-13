package tn.rifq_android.viewmodel.pet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tn.rifq_android.data.api.RetrofitInstance
import tn.rifq_android.data.repository.PetsRepository
import tn.rifq_android.data.storage.TokenManager

class PetDetailViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PetDetailViewModel::class.java)) {
            val repository = PetsRepository(RetrofitInstance.petsApi)
            val tokenManager = TokenManager(context.applicationContext)
            @Suppress("UNCHECKED_CAST")
            return PetDetailViewModel(repository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

