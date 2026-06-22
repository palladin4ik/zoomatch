package com.example.zoomatch.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(user: UserEntity)

  @Query(
    """
        UPDATE user SET 
        name = :name,
        firstname = :firstname,
        lastname = :lastname,
        location = :location,
        email = :email,
        phone_number = :phoneNumber,
        avatar = :avatar,
        status = :status,
        organization = :organization
        WHERE id = :id
        """
  )
  suspend fun update(
    id: Int,
    name: String,
    firstname: String,
    lastname: String,
    location: String,
    email: String,
    phoneNumber: String?,
    avatar: String?,
    status: String?,
    organization: String?
  )

  @Query("SELECT * FROM user LIMIT 1")
  fun getCurrentUserFlow(): Flow<UserEntity?>

  @Query("DELETE FROM user")
  suspend fun clear()

  @Query("DELETE FROM pet")
  suspend fun clearPets()

  @Query("DELETE FROM animal_type")
  suspend fun clearAnimalTypes()

  @Query("DELETE FROM breed")
  suspend fun clearBreeds()

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

  @Query("SELECT COUNT(*) FROM pet WHERE owner_id = (SELECT id FROM user LIMIT 1) AND (moderation_status IS NULL OR moderation_status = 'approved')")
  fun getActivePetCountForCurrentUser(): Flow<Int>

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

  @Query("UPDATE pet SET avatar = :avatar WHERE id = :petId")
  suspend fun updateAvatar(petId: Int, avatar: String?)

  @Query("UPDATE pet SET pedigree_documents = :documents WHERE id = :petId")
  suspend fun updatePedigreeDocuments(petId: Int, documents: String?)

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

  @Query("SELECT * FROM animal_type WHERE name LIKE '%' || :query || '%' COLLATE NOCASE")
  suspend fun search(query: String): List<AnimalTypeEntity>

  @Query("SELECT * FROM animal_type WHERE name = :name COLLATE NOCASE LIMIT 1")
  suspend fun findExact(name: String): AnimalTypeEntity?
}

@Dao
interface BreedDao {
  @Query("SELECT * FROM breed")
  fun getAllFlow(): Flow<List<BreedEntity>>

  @Query("SELECT * FROM breed WHERE animal_type = :animalTypeId AND name LIKE '%' || :query || '%' COLLATE NOCASE")
  suspend fun search(animalTypeId: Int, query: String): List<BreedEntity>

  @Query("SELECT * FROM breed WHERE animal_type = :animalTypeId AND name = :name COLLATE NOCASE LIMIT 1")
  suspend fun findExact(animalTypeId: Int, name: String): BreedEntity?
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

@Dao
interface MessageDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(message: MessageEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(messages: List<MessageEntity>)

  @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt ASC")
  fun getMessagesForChat(chatId: String): Flow<List<MessageEntity>>

  @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY createdAt DESC LIMIT :limit")
  suspend fun getLastMessages(chatId: String, limit: Int = 30): List<MessageEntity>

  @Query("UPDATE messages SET isRead = 1 WHERE (id = :messageId OR localId = :messageId) AND receiverId = :currentUserId")
  suspend fun markAsRead(messageId: Long, currentUserId: Int)

  @Query("UPDATE messages SET isDelivered = 1 WHERE id = :messageId OR localId = :messageId")
  suspend fun markAsDelivered(messageId: Long)

  @Query("DELETE FROM messages WHERE chatId = :chatId")
  suspend fun deleteChatMessages(chatId: String)

  @Query("SELECT * FROM messages WHERE id = :id")
  suspend fun getMessageById(id: Int): MessageEntity?

  @Query("UPDATE messages SET id = :serverId, isPending = 0 WHERE localId = :localId")
  suspend fun updateServerId(localId: Long, serverId: Int)

  @Query("UPDATE messages SET text = :newText WHERE id = :serverId")
  suspend fun updateText(serverId: Int, newText: String)

  @Query("DELETE FROM messages WHERE id = :serverId")
  suspend fun deleteByServerId(serverId: Int)

  @Query("UPDATE messages SET id = :serverId, isPending = 0 WHERE localId = (SELECT localId FROM messages WHERE chatId = :chatId AND senderId = :senderId AND text = :text AND isPending = 1 LIMIT 1)")
  suspend fun claimPendingMessage(chatId: String, senderId: Int, text: String, serverId: Int)
}

@Dao
interface ChatDao {
  @Query("SELECT * FROM chats ORDER BY lastMessageTime DESC")
  fun getAllChatsFlow(): Flow<List<ChatEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(chats: List<ChatEntity>)

  @Query("DELETE FROM chats")
  suspend fun clearAll()

  @Transaction
  suspend fun replaceAll(chats: List<ChatEntity>) {
    clearAll()
    insertAll(chats)
  }
}