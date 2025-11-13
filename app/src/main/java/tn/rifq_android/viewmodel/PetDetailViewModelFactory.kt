package tn.rifq_android.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tn.rifq_android.data.api.RetrofitInstance
import tn.rifq_android.data.repository.PetsRepository

class PetDetailViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PetDetailViewModel::class.java)) {
            val repository = PetsRepository(RetrofitInstance.petsApi)
            @Suppress("UNCHECKED_CAST")
            return PetDetailViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

