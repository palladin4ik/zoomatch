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
  fun getCurrentUserFlow(): Flow<UserEntity?>

  @Query("DELETE FROM user")
  suspend fun clear()

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllPets(pet: List<PetEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllAnimalTypes(animal_type: List<AnimalTypeEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAllBreeds(breed: List<BreedEntity>)

  @Query("SELECT * FROM pet")
  suspend fun getAllPets(): List<PetEntity>

//  @Query("DELETE FROM animal_type")
//  suspend fun clearAnimalTypes()
//
//  @Query("DELETE FROM breed")
//  suspend fun clearBreeds()

  @Query("SELECT COUNT(*) FROM pet WHERE owner_id = (SELECT id FROM user LIMIT 1)")
  fun getPetCountForCurrentUser(): Flow<Int>

  @Query("SELECT id FROM pet WHERE owner_id = (SELECT id FROM user LIMIT 1)")
  suspend fun getCurrentUserPetIds(): List<Int>
}

@Dao
interface PetDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(pet: PetEntity)

  @Query("SELECT * FROM pet WHERE id = :id")
  suspend fun getPetById(id: Int): PetEntity?

//  @Insert(onConflict = OnConflictStrategy.REPLACE)
//  suspend fun insertPetTags(tags: List<PetTagCrossRef>)
//
//  @Transaction
//  @Query("SELECT * FROM pet WHERE owner_id = (SELECT id FROM user LIMIT 1)")
//  fun getPetsWithTags(): Flow<List<PetWithTags>>

  @Query("SELECT * FROM pet WHERE owner_id = (SELECT id FROM user LIMIT 1)")
  fun getPetsFlow(): Flow<List<PetEntity>>

  @Query("DELETE FROM pet WHERE id = :id")
  suspend fun deleteById(id: Int)

//  @Query("DELETE FROM pet_tag WHERE pet_id = :petId")
//  suspend fun deletePetTags(petId: Int)

//  @Transaction
//  suspend fun upsertPetWithTags(pet: PetEntity, tagIds: List<Int>) {
//    insert(pet)
//    deletePetTags(pet.id)
//    if (tagIds.isNotEmpty()) {
//      insertPetTags(tagIds.map { PetTagCrossRef(pet.id, it) })
//    }
//  }

}

@Dao
interface AnimalTypeDao {
  @Query("SELECT * FROM animal_type")
  fun getAllFlow(): Flow<List<AnimalTypeEntity>>
}

@Dao
interface BreedDao {
  @Query("SELECT * FROM breed")
  fun getAllFlow(): Flow<List<BreedEntity>>
}
//
//data class PetWithTags(
//  @Embedded val pet: PetEntity,
//
//  @Relation(
//    parentColumn = "id",
//    entityColumn = "id",
//    associateBy = Junction(
//      PetTagCrossRef::class,
//      parentColumn = "pet_id",
//      entityColumn = "tag_id"
//    )
//  )
//  val tags: List<TagEntity>
//)