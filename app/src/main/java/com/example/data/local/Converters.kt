package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.Education
import com.example.data.model.SkillGroup
import com.example.data.model.Project
import com.example.data.model.Experience
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class Converters {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val educationListType = Types.newParameterizedType(List::class.java, Education::class.java)
    private val skillGroupListType = Types.newParameterizedType(List::class.java, SkillGroup::class.java)
    private val projectListType = Types.newParameterizedType(List::class.java, Project::class.java)
    private val experienceListType = Types.newParameterizedType(List::class.java, Experience::class.java)

    @TypeConverter
    fun fromEducationList(value: List<Education>?): String {
        return moshi.adapter<List<Education>>(educationListType).toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toEducationList(value: String?): List<Education> {
        if (value.isNullOrEmpty()) return emptyList()
        return moshi.adapter<List<Education>>(educationListType).fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromSkillGroupList(value: List<SkillGroup>?): String {
        return moshi.adapter<List<SkillGroup>>(skillGroupListType).toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toSkillGroupList(value: String?): List<SkillGroup> {
        if (value.isNullOrEmpty()) return emptyList()
        return moshi.adapter<List<SkillGroup>>(skillGroupListType).fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromProjectList(value: List<Project>?): String {
        return moshi.adapter<List<Project>>(projectListType).toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toProjectList(value: String?): List<Project> {
        if (value.isNullOrEmpty()) return emptyList()
        return moshi.adapter<List<Project>>(projectListType).fromJson(value) ?: emptyList()
    }

    @TypeConverter
    fun fromExperienceList(value: List<Experience>?): String {
        return moshi.adapter<List<Experience>>(experienceListType).toJson(value ?: emptyList())
    }

    @TypeConverter
    fun toExperienceList(value: String?): List<Experience> {
        if (value.isNullOrEmpty()) return emptyList()
        return moshi.adapter<List<Experience>>(experienceListType).fromJson(value) ?: emptyList()
    }
}
