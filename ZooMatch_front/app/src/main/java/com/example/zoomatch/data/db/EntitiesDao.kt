package com.example.zoomatch.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
  @PrimaryKey val id: Int,
  val email: String,
  val name: String,
  val avatar: String?,
  val location: String?,
  val phone_number: String?,
  val role: Int,
  val last_seen: String,
  val is_active: Boolean,
  val description: String?
)
