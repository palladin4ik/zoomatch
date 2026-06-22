package com.example.zoomatch.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    UserEntity::class,
    AnimalTypeEntity::class,
    BreedEntity::class,
    PetEntity::class,
    MessageEntity::class,
    ChatEntity::class
  ],
  version = 4, // Добавлены animal_type_custom, breed_custom, moderation_status в PetEntity
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun userDao(): UserDao
  abstract fun petDao(): PetDao
  abstract fun animalTypeDao(): AnimalTypeDao
  abstract fun breedDao(): BreedDao
  abstract fun messageDao(): MessageDao
  abstract fun chatDao(): ChatDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "zoomatch_db"
        )
          .fallbackToDestructiveMigration(true)
          .build()
          .also { INSTANCE = it }
      }
    }
  }
}