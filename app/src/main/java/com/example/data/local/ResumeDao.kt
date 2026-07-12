package com.example.data.local

import androidx.room.*
import com.example.data.model.ResumeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResumeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResume(resume: ResumeEntity): Long

    @Update
    suspend fun updateResume(resume: ResumeEntity)

    @Delete
    suspend fun deleteResume(resume: ResumeEntity)

    @Query("SELECT * FROM resumes WHERE id = :id")
    suspend fun getResumeById(id: Int): ResumeEntity?

    @Query("SELECT * FROM resumes ORDER BY updatedAt DESC")
    fun getAllResumesFlow(): Flow<List<ResumeEntity>>

    @Query("SELECT * FROM resumes ORDER BY updatedAt DESC")
    suspend fun getAllResumes(): List<ResumeEntity>
}
