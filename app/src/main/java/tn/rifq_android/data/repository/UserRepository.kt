package tn.rifq_android.data.repository

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import tn.rifq_android.data.api.UserApi
import java.io.File

class UserRepository(private val api: UserApi) {

    suspend fun getProfile() = api.getProfile()

    suspend fun updateProfile(
        name: String? = null,
        email: String? = null,
        photoFile: File? = null
    ) = api.updateProfile(
        name = name?.toRequestBody("text/plain".toMediaTypeOrNull()),
        email = email?.toRequestBody("text/plain".toMediaTypeOrNull()),
        photo = photoFile?.let {
            val requestBody = it.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData("photo", it.name, requestBody)
        }
    )

    suspend fun deleteAccount() = api.deleteAccount()
}

