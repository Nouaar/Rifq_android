package tn.rifq_android.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import tn.rifq_android.ui.theme.PetAvatarBrown
import tn.rifq_android.ui.theme.PetAvatarTan
import tn.rifq_android.util.Constants

/**
 * Utility functions for pet-related UI operations
 * Centralizes pet emoji and color logic to avoid duplication
 */
object PetUtils {

    /**
     * Get emoji representation for a pet species
     * @param species The species name (dog, cat, bird, etc.)
     * @return Emoji string representing the species
     */
    fun getPetEmoji(species: String): String {
        return when (species.lowercase()) {
            Constants.Pet.Species.DOG -> "🐕"
            Constants.Pet.Species.CAT -> "🐈"
            Constants.Pet.Species.BIRD -> "🐦"
            Constants.Pet.Species.FISH -> "🐠"
            Constants.Pet.Species.RABBIT -> "🐰"
            Constants.Pet.Species.HAMSTER -> "🐹"
            else -> "🐾"
        }
    }

    /**
     * Get color for pet avatar based on species
     * Theme-aware - adapts to light/dark mode through theme colors
     * @param species The species name
     * @return Color for the pet avatar background
     */
    @Composable
    fun getPetColor(species: String): Color {
        return when (species.lowercase()) {
            Constants.Pet.Species.DOG -> PetAvatarBrown
            Constants.Pet.Species.CAT -> PetAvatarTan
            Constants.Pet.Species.BIRD -> Color(0xFFADD8E6) // Light Blue
            Constants.Pet.Species.FISH -> Color(0xFF87CEEB) // Sky Blue
            Constants.Pet.Species.RABBIT -> Color(0xFFFFB6C1) // Light Pink
            Constants.Pet.Species.HAMSTER -> Color(0xFFFFA07A) // Light Salmon
            else -> PetAvatarBrown
        }
    }

    /**
     * Format pet age display
     * @param age Age in years
     * @return Formatted age string (e.g., "3 years", "1 year")
     */
    fun formatAge(age: Int?): String {
        return age?.let {
            if (it == 1) "$it year" else "$it years"
        } ?: "Unknown age"
    }

    /**
     * Format pet info display
     * @param breed Optional breed
     * @param species Species (fallback if breed is null)
     * @param age Optional age
     * @return Formatted info string (e.g., "Golden Retriever • 3 years")
     */
    fun formatPetInfo(breed: String?, species: String, age: Int?): String {
        val breedOrSpecies = breed ?: species.replaceFirstChar { it.uppercase() }
        val ageText = age?.let { " • ${formatAge(it)}" } ?: ""
        return "$breedOrSpecies$ageText"
    }

    /**
     * Format weight display
     * @param weight Weight in kg
     * @return Formatted weight string
     */
    fun formatWeight(weight: Double?): String {
        return weight?.let { "$it kg" } ?: "N/A"
    }

    /**
     * Format height display
     * @param height Height in cm
     * @return Formatted height string
     */
    fun formatHeight(height: Double?): String {
        return height?.let { "$it cm" } ?: "N/A"
    }
}

