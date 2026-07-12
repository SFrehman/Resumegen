package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.ui.components.JaniDevFooter
import com.example.ui.viewmodel.ResumeViewModel
import com.example.util.ResumeExporter
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResumeBuilderApp(
    viewModel: ResumeViewModel,
    modifier: Modifier = Modifier
) {
    val currentResume by viewModel.currentResume.collectAsState()
    val savedResumes by viewModel.savedResumes.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    var activeTab by remember { mutableStateOf("saved") } // "saved", "builder", "preview", "about"

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 4.dp,
                color = MaterialTheme.colorScheme.background
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(10.dp))
                                .testTag("app_logo"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "App Logo",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "ResumeGen",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "High Density Builder",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Theme Toggle button
                        IconButton(
                            onClick = { viewModel.toggleTheme() },
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Dark Mode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = activeTab == "saved",
                    onClick = { activeTab = "saved" },
                    icon = { Icon(Icons.Default.Folder, contentDescription = "Saved Resumes") },
                    label = { Text("Resumes") },
                    modifier = Modifier.testTag("nav_tab_saved")
                )
                NavigationBarItem(
                    selected = activeTab == "builder",
                    onClick = { activeTab = "builder" },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Form Builder") },
                    label = { Text("Builder") },
                    modifier = Modifier.testTag("nav_tab_builder")
                )
                NavigationBarItem(
                    selected = activeTab == "preview",
                    onClick = { activeTab = "preview" },
                    icon = { Icon(Icons.Default.Visibility, contentDescription = "Resume Preview") },
                    label = { Text("Preview") },
                    modifier = Modifier.testTag("nav_tab_preview")
                )
                NavigationBarItem(
                    selected = activeTab == "about",
                    onClick = { activeTab = "about" },
                    icon = { Icon(Icons.Default.Info, contentDescription = "About JaniDev") },
                    label = { Text("About") },
                    modifier = Modifier.testTag("nav_tab_about")
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeTab) {
                "saved" -> SavedResumesScreen(
                    resumes = savedResumes,
                    onSelect = { resume ->
                        viewModel.loadResume(resume)
                        activeTab = "preview"
                    },
                    onEdit = { resume ->
                        viewModel.loadResume(resume)
                        activeTab = "builder"
                    },
                    onDelete = { resume ->
                        viewModel.deleteResume(resume)
                    },
                    onCreateNew = {
                        viewModel.createNewResume()
                        activeTab = "builder"
                    }
                )

                "builder" -> ResumeWizardScreen(
                    resume = currentResume,
                    currentStep = currentStep,
                    viewModel = viewModel,
                    onViewPreview = {
                        viewModel.saveCurrentResume {
                            activeTab = "preview"
                        }
                    }
                )

                "preview" -> ResumePreviewScreen(
                    resume = currentResume,
                    viewModel = viewModel,
                    onEditBack = {
                        activeTab = "builder"
                    }
                )

                "about" -> AboutScreen()
            }
        }
    }
}

@Composable
fun SavedResumesScreen(
    resumes: List<ResumeEntity>,
    onSelect: (ResumeEntity) -> Unit,
    onEdit: (ResumeEntity) -> Unit,
    onDelete: (ResumeEntity) -> Unit,
    onCreateNew: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Resumes",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Button(
                onClick = onCreateNew,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("create_resume_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create New", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (resumes.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "No Resumes",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No saved resumes found",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "Build your high-quality professional resume in minutes by tapping 'Create New'.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(resumes) { resume ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(resume) }
                            .testTag("resume_card_${resume.id}"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = Color(android.graphics.Color.parseColor(resume.accentColor)),
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = resume.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = if (resume.name.isBlank()) "No Name Entered" else resume.name,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                                )
                                Text(
                                    text = "Modified: " + SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                                        .format(Date(resume.updatedAt)),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(start = 16.dp, top = 2.dp)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { onEdit(resume) },
                                    modifier = Modifier.testTag("edit_resume_button_${resume.id}")
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(
                                    onClick = { onDelete(resume) },
                                    modifier = Modifier.testTag("delete_resume_button_${resume.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }

        JaniDevFooter()
    }
}

@Composable
fun ResumeWizardScreen(
    resume: ResumeEntity,
    currentStep: Int,
    viewModel: ResumeViewModel,
    onViewPreview: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Horizontal step progress indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            for (step in 1..6) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .background(
                            color = if (step <= currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Step $currentStep of 6",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = when (currentStep) {
                        1 -> "Contact Information"
                        2 -> "Professional Summary"
                        3 -> "Education History"
                        4 -> "Technical Skills"
                        5 -> "Portfolio Projects"
                        else -> "Work Experience"
                    },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Quick Name editor for the Resume item
            var isEditingTitle by remember { mutableStateOf(false) }
            if (isEditingTitle) {
                OutlinedTextField(
                    value = resume.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .width(150.dp)
                        .testTag("resume_title_field"),
                    trailingIcon = {
                        IconButton(onClick = { isEditingTitle = false }) {
                            Icon(Icons.Default.Check, contentDescription = "Save Title", modifier = Modifier.size(16.dp))
                        }
                    }
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { isEditingTitle = true }
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = resume.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit title",
                        modifier = Modifier.size(10.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Form Area
        Box(modifier = Modifier.weight(1f)) {
            when (currentStep) {
                1 -> ContactStep(resume, viewModel)
                2 -> SummaryStep(resume, viewModel)
                3 -> EducationStep(resume, viewModel)
                4 -> SkillsStep(resume, viewModel)
                5 -> ProjectsStep(resume, viewModel)
                6 -> ExperienceStep(resume, viewModel)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation Controllers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { viewModel.prevStep() },
                enabled = currentStep > 1,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                modifier = Modifier
                    .weight(1f)
                    .testTag("wizard_prev_button")
            ) {
                Text("Previous")
            }

            Spacer(modifier = Modifier.width(12.dp))

            if (currentStep < 6) {
                Button(
                    onClick = { viewModel.nextStep() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wizard_next_button")
                ) {
                    Text("Next Step")
                }
            } else {
                Button(
                    onClick = onViewPreview,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38A169)), // Success green
                    modifier = Modifier
                        .weight(1f)
                        .testTag("wizard_preview_button")
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Live Preview")
                }
            }
        }

        JaniDevFooter()
    }
}

@Composable
fun ContactStep(resume: ResumeEntity, viewModel: ResumeViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            OutlinedTextField(
                value = resume.name,
                onValueChange = { viewModel.updateName(it) },
                label = { Text("Full Name *") },
                placeholder = { Text("e.g. John Doe") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_name")
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = resume.city,
                    onValueChange = { viewModel.updateCity(it) },
                    label = { Text("City *") },
                    placeholder = { Text("Faisalabad") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_city")
                )
                OutlinedTextField(
                    value = resume.country,
                    onValueChange = { viewModel.updateCountry(it) },
                    label = { Text("Country *") },
                    placeholder = { Text("Pakistan") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_country")
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = resume.phone,
                    onValueChange = { viewModel.updatePhone(it) },
                    label = { Text("Phone Number *") },
                    placeholder = { Text("0300-1234567") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_phone")
                )
                OutlinedTextField(
                    value = resume.email,
                    onValueChange = { viewModel.updateEmail(it) },
                    label = { Text("Email Address *") },
                    placeholder = { Text("contact@domain.com") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_email")
                )
            }
        }
        item {
            OutlinedTextField(
                value = resume.website,
                onValueChange = { viewModel.updateWebsite(it) },
                label = { Text("Portfolio Website (Optional)") },
                placeholder = { Text("https://example.com") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_website")
            )
        }
        item {
            OutlinedTextField(
                value = resume.github,
                onValueChange = { viewModel.updateGithub(it) },
                label = { Text("GitHub Link (Optional)") },
                placeholder = { Text("https://github.com/username") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_github")
            )
        }
    }
}

@Composable
fun SummaryStep(resume: ResumeEntity, viewModel: ResumeViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Professional Summary Statement",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = resume.summary,
            onValueChange = { viewModel.updateSummary(it) },
            placeholder = { Text("Introduce your technical focus, primary skills, and accomplishments...") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("input_summary")
        )
    }
}

@Composable
fun EducationStep(resume: ResumeEntity, viewModel: ResumeViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Schools & Degrees",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            TextButton(onClick = { viewModel.addEducation(Education()) }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Track")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (resume.education.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No education records added yet. Click 'Add Track' to insert one.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(resume.education.size) { index ->
                    val edu = resume.education[index]
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Track #${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = { viewModel.removeEducation(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = edu.institute,
                                onValueChange = { viewModel.updateEducation(index, edu.copy(institute = it)) },
                                label = { Text("School / University Name") },
                                placeholder = { Text("e.g. Govt. Islamia College Faisalabad") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = edu.degree,
                                onValueChange = { viewModel.updateEducation(index, edu.copy(degree = it)) },
                                label = { Text("Degree & Academic Achievement") },
                                placeholder = { Text("e.g. BS in Computer Science (BSCS) • CGPA: 3.45") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = edu.startYear,
                                    onValueChange = { viewModel.updateEducation(index, edu.copy(startYear = it)) },
                                    label = { Text("Start Year") },
                                    placeholder = { Text("2020") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = edu.endYear,
                                    onValueChange = { viewModel.updateEducation(index, edu.copy(endYear = it)) },
                                    label = { Text("End Year") },
                                    placeholder = { Text("2024") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillsStep(resume: ResumeEntity, viewModel: ResumeViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Skill Categories",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            TextButton(onClick = { viewModel.addSkillGroup(SkillGroup(label = "", items = emptyList())) }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Category")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (resume.skills.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No skills listed yet. Click 'Add Category' to input one.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(resume.skills.size) { index ->
                    val group = resume.skills[index]
                    var pendingSkillText by remember { mutableStateOf("") }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Category #${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = { viewModel.removeSkillGroup(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = group.label,
                                onValueChange = { viewModel.updateSkillGroup(index, group.copy(label = it)) },
                                label = { Text("Category Title") },
                                placeholder = { Text("e.g. Core Languages, Frameworks") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Chip Tag Entry Box
                            OutlinedTextField(
                                value = pendingSkillText,
                                onValueChange = { pendingSkillText = it },
                                label = { Text("Add Skill Item (Press space or click +)") },
                                placeholder = { Text("Type skill, e.g. Kotlin") },
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            val trimmed = pendingSkillText.trim()
                                            if (trimmed.isNotBlank() && !group.items.contains(trimmed)) {
                                                viewModel.updateSkillGroup(index, group.copy(items = group.items + trimmed))
                                                pendingSkillText = ""
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add Item")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Flowering tags list
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                group.items.forEach { skill ->
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = skill,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Remove skill",
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clickable {
                                                        viewModel.updateSkillGroup(
                                                            index,
                                                            group.copy(items = group.items - skill)
                                                        )
                                                    },
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProjectsStep(resume: ResumeEntity, viewModel: ResumeViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Projects",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            TextButton(onClick = { viewModel.addProject(Project()) }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Project")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (resume.projects.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No projects listed yet. Click 'Add Project' to input one.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(resume.projects.size) { index ->
                    val proj = resume.projects[index]
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Project #${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = { viewModel.removeProject(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = proj.title,
                                    onValueChange = { viewModel.updateProject(index, proj.copy(title = it)) },
                                    label = { Text("Project Title") },
                                    placeholder = { Text("e.g. Chat Web App") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.2f)
                                )
                                OutlinedTextField(
                                    value = proj.stack,
                                    onValueChange = { viewModel.updateProject(index, proj.copy(stack = it)) },
                                    label = { Text("Tech Stack") },
                                    placeholder = { Text("e.g. Kotlin, Ktor") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = proj.role,
                                onValueChange = { viewModel.updateProject(index, proj.copy(role = it)) },
                                label = { Text("Your Role") },
                                placeholder = { Text("e.g. Lead Mobile Developer") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Text area for bullet lines
                            val bulletsText = proj.bullets.joinToString("\n")
                            OutlinedTextField(
                                value = bulletsText,
                                onValueChange = { text ->
                                    val linesList = text.split("\n")
                                    viewModel.updateProject(index, proj.copy(bullets = linesList))
                                },
                                label = { Text("Bullets Details (One bullet per line)") },
                                placeholder = { Text("Architected responsive UI modules...\nIntegrated offline persistence support...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExperienceStep(resume: ResumeEntity, viewModel: ResumeViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Professional Experience",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
            )
            TextButton(onClick = { viewModel.addExperience(Experience()) }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Entry")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (resume.experiences.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No work histories listed yet. Click 'Add Entry' to insert one.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(resume.experiences.size) { index ->
                    val exp = resume.experiences[index]
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Experience #${index + 1}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(
                                    onClick = { viewModel.removeExperience(index) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = exp.organization,
                                    onValueChange = { viewModel.updateExperience(index, exp.copy(organization = it)) },
                                    label = { Text("Company / Organization") },
                                    placeholder = { Text("e.g. JaniDev Studio") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1.2f)
                                )
                                OutlinedTextField(
                                    value = exp.role,
                                    onValueChange = { viewModel.updateExperience(index, exp.copy(role = it)) },
                                    label = { Text("Your Job Role") },
                                    placeholder = { Text("e.g. Software Engineer") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = exp.startYear,
                                    onValueChange = { viewModel.updateExperience(index, exp.copy(startYear = it)) },
                                    label = { Text("Start Year") },
                                    placeholder = { Text("2021") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = exp.endYear,
                                    onValueChange = { viewModel.updateExperience(index, exp.copy(endYear = it)) },
                                    label = { Text("End Year") },
                                    placeholder = { Text("Present") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Bullet inputs
                            val bulletsText = exp.bullets.joinToString("\n")
                            OutlinedTextField(
                                value = bulletsText,
                                onValueChange = { text ->
                                    val linesList = text.split("\n")
                                    viewModel.updateExperience(index, exp.copy(bullets = linesList))
                                },
                                label = { Text("Core Responsibilities (One bullet per line)") },
                                placeholder = { Text("Instructed student groups in algorithms...\nMaintained robust cloud server pipelines...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResumePreviewScreen(
    resume: ResumeEntity,
    viewModel: ResumeViewModel,
    onEditBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTemplate by remember { mutableStateOf(resume.templateName) }
    var selectedColorHex by remember { mutableStateOf(resume.accentColor) }
    var fontSizeMultiplier by remember { mutableStateOf(resume.fontSizeMultiplier) }

    // Intercept Dialog for filename input
    var showFilenameDialog by remember { mutableStateOf(false) }
    var pendingExportType by remember { mutableStateOf("") } // "pdf" or "doc"
    var filenameInputText by remember { mutableStateOf(resume.name.replace(" ", "_") + "_Resume") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Generated Document Preview",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Inspect and customize your professional layout styling parameters before downloading.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Custom styling selectors
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Color choices
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Theme Accents:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    
                    val colorsList = listOf("#6750A4", "#1A365D", "#2B6CB0", "#38A169", "#E53E3E", "#2D3748")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        colorsList.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(android.graphics.Color.parseColor(hex)), CircleShape)
                                    .border(
                                        width = if (selectedColorHex == hex) 2.5.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedColorHex = hex
                                        viewModel.selectAccentColor(hex)
                                        viewModel.saveCurrentResume()
                                    }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Font size scale selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Font Sizing:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(0.8f, 1.0f, 1.2f).forEach { scale ->
                            val label = when (scale) {
                                0.8f -> "Compact"
                                1.0f -> "Normal"
                                else -> "Spacious"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (fontSizeMultiplier == scale) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable {
                                        fontSizeMultiplier = scale
                                        viewModel.updateFontSizeMultiplier(scale)
                                        viewModel.saveCurrentResume()
                                    }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (fontSizeMultiplier == scale) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Paper Sheet simulation viewport
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                .background(Color.White)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Block
                item {
                    Text(
                        text = if (resume.name.isBlank()) "YOUR NAME" else resume.name.uppercase(),
                        fontSize = (22f * fontSizeMultiplier).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(android.graphics.Color.parseColor(selectedColorHex))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    val contacts = listOfNotNull(
                        if (resume.city.isNotBlank()) resume.city else null,
                        if (resume.country.isNotBlank()) resume.country else null,
                        if (resume.phone.isNotBlank()) resume.phone else null,
                        if (resume.email.isNotBlank()) resume.email else null
                    ).joinToString("  |  ")
                    
                    if (contacts.isNotBlank()) {
                        Text(contacts, fontSize = (10f * fontSizeMultiplier).sp, color = Color.DarkGray)
                    }

                    val links = listOfNotNull(
                        if (resume.website.isNotBlank()) "Portfolio: ${resume.website}" else null,
                        if (resume.github.isNotBlank()) "GitHub: ${resume.github}" else null
                    ).joinToString("  |  ")

                    if (links.isNotBlank()) {
                        Text(links, fontSize = (10f * fontSizeMultiplier).sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color(android.graphics.Color.parseColor(selectedColorHex)))
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Summary
                if (resume.summary.isNotBlank()) {
                    item {
                        Text("PROFESSIONAL SUMMARY", fontSize = (11f * fontSizeMultiplier).sp, fontWeight = FontWeight.Bold, color = Color(android.graphics.Color.parseColor(selectedColorHex)))
                        Text(
                            text = resume.summary,
                            fontSize = (10f * fontSizeMultiplier).sp,
                            color = Color.Black,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
                            textAlign = TextAlign.Justify
                        )
                    }
                }

                // Experiences
                val activeExps = resume.experiences.filter { it.organization.isNotBlank() }
                if (activeExps.isNotEmpty()) {
                    item {
                        Text("EXPERIENCE", fontSize = (11f * fontSizeMultiplier).sp, fontWeight = FontWeight.Bold, color = Color(android.graphics.Color.parseColor(selectedColorHex)))
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(activeExps) { exp ->
                        Column(modifier = Modifier.padding(bottom = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(exp.role, fontWeight = FontWeight.Bold, fontSize = (10.5f * fontSizeMultiplier).sp, color = Color.Black)
                                Text("${exp.startYear} - ${if (exp.endYear.isBlank()) "Present" else exp.endYear}", fontSize = (10f * fontSizeMultiplier).sp, color = Color.Black)
                            }
                            Text(exp.organization, fontStyle = FontStyle.Italic, fontSize = (9.5f * fontSizeMultiplier).sp, color = Color(android.graphics.Color.parseColor(selectedColorHex)))
                            
                            exp.bullets.forEach { bullet ->
                                if (bullet.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("•", fontSize = (10f * fontSizeMultiplier).sp, color = Color.Black)
                                        Text(bullet, fontSize = (9.5f * fontSizeMultiplier).sp, color = Color.DarkGray)
                                    }
                                }
                            }
                        }
                    }
                }

                // Projects
                val activeProjects = resume.projects.filter { it.title.isNotBlank() }
                if (activeProjects.isNotEmpty()) {
                    item {
                        Text("PROJECTS & PORTFOLIOS", fontSize = (11f * fontSizeMultiplier).sp, fontWeight = FontWeight.Bold, color = Color(android.graphics.Color.parseColor(selectedColorHex)))
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(activeProjects) { proj ->
                        Column(modifier = Modifier.padding(bottom = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (proj.stack.isNotBlank()) "${proj.title} (${proj.stack})" else proj.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = (10.5f * fontSizeMultiplier).sp,
                                    color = Color.Black
                                )
                                if (proj.role.isNotBlank()) {
                                    Text(proj.role, fontStyle = FontStyle.Italic, fontSize = (9.5f * fontSizeMultiplier).sp, color = Color.Gray)
                                }
                            }
                            proj.bullets.forEach { bullet ->
                                if (bullet.isNotBlank()) {
                                    Row(
                                        modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("•", fontSize = (10f * fontSizeMultiplier).sp, color = Color.Black)
                                        Text(bullet, fontSize = (9.5f * fontSizeMultiplier).sp, color = Color.DarkGray)
                                    }
                                }
                            }
                        }
                    }
                }

                // Education
                val activeEdus = resume.education.filter { it.institute.isNotBlank() }
                if (activeEdus.isNotEmpty()) {
                    item {
                        Text("EDUCATION", fontSize = (11f * fontSizeMultiplier).sp, fontWeight = FontWeight.Bold, color = Color(android.graphics.Color.parseColor(selectedColorHex)))
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(activeEdus) { edu ->
                        Column(modifier = Modifier.padding(bottom = 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(edu.institute, fontWeight = FontWeight.Bold, fontSize = (10.5f * fontSizeMultiplier).sp, color = Color.Black)
                                Text("${edu.startYear} - ${edu.endYear}", fontSize = (10f * fontSizeMultiplier).sp, color = Color.Black)
                            }
                            Text(edu.degree, fontStyle = FontStyle.Italic, fontSize = (9.5f * fontSizeMultiplier).sp, color = Color(android.graphics.Color.parseColor(selectedColorHex)))
                        }
                    }
                }

                // Skills
                val activeSkills = resume.skills.filter { it.label.isNotBlank() && it.items.isNotEmpty() }
                if (activeSkills.isNotEmpty()) {
                    item {
                        Text("TECHNICAL EXPERTISE", fontSize = (11f * fontSizeMultiplier).sp, fontWeight = FontWeight.Bold, color = Color(android.graphics.Color.parseColor(selectedColorHex)))
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    items(activeSkills) { group ->
                        Row(modifier = Modifier.padding(bottom = 4.dp)) {
                            Text("${group.label}: ", fontWeight = FontWeight.Bold, fontSize = (9.5f * fontSizeMultiplier).sp, color = Color.Black)
                            Text(group.items.joinToString(", "), fontSize = (9.5f * fontSizeMultiplier).sp, color = Color.DarkGray)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Actions Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onEditBack,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                modifier = Modifier
                    .weight(0.8f)
                    .testTag("back_to_editor_button")
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit", fontSize = 13.sp)
            }

            Button(
                onClick = {
                    pendingExportType = "pdf"
                    showFilenameDialog = true
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1f)
                    .testTag("download_pdf_button")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Download PDF", fontSize = 12.sp)
            }

            Button(
                onClick = {
                    pendingExportType = "docx"
                    showFilenameDialog = true
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1f)
                    .testTag("download_docx_button")
            ) {
                Icon(Icons.Default.Article, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Word (Docx)", fontSize = 12.sp)
            }
        }

        JaniDevFooter()
    }

    // Export Dialog interceptor
    if (showFilenameDialog) {
        AlertDialog(
            onDismissRequest = { showFilenameDialog = false },
            title = { Text("Name your downloaded file", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = filenameInputText,
                    onValueChange = { filenameInputText = it },
                    label = { Text("Filename") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFilenameDialog = false
                        if (pendingExportType == "pdf") {
                            val pdfFile = ResumeExporter.exportToPdf(context, resume, filenameInputText)
                            if (pdfFile != null) {
                                Toast.makeText(context, "PDF saved to Downloads folder!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Failed to download PDF.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val docFile = ResumeExporter.exportToDocx(context, resume, filenameInputText)
                            if (docFile != null) {
                                Toast.makeText(context, "Editable Word file saved to Downloads folder!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Failed to download Word document.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFilenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AboutScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // High Density Developer Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("developer_about_card")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Image Frame
                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                            .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        ) {
                            androidx.compose.foundation.Image(
                                painter = painterResource(id = R.drawable.dev_pic),
                                contentDescription = "Developer portrait",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "JaniDev",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Lead Android & Full-Stack Architect",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(3.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), RoundedCornerShape(1.5.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Dedicated to creating responsive offline-first Android apps with pristine Material 3 designs, robust local databases, and high-fidelity local content rendering capabilities. Built using Room, Coroutines, and beautiful custom Jetpack Compose canvases.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        item {
            // App Features Description Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Core Capabilities",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val features = listOf(
                        "📝 Direct On-Device PDF Generator (A4 layout formatting)",
                        "💼 Native Word-Compatible (.doc) Export capability",
                        "🎨 Custom Theme Accents & Font size configurations",
                        "💾 Offline Persistence utilizing Room SQLite databases",
                        "🛠️ Fully responsive interactive Form Wizard"
                    )
                    
                    features.forEach { feat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text("•", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                            Text(text = feat, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item {
            JaniDevFooter()
        }
    }
}
