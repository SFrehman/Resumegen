package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Education
import com.example.data.model.Experience
import com.example.data.model.Project
import com.example.data.model.ResumeEntity
import com.example.data.model.SkillGroup
import com.example.data.repository.ResumeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResumeViewModel(private val repository: ResumeRepository) : ViewModel() {

    // Current Resume being created or edited
    private val _currentResume = MutableStateFlow(ResumeEntity())
    val currentResume: StateFlow<ResumeEntity> = _currentResume.asStateFlow()

    // All Saved Resumes
    val savedResumes: StateFlow<List<ResumeEntity>> = repository.allResumes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current active wizard step (1 to 6)
    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    // UI Dark Theme toggle state override
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.update { !it }
    }

    fun setStep(step: Int) {
        if (step in 1..6) {
            _currentStep.value = step
        }
    }

    fun nextStep() {
        setStep(_currentStep.value + 1)
    }

    fun prevStep() {
        setStep(_currentStep.value - 1)
    }

    // Core identification field setters
    fun updateTitle(title: String) {
        _currentResume.update { it.copy(title = title, updatedAt = System.currentTimeMillis()) }
    }

    fun updateName(name: String) {
        _currentResume.update { it.copy(name = name, updatedAt = System.currentTimeMillis()) }
    }

    fun updateCity(city: String) {
        _currentResume.update { it.copy(city = city, updatedAt = System.currentTimeMillis()) }
    }

    fun updateCountry(country: String) {
        _currentResume.update { it.copy(country = country, updatedAt = System.currentTimeMillis()) }
    }

    fun updatePhone(phone: String) {
        _currentResume.update { it.copy(phone = phone, updatedAt = System.currentTimeMillis()) }
    }

    fun updateEmail(email: String) {
        _currentResume.update { it.copy(email = email, updatedAt = System.currentTimeMillis()) }
    }

    fun updateWebsite(website: String) {
        _currentResume.update { it.copy(website = website, updatedAt = System.currentTimeMillis()) }
    }

    fun updateGithub(github: String) {
        _currentResume.update { it.copy(github = github, updatedAt = System.currentTimeMillis()) }
    }

    fun updateSummary(summary: String) {
        _currentResume.update { it.copy(summary = summary, updatedAt = System.currentTimeMillis()) }
    }

    fun selectTemplate(template: String) {
        _currentResume.update { it.copy(templateName = template, updatedAt = System.currentTimeMillis()) }
    }

    fun selectAccentColor(hexColor: String) {
        _currentResume.update { it.copy(accentColor = hexColor, updatedAt = System.currentTimeMillis()) }
    }

    fun updateFontSizeMultiplier(multiplier: Float) {
        _currentResume.update { it.copy(fontSizeMultiplier = multiplier, updatedAt = System.currentTimeMillis()) }
    }

    // Dynamic list controllers: Education
    fun addEducation(edu: Education = Education()) {
        _currentResume.update {
            it.copy(
                education = it.education + edu,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    fun removeEducation(index: Int) {
        _currentResume.update {
            val newList = it.education.toMutableList()
            if (index in newList.indices) {
                newList.removeAt(index)
            }
            it.copy(education = newList, updatedAt = System.currentTimeMillis())
        }
    }

    fun updateEducation(index: Int, updated: Education) {
        _currentResume.update {
            val newList = it.education.toMutableList()
            if (index in newList.indices) {
                newList[index] = updated
            }
            it.copy(education = newList, updatedAt = System.currentTimeMillis())
        }
    }

    // Dynamic list controllers: Skills
    fun addSkillGroup(group: SkillGroup = SkillGroup(label = "", items = emptyList())) {
        _currentResume.update {
            it.copy(
                skills = it.skills + group,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    fun removeSkillGroup(index: Int) {
        _currentResume.update {
            val newList = it.skills.toMutableList()
            if (index in newList.indices) {
                newList.removeAt(index)
            }
            it.copy(skills = newList, updatedAt = System.currentTimeMillis())
        }
    }

    fun updateSkillGroup(index: Int, updated: SkillGroup) {
        _currentResume.update {
            val newList = it.skills.toMutableList()
            if (index in newList.indices) {
                newList[index] = updated
            }
            it.copy(skills = newList, updatedAt = System.currentTimeMillis())
        }
    }

    // Dynamic list controllers: Projects
    fun addProject(proj: Project = Project()) {
        _currentResume.update {
            it.copy(
                projects = it.projects + proj,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    fun removeProject(index: Int) {
        _currentResume.update {
            val newList = it.projects.toMutableList()
            if (index in newList.indices) {
                newList.removeAt(index)
            }
            it.copy(projects = newList, updatedAt = System.currentTimeMillis())
        }
    }

    fun updateProject(index: Int, updated: Project) {
        _currentResume.update {
            val newList = it.projects.toMutableList()
            if (index in newList.indices) {
                newList[index] = updated
            }
            it.copy(projects = newList, updatedAt = System.currentTimeMillis())
        }
    }

    // Dynamic list controllers: Experiences
    fun addExperience(exp: Experience = Experience()) {
        _currentResume.update {
            it.copy(
                experiences = it.experiences + exp,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    fun removeExperience(index: Int) {
        _currentResume.update {
            val newList = it.experiences.toMutableList()
            if (index in newList.indices) {
                newList.removeAt(index)
            }
            it.copy(experiences = newList, updatedAt = System.currentTimeMillis())
        }
    }

    fun updateExperience(index: Int, updated: Experience) {
        _currentResume.update {
            val newList = it.experiences.toMutableList()
            if (index in newList.indices) {
                newList[index] = updated
            }
            it.copy(experiences = newList, updatedAt = System.currentTimeMillis())
        }
    }

    // Database actions
    fun loadResume(resume: ResumeEntity) {
        _currentResume.value = resume
        _currentStep.value = 1
    }

    fun createNewResume() {
        _currentResume.value = ResumeEntity(
            title = "Resume #${System.currentTimeMillis().toString().takeLast(4)}",
            education = listOf(Education()),
            skills = listOf(SkillGroup(label = "Core Skills", items = emptyList())),
            projects = listOf(Project()),
            experiences = listOf(Experience())
        )
        _currentStep.value = 1
    }

    fun saveCurrentResume(onSuccess: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val resume = _currentResume.value.copy(updatedAt = System.currentTimeMillis())
            if (resume.id == 0) {
                val newId = repository.insertResume(resume)
                _currentResume.update { it.copy(id = newId.toInt()) }
                onSuccess(newId.toInt())
            } else {
                repository.updateResume(resume)
                onSuccess(resume.id)
            }
        }
    }

    fun deleteResume(resume: ResumeEntity) {
        viewModelScope.launch {
            repository.deleteResume(resume)
            if (_currentResume.value.id == resume.id) {
                createNewResume()
            }
        }
    }
}
