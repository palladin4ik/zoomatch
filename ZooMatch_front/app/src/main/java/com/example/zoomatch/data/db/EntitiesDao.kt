package com.example.zoomatch.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
  @PrimaryKey val id: Int,
  val name: String,
  val email: String,
  val avatar: String?,
  val location: String?,
  val status: String?,
  val phone_number: String?,
  val role: Int = 0
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
  val is_active: Boolean = false
)

//@Entity(tableName = "tag")
//data class TagEntity(
//  @PrimaryKey val id: Int,
//  val tag: String
//) {
//  constructor() : this(0, "")
//}

//@Entity(
//  tableName = "pet_tag",
//  primaryKeys = ["pet_id", "tag_id"],
//  foreignKeys = [
//    ForeignKey(
//      entity = PetEntity::class,
//      parentColumns = ["id"],
//      childColumns = ["pet_id"],
//      onDelete = ForeignKey.CASCADE
//    ),
//    ForeignKey(
//      entity = TagEntity::class,
//      parentColumns = ["id"],
//      childColumns = ["tag_id"],
//      onDelete = ForeignKey.CASCADE
//    )
//  ],
//  indices = [Index("pet_id"), Index("tag_id")]
//)
//data class PetTagCrossRef(
//  val pet_id: Int,
//  val tag_id: Int
//)
