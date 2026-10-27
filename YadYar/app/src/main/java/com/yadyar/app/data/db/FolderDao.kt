package com.yadyar.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yadyar.app.data.model.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Query("SELECT * FROM folders WHERE parentId IS :parentId ORDER BY sortOrder ASC, createdAt ASC")
    fun observeByParent(parentId: String?): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentId IS :parentId ORDER BY sortOrder ASC, createdAt ASC")
    suspend fun getByParent(parentId: String?): List<Folder>

    @Query("SELECT * FROM folders WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Folder?

    @Query("SELECT * FROM folders WHERE parentId IS NULL ORDER BY sortOrder ASC")
    suspend fun getRoots(): List<Folder>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(folder: Folder)

    @Query("DELETE FROM folders WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT COUNT(*) FROM folders")
    suspend fun count(): Int

    @Query("DELETE FROM folders")
    suspend fun deleteAll()
}
