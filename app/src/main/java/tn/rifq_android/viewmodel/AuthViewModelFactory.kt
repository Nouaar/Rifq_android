package tn.rifq_android.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tn.rifq_android.data.api.RetrofitInstance
import tn.rifq_android.data.repository.AuthRepository
import tn.rifq_android.data.storage.TokenManager

class AuthViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    private val repository = AuthRepository(RetrofitInstance.api)
    private val tokenManager = TokenManager(context.applicationContext)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            return AuthViewModel(repository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
