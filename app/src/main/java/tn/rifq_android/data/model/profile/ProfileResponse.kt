package tn.rifq_android.data.model.profile

import tn.rifq_android.data.model.auth.User
import tn.rifq_android.data.model.pet.Pet

data class ProfileResponse(
    val user: User,
    val pets: List<Pet>
)

