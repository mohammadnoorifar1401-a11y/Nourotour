package com.mono.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val parentId: String? = null, // null = ریشه (امور کاری / امور شخصی)
    val name: String,
    val avatarLetter: String = "",
    val avatarColor: String = "#5B8DEF",
    val avatarImageUri: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
