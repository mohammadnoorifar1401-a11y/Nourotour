package com.mono.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mono.app.data.model.Member
import com.mono.app.data.model.MemberWithStats
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {

    @Query(
        """
        SELECT m.*,
          (SELECT COUNT(*) FROM tasks t WHERE t.memberId = m.id AND t.done = 0) AS openTaskCount,
          (SELECT COUNT(*) FROM tasks t WHERE t.memberId = m.id) AS totalTaskCount
        FROM members m
        WHERE m.archived = 0
        ORDER BY m.createdAt DESC
        """
    )
    fun observeActiveWithStats(): Flow<List<MemberWithStats>>

    @Query("SELECT * FROM members WHERE archived = 1 ORDER BY updatedAt DESC")
    fun observeArchived(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<Member?>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Member?

    @Query("SELECT COUNT(*) FROM members")
    suspend fun count(): Int

    @Query("SELECT * FROM members")
    suspend fun getAll(): List<Member>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(member: Member)

    @Update
    suspend fun update(member: Member)

    @Delete
    suspend fun delete(member: Member)

    @Query("DELETE FROM members")
    suspend fun deleteAll()

    @Query("UPDATE members SET archived = :archived, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setArchived(id: String, archived: Boolean, updatedAt: Long)
}
