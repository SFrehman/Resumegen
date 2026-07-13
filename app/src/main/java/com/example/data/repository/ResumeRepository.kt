package com.example.data.repository

import com.example.data.local.ResumeDao
import com.example.data.model.ResumeEntity
import kotlinx.coroutines.flow.Flow

class ResumeRepository(private val resumeDao: ResumeDao) {
    val allResumes: Flow<List<ResumeEntity>> = resumeDao.getAllResumesFlow()

    suspend fun insertResume(resume: ResumeEntity): Long {
        return resumeDao.insertResume(resume)
    }

    suspend fun updateResume(resume: ResumeEntity) {
        resumeDao.updateResume(resume)
    }

    suspend fun deleteResume(resume: ResumeEntity) {
        resumeDao.deleteResume(resume)
    }

    suspend fun getResumeById(id: Int): ResumeEntity? {
        return resumeDao.getResumeById(id)
    }
}
