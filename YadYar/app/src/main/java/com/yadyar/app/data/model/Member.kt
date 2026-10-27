package com.yadyar.app.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class MemberType(val key: String, val fa: String) {
    CUSTOMER("customer", "مشتری"),
    STAFF("staff", "پرسنل");

    companion object {
        fun fromKey(key: String?): MemberType =
            entries.firstOrNull { it.key == key } ?: CUSTOMER
    }
}

@Entity(tableName = "members")
data class Member(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val type: String = MemberType.CUSTOMER.key,
    val name: String,
    val avatarLetter: String = "",
    val avatarColor: String = DEFAULT_COLORS[0],
    val avatarImageBase64: String? = null,
    val avatarImageUri: String? = null,
    val folderId: String? = null,
    val notes: String = "",
    val archived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val typeEnum: MemberType get() = MemberType.fromKey(type)

    companion object {
        val DEFAULT_COLORS = listOf(
            "#5B8DEF", "#F2994A", "#9B51E0", "#27AE60",
            "#EB5757", "#2D9CDB", "#F2C94C", "#BB6BD9"
        )
    }
}

data class MemberWithStats(
    @Embedded val member: Member,
    val openTaskCount: Int,
    val totalTaskCount: Int
)
