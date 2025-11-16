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
        UPDATE user SET 
        name = :name,
        location = :location,
        email = :email,
        phone_number = :phoneNumber,
        avatar = :avatar,
        status = :status
        WHERE id = :id
        """
  )
  suspend fun update(
    id: Int,
    name: String,
    location: String,
    email: String,
    phoneNumber: String?,
    avatar: String?,
    status: String?
  )

  @Query("SELECT * FROM user LIMIT 1")
  fun getCurrentUserFlow(): Flow<UserEntity>

  @Query("DELETE FROM user")
  suspend fun clear()
}