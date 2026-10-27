package com.yadyar.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yadyar.app.data.model.MemberDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDetailsDao {

    @Query("SELECT * FROM member_details WHERE memberId = :memberId LIMIT 1")
    fun observeByMember(memberId: String): Flow<MemberDetails?>

    @Query("SELECT * FROM member_details WHERE memberId = :memberId LIMIT 1")
    suspend fun getByMember(memberId: String): MemberDetails?

    @Query("SELECT * FROM member_details")
    suspend fun getAll(): List<MemberDetails>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(details: MemberDetails)

    @Query("DELETE FROM member_details WHERE memberId = :memberId")
    suspend fun deleteByMember(memberId: String)

    @Query("DELETE FROM member_details")
    suspend fun deleteAll()
}
