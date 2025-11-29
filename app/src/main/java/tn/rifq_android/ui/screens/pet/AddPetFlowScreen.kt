package tn.rifq_android.ui.screens.pet

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import tn.rifq_android.ui.components.TopNavBar
import tn.rifq_android.ui.theme.*
import tn.rifq_android.viewmodel.pet.PetViewModel
import tn.rifq_android.viewmodel.pet.PetViewModelFactory
import tn.rifq_android.viewmodel.pet.PetUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPetFlowScreen(navController: NavHostController) {
    val context = LocalContext.current
    val viewModel: PetViewModel = viewModel(factory = PetViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()

    var currentStep by remember { mutableStateOf(AddPetStep.PET_INFO) }
    var isSaving by remember { mutableStateOf(false) }


    var petName by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Dog") }
    var petBreed by remember { mutableStateOf("") }
    var petGender by remember { mutableStateOf("Male") }
    var petBirthDate by remember { mutableStateOf(Date()) }
    var petWeight by remember { mutableStateOf("") }
    var petHeight by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageFile by remember { mutableStateOf<java.io.File?>(null) }


    var vaccinations by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var conditions by remember { mutableStateOf("") }
    var medications by remember { mutableStateOf("") }


    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedImageUri = uri
        uri?.let {
            val file = tn.rifq_android.util.ImageFileHelper.uriToFile(context, it)
            selectedImageFile = file
        }
    }


    LaunchedEffect(uiState) {
        if (uiState is PetUiState.Success) {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = PageBackground,
        topBar = {
            TopNavBar(
                title = "Add New Pet",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            StepIndicator(
                currentStep = currentStep,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )


            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState.ordinal > initialState.ordinal) {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith
                                    slideOutHorizontally { it } + fadeOut()
                        }
                    },
                    label = "stepTransition"
                ) { step ->
                    when (step) {
                        AddPetStep.PET_INFO -> PetInfoStep(
                            name = petName,
                            onNameChange = { petName = it },
                            type = petType,
                            onTypeChange = { petType = it },
                            breed = petBreed,
                            onBreedChange = { petBreed = it },
                            gender = petGender,
                            onGenderChange = { petGender = it },
                            birthDate = petBirthDate,
                            onBirthDateChange = { petBirthDate = it },
                            weight = petWeight,
                            onWeightChange = { petWeight = it },
                            height = petHeight,
                            onHeightChange = { petHeight = it },
                            imageUri = selectedImageUri,
                            onImagePick = {
                                imagePicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )
                        AddPetStep.MEDICAL_INFO -> MedicalInfoStep(
                            vaccinations = vaccinations,
                            onVaccinationsChange = { vaccinations = it },
                            allergies = allergies,
                            onAllergiesChange = { allergies = it },
                            conditions = conditions,
                            onConditionsChange = { conditions = it },
                            medications = medications,
                            onMedicationsChange = { medications = it }
                        )
                        AddPetStep.REVIEW -> ReviewStep(
                            name = petName,
                            type = petType,
                            breed = petBreed,
                            gender = petGender,
                            birthDate = petBirthDate,
                            weight = petWeight,
                            height = petHeight,
                            vaccinations = vaccinations,
                            allergies = allergies,
                            conditions = conditions,
                            medications = medications,
                            imageUri = selectedImageUri
                        )
                    }
                }
            }


            BottomNavigation(
                currentStep = currentStep,
                canGoNext = canProceed(currentStep, petName, petType),
                isSaving = isSaving,
                onBack = {
                    currentStep = when (currentStep) {
                        AddPetStep.MEDICAL_INFO -> AddPetStep.PET_INFO
                        AddPetStep.REVIEW -> AddPetStep.MEDICAL_INFO
                        else -> currentStep
                    }
                },
                onNext = {
                    currentStep = when (currentStep) {
                        AddPetStep.PET_INFO -> AddPetStep.MEDICAL_INFO
                        AddPetStep.MEDICAL_INFO -> AddPetStep.REVIEW
                        else -> currentStep
                    }
                },
                onSave = {
                    isSaving = true
                    viewModel.addPet(
                        name = petName,
                        species = petType,
                        breed = petBreed.ifBlank { null },
                        age = calculateAge(petBirthDate),
                        gender = petGender,
                        color = null,
                        weight = petWeight.toDoubleOrNull(),
                        height = petHeight.toDoubleOrNull(),
                        microchipId = null,
                        photoFile = selectedImageFile
                    )
                }
            )
        }
    }
}

enum class AddPetStep {
    PET_INFO, MEDICAL_INFO, REVIEW
}

@Composable
private fun StepIndicator(currentStep: AddPetStep, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AddPetStep.entries.forEach { step ->
            val isActive = step.ordinal <= currentStep.ordinal
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        if (isActive) VetCanyon else VetStroke.copy(alpha = 0.3f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

@Composable
private fun PetInfoStep(
    name: String,
    onNameChange: (String) -> Unit,
    type: String,
    onTypeChange: (String) -> Unit,
    breed: String,
    onBreedChange: (String) -> Unit,
    gender: String,
    onGenderChange: (String) -> Unit,
    birthDate: Date,
    onBirthDateChange: (Date) -> Unit,
    weight: String,
    onWeightChange: (String) -> Unit,
    height: String,
    onHeightChange: (String) -> Unit,
    imageUri: Uri?,
    onImagePick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }


        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Pet photo",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .border(1.dp, VetStroke, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(VetCanyon.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add photo",
                            tint = VetCanyon,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Button(
                    onClick = onImagePick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = VetCanyon,
                        containerColor = CardBackground
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, VetCanyon),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Import Photo",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }


        item {
            SectionCard(title = "Pet Information") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PetTextField(
                        icon = "🐾",
                        placeholder = "Name",
                        value = name,
                        onValueChange = onNameChange
                    )

                    PetDropdownField(
                        title = "Type",
                        value = type,
                        options = listOf("Dog", "Cat", "Bird", "Rabbit", "Other"),
                        onValueChange = onTypeChange
                    )

                    PetTextField(
                        icon = "🌿",
                        placeholder = "Breed",
                        value = breed,
                        onValueChange = onBreedChange
                    )

                    PetSegmentedControl(
                        title = "Gender",
                        selectedOption = gender,
                        options = listOf("Male", "Female"),
                        onOptionSelected = onGenderChange
                    )

                    PetTextField(
                        icon = "⚖️",
                        placeholder = "Weight (kg)",
                        value = weight,
                        onValueChange = onWeightChange,
                        keyboardType = KeyboardType.Decimal
                    )

                    PetTextField(
                        icon = "📏",
                        placeholder = "Height (cm)",
                        value = height,
                        onValueChange = onHeightChange,
                        keyboardType = KeyboardType.Decimal
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun MedicalInfoStep(
    vaccinations: String,
    onVaccinationsChange: (String) -> Unit,
    allergies: String,
    onAllergiesChange: (String) -> Unit,
    conditions: String,
    onConditionsChange: (String) -> Unit,
    medications: String,
    onMedicationsChange: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            SectionCard(title = "Medical Information") {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PetTextField(
                        icon = "💉",
                        placeholder = "Vaccinations (comma separated)",
                        value = vaccinations,
                        onValueChange = onVaccinationsChange,
                        singleLine = false
                    )

                    PetTextField(
                        icon = "⚠️",
                        placeholder = "Allergies (comma separated)",
                        value = allergies,
                        onValueChange = onAllergiesChange,
                        singleLine = false
                    )

                    PetTextField(
                        icon = "🏥",
                        placeholder = "Conditions (comma separated)",
                        value = conditions,
                        onValueChange = onConditionsChange,
                        singleLine = false
                    )

                    PetTextField(
                        icon = "💊",
                        placeholder = "Medications (name: dosage)",
                        value = medications,
                        onValueChange = onMedicationsChange,
                        singleLine = false
                    )
                }
            }
        }

        item {
            Text(
                text = "Medical information is optional but helps provide better care for your pet.",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ReviewStep(
    name: String,
    type: String,
    breed: String,
    gender: String,
    birthDate: Date,
    weight: String,
    height: String,
    vaccinations: String,
    allergies: String,
    conditions: String,
    medications: String,
    imageUri: Uri?
) {
    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }


        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = "Pet photo",
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(VetCanyon.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🐾", fontSize = 48.sp)
                    }
                }

                Text(
                    text = name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Text(
                    text = "$type${if (breed.isNotBlank()) " • $breed" else ""}",
                    fontSize = 16.sp,
                    color = TextSecondary
                )
            }
        }


        item {
            SectionCard(title = "Basic Information") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReviewRow(label = "Gender", value = gender)
                    ReviewRow(label = "Birth Date", value = dateFormatter.format(birthDate))
                    if (weight.isNotBlank()) ReviewRow(label = "Weight", value = "$weight kg")
                    if (height.isNotBlank()) ReviewRow(label = "Height", value = "$height cm")
                }
            }
        }


        if (vaccinations.isNotBlank() || allergies.isNotBlank() ||
            conditions.isNotBlank() || medications.isNotBlank()) {
            item {
                SectionCard(title = "Medical Information") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (vaccinations.isNotBlank())
                            ReviewRow(label = "Vaccinations", value = vaccinations)
                        if (allergies.isNotBlank())
                            ReviewRow(label = "Allergies", value = allergies)
                        if (conditions.isNotBlank())
                            ReviewRow(label = "Conditions", value = conditions)
                        if (medications.isNotBlank())
                            ReviewRow(label = "Medications", value = medications)
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = BorderStroke(1.dp, VetStroke.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = VetCanyon
            )
            content()
        }
    }
}

@Composable
private fun PetTextField(
    icon: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = TextSecondary) },
        leadingIcon = { Text(text = icon, fontSize = 18.sp) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 3,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VetCanyon,
            unfocusedBorderColor = VetStroke,
            focusedContainerColor = CardBackground,
            unfocusedContainerColor = CardBackground
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
    )
}

@Composable
private fun PetDropdownField(
    title: String,
    value: String,
    options: List<String>,
    onValueChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            label = { Text(title) },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VetCanyon,
                unfocusedBorderColor = VetStroke,
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PetSegmentedControl(
    title: String,
    selectedOption: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                Button(
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) VetCanyon else CardBackground,
                        contentColor = if (isSelected) Color.White else TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = if (!isSelected) BorderStroke(1.dp, VetStroke) else null
                ) {
                    Text(option, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

@Composable
private fun BottomNavigation(
    currentStep: AddPetStep,
    canGoNext: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSave: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (currentStep != AddPetStep.PET_INFO) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(2.dp, VetCanyon),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = VetCanyon),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = "Back",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (currentStep != AddPetStep.REVIEW) {
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = canGoNext,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VetCanyon,
                    disabledContainerColor = VetCanyon.copy(alpha = 0.35f)
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = "Next",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        } else {
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                enabled = !isSaving,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = VetCanyon,
                    disabledContainerColor = VetCanyon.copy(alpha = 0.6f)
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = if (isSaving) "Saving..." else "Save Pet Profile",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

private fun canProceed(step: AddPetStep, name: String, type: String): Boolean {
    return when (step) {
        AddPetStep.PET_INFO -> name.isNotBlank() && type.isNotBlank()
        else -> true
    }
}

private fun calculateAge(birthDate: Date): Double {
    val calendar = Calendar.getInstance()
    val birthCalendar = Calendar.getInstance().apply { time = birthDate }

    val years = calendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)
    val months = calendar.get(Calendar.MONTH) - birthCalendar.get(Calendar.MONTH)
    val days = calendar.get(Calendar.DAY_OF_MONTH) - birthCalendar.get(Calendar.DAY_OF_MONTH)
    
    // Calculate more precise age including months and days
    val totalAge = years + (months / 12.0) + (days / 365.0)
    return if (totalAge < 0) 0.0 else totalAge
}
