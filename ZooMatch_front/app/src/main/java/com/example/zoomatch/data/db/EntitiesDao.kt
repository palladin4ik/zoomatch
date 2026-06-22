package com.example.zoomatch.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
  @PrimaryKey val id: Int,
  val name: String,
  val firstname: String = "",
  val lastname: String = "",
  val email: String,
  val avatar: String?,
  val location: String?,
  val status: String?,
  val phone_number: String?,
  val role: Int = 0,
  val organization: String? = null
)

@Entity(tableName = "animal_type")
data class AnimalTypeEntity(
  @PrimaryKey val id: Int,
  val name: String
) {
  constructor() : this(0, "")
}

@Entity(tableName = "breed")
data class BreedEntity(
  @PrimaryKey val id: Int,
  val name: String,
  val animal_type: Int
) {
  constructor() : this(0, "", 0)
}

@Entity(
  tableName = "pet",
  foreignKeys = [
    ForeignKey(
      entity = AnimalTypeEntity::class,
      parentColumns = ["id"],
      childColumns = ["animal_type_id"],
      onDelete = ForeignKey.SET_NULL
    ),
    ForeignKey(
      entity = BreedEntity::class,
      parentColumns = ["id"],
      childColumns = ["breed_id"],
      onDelete = ForeignKey.SET_NULL
    ),
    ForeignKey(
      entity = UserEntity::class,
      parentColumns = ["id"],
      childColumns = ["owner_id"],
      onDelete = ForeignKey.CASCADE
    )
  ],
  indices = [Index("animal_type_id"), Index("breed_id"), Index("owner_id")]
)
data class PetEntity(
  @PrimaryKey val id: Int,
  val name: String,
  val animal_type_id: Int?,
  val breed_id: Int?,
  val is_male: Boolean,
  val age: Int,
  val owner_id: Int,
  val avatar: String?,
  val location: String?,
  val has_pedigree: Boolean = false,
  val pedigree_documents: String?,
  val awards: String?,
  val description: String?,
  val is_active: Boolean = false,
  val animal_type_custom: String? = null,
  val breed_custom: String? = null,
  val moderation_status: String? = null
)

@Entity(
  tableName = "messages",
  indices = [
    Index(value = ["chatId"]),
    Index(value = ["senderId"]),
    Index(value = ["receiverId"]),
    Index(value = ["createdAt"])
  ]
)
data class MessageEntity(
  @PrimaryKey(autoGenerate = true) val localId: Long = 0,
  val id: Int? = null,
  val chatId: String,
  val senderId: Int,
  val receiverId: Int,
  val text: String? = null,
  val mediaUrl: String? = null,
  val mediaType: String? = null,
  val isDelivered: Boolean = false,
  val isRead: Boolean = false,
  val isPending: Boolean = false,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chats")
data class ChatEntity(
  @PrimaryKey val chatId: String,
  val interlocutorId: Int,
  val name: String,
  val avatar: String?,
  val lastMessage: String?,
  val lastMessageTime: Long?,
  val unreadCount: Int = 0
)
