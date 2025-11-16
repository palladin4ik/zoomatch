package com.example.zoomatch.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(user: UserEntity)

  @Query(
    """
    UPDATE users SET 
    name = :name,
    location = :location,
    email = :email,
    phone_number = :phoneNumber,
    avatar = :avatar
    WHERE id = :id
    """
  )
  suspend fun update(
    id: Int,
    name: String,
    location: String,
    email: String,
    phoneNumber: String?,
    avatar: String?
  )

  @Query("SELECT * FROM users LIMIT 1")
  fun getCurrentUserFlow(): Flow<UserEntity>

  @Query("DELETE FROM users")
  suspend fun clear()
}