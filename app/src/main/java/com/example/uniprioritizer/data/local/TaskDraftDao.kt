package com.example.uniprioritizer.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDraftDao {
    @Query("SELECT * FROM task_drafts WHERE id = :id")
    fun observeDraft(id: Int = CURRENT_DRAFT_ID): Flow<TaskDraftEntity?>

    @Upsert
    suspend fun upsert(draft: TaskDraftEntity)

    @Query("DELETE FROM task_drafts")
    suspend fun deleteAll()
}
