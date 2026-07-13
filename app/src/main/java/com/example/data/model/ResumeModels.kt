package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

data class Education(
    val institute: String = "",
    val degree: String = "",
    val startYear: String = "",
    val endYear: String = ""
)

data class SkillGroup(
    val label: String = "",
    val items: List<String> = emptyList()
)

data class Project(
    val title: String = "",
    val stack: String = "",
    val role: String = "",
    val bullets: List<String> = emptyList()
)

data class Experience(
    val organization: String = "",
    val role: String = "",
    val startYear: String = "",
    val endYear: String = "",
    val bullets: List<String> = emptyList()
)

@Entity(tableName = "resumes")
data class ResumeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String = "Untitled Resume",
    val updatedAt: Long = System.currentTimeMillis(),
    val name: String = "",
    val city: String = "",
    val country: String = "",
    val phone: String = "",
    val email: String = "",
    val website: String = "",
    val github: String = "",
    val summary: String = "",
    val education: List<Education> = emptyList(),
    val skills: List<SkillGroup> = emptyList(),
    val projects: List<Project> = emptyList(),
    val experiences: List<Experience> = emptyList(),
    val templateName: String = "Modern Slate",
    val accentColor: String = "#6750A4",
    val fontSizeMultiplier: Float = 1.0f
)
