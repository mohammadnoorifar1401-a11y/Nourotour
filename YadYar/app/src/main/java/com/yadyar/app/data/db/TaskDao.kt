package com.yadyar.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yadyar.app.data.model.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks WHERE memberId = :memberId ORDER BY done ASC, createdAt DESC")
    fun observeByMember(memberId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE memberId = :memberId")
    suspend fun getByMember(memberId: String): List<Task>

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Task?

    @Query("SELECT COUNT(*) FROM tasks WHERE memberId = :memberId AND done = 0")
    fun observeOpenCount(memberId: String): Flow<Int>

    @Query("SELECT * FROM tasks WHERE reminderEnabled = 1")
    suspend fun getAllWithReminders(): List<Task>

    @Query("SELECT * FROM tasks")
    suspend fun getAll(): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()

    @Query("UPDATE tasks SET done = :done, doneAt = :doneAt, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setDone(id: String, done: Boolean, doneAt: Long?, updatedAt: Long)

    @Query("UPDATE tasks SET reminderEnabled = 0, updatedAt = :updatedAt WHERE memberId = :memberId")
    suspend fun disableAllReminders(memberId: String, updatedAt: Long)
}
