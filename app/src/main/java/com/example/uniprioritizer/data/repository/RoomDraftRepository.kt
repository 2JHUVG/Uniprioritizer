package com.example.uniprioritizer.data.repository

import com.example.uniprioritizer.data.local.TaskDraftDao
import com.example.uniprioritizer.data.local.toDomain
import com.example.uniprioritizer.data.local.toEntity
import com.example.uniprioritizer.domain.model.TaskDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomDraftRepository(
    private val taskDraftDao: TaskDraftDao
) : DraftRepository {

    override fun observeDraft(): Flow<TaskDraft?> =
        taskDraftDao.observeDraft().map { entity -> entity?.toDomain()?.takeUnless { it.isEmpty } }

    override suspend fun saveDraft(draft: TaskDraft) {
        if (draft.isEmpty) {
            taskDraftDao.deleteAll()
        } else {
            taskDraftDao.upsert(draft.toEntity())
        }
    }

    override suspend fun clearDraft() {
        taskDraftDao.deleteAll()
    }
}
