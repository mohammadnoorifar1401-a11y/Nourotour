package com.yadyar.app.data.repository

import com.yadyar.app.data.db.MemberDao
import com.yadyar.app.data.db.MemberDetailsDao
import com.yadyar.app.data.db.TaskDao
import com.yadyar.app.data.model.Member
import com.yadyar.app.data.model.MemberDetails
import com.yadyar.app.data.model.MemberWithStats
import com.yadyar.app.data.model.Task
import kotlinx.coroutines.flow.Flow

class MemberRepository(private val memberDao: MemberDao) {

    fun observeActiveWithStats(): Flow<List<MemberWithStats>> = memberDao.observeActiveWithStats()
    fun observeActiveWithStatsByFolder(folderId: String): Flow<List<MemberWithStats>> = memberDao.observeActiveWithStatsByFolder(folderId)

    fun observeArchived(): Flow<List<Member>> = memberDao.observeArchived()

    fun observeById(id: String): Flow<Member?> = memberDao.observeById(id)

    suspend fun getById(id: String): Member? = memberDao.getById(id)

    suspend fun count(): Int = memberDao.count()

    suspend fun getAll(): List<Member> = memberDao.getAll()

    suspend fun upsert(member: Member) = memberDao.upsert(member)

    suspend fun delete(member: Member) = memberDao.delete(member)

    suspend fun deleteAll() = memberDao.deleteAll()

    suspend fun setArchived(id: String, archived: Boolean) =
        memberDao.setArchived(id, archived, System.currentTimeMillis())
}

class TaskRepository(private val taskDao: TaskDao) {

    fun observeByMember(memberId: String): Flow<List<Task>> = taskDao.observeByMember(memberId)

    fun observeOpenCount(memberId: String): Flow<Int> = taskDao.observeOpenCount(memberId)

    suspend fun getById(id: String): Task? = taskDao.getById(id)

    suspend fun getByMember(memberId: String): List<Task> = taskDao.getByMember(memberId)

    suspend fun getAll(): List<Task> = taskDao.getAll()

    suspend fun getAllWithReminders(): List<Task> = taskDao.getAllWithReminders()

    suspend fun upsert(task: Task) = taskDao.upsert(task)

    suspend fun update(task: Task) = taskDao.update(task)

    suspend fun delete(task: Task) = taskDao.delete(task)

    suspend fun deleteAll() = taskDao.deleteAll()

    suspend fun setDone(id: String, done: Boolean) =
        taskDao.setDone(
            id = id,
            done = done,
            doneAt = if (done) System.currentTimeMillis() else null,
            updatedAt = System.currentTimeMillis()
        )

    suspend fun disableAllReminders(memberId: String) =
        taskDao.disableAllReminders(memberId, System.currentTimeMillis())
}

class MemberDetailsRepository(private val dao: MemberDetailsDao) {

    fun observeByMember(memberId: String): Flow<MemberDetails?> = dao.observeByMember(memberId)

    suspend fun getByMember(memberId: String): MemberDetails? = dao.getByMember(memberId)

    suspend fun getAll(): List<MemberDetails> = dao.getAll()

    suspend fun upsert(details: MemberDetails) = dao.upsert(details)

    suspend fun deleteByMember(memberId: String) = dao.deleteByMember(memberId)

    suspend fun deleteAll() = dao.deleteAll()
}
