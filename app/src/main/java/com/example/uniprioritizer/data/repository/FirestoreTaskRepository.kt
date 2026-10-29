package com.example.uniprioritizer.data.repository

import com.example.uniprioritizer.data.remote.FirestorePaths
import com.example.uniprioritizer.data.remote.TaskFields
import com.example.uniprioritizer.data.remote.awaitWrite
import com.example.uniprioritizer.data.remote.snapshotFlow
import com.example.uniprioritizer.data.remote.toCourse
import com.example.uniprioritizer.data.remote.toFirestoreMap
import com.example.uniprioritizer.data.remote.toTask
import com.example.uniprioritizer.domain.model.Course
import com.example.uniprioritizer.domain.model.NewTask
import com.example.uniprioritizer.domain.model.SyncedData
import com.example.uniprioritizer.domain.model.Task
import com.example.uniprioritizer.domain.model.WriteStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class FirestoreTaskRepository(
    private val paths: FirestorePaths,
    private val courseRepository: CourseRepository
) : TaskRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeTasks(): Flow<SyncedData<List<Task>>> =
        courseRepository.observeCourses().flatMapLatest { courses ->
            if (courses.data.isEmpty()) {
                flowOf(SyncedData(emptyList(), courses.fromCache))
            } else {
                combine(courses.data.map { observeCourseTasks(it) }) { perCourse ->
                    SyncedData(
                        data = perCourse.flatMap { it.data },
                        fromCache = courses.fromCache || perCourse.any { it.fromCache }
                    )
                }
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeTask(courseId: String, taskId: String): Flow<SyncedData<Task?>> =
        paths.course(courseId).snapshotFlow().flatMapLatest { courseSnapshot ->
            val course = courseSnapshot.toCourse() ?: Course(id = courseId, name = "", code = "")
            paths.task(courseId, taskId).snapshotFlow().map { snapshot ->
                SyncedData(
                    data = snapshot.toTask(course),
                    fromCache = snapshot.metadata.isFromCache
                )
            }
        }

    override suspend fun addTask(newTask: NewTask): WriteStatus =
        paths.tasks(newTask.courseId).document().set(newTask.toFirestoreMap()).awaitWrite()

    override suspend fun setCompleted(courseId: String, taskId: String, completed: Boolean): WriteStatus =
        paths.task(courseId, taskId).update(TaskFields.COMPLETED, completed).awaitWrite()

    override suspend fun deleteTask(courseId: String, taskId: String): WriteStatus =
        paths.task(courseId, taskId).delete().awaitWrite()

    private fun observeCourseTasks(course: Course): Flow<SyncedData<List<Task>>> =
        paths.tasks(course.id).snapshotFlow().map { snapshot ->
            SyncedData(
                data = snapshot.documents.mapNotNull { it.toTask(course) },
                fromCache = snapshot.metadata.isFromCache
            )
        }
}
