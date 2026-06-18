package com.example.zoomatch.data.db

import com.example.zoomatch.data.homeScreen.chats.ChatResponse
import com.example.zoomatch.data.homeScreen.pets.PetCreateRequest
import com.example.zoomatch.data.homeScreen.pets.PetCreateResponse
import com.example.zoomatch.data.homeScreen.pets.PetResponse
import com.example.zoomatch.data.homeScreen.pets.PetUpdateRequest
import com.example.zoomatch.data.homeScreen.profile.UserEditResponse
import com.example.zoomatch.data.homeScreen.profile.UserEditUI
import com.example.zoomatch.data.homeScreen.search.MatchRequest
import com.example.zoomatch.data.homeScreen.search.MatchResponse
import com.example.zoomatch.data.homeScreen.search.PetLongResponse
import com.example.zoomatch.data.homeScreen.settings.EditPassRequest
import com.example.zoomatch.data.homeScreen.settings.EditPassResponse
import com.example.zoomatch.data.startScreen.AnimalTypeResponse
import com.example.zoomatch.data.startScreen.BreedResponse
import com.example.zoomatch.data.startScreen.JwtResponse
import com.example.zoomatch.data.startScreen.LoginRequest
import com.example.zoomatch.data.startScreen.LoginResponse
import com.example.zoomatch.data.startScreen.RegUser
import com.example.zoomatch.data.startScreen.RegUserResponse
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

data class RefreshRequest(val refresh: String)
data class RefreshResponse(val access: String)

data class MessagesPaginationResponse(
  val next: String?,
  val previous: String?,
  val results: List<ServerMessageResponse>
)

data class ServerMessageResponse(
  val id: Int,
  val sender: SimpleUserResponse?,
  val receiver: SimpleUserResponse?,
  val text: String?,
  val media: String?,
  @SerializedName("is_read") val is_read: Boolean,
  @SerializedName("created_at") val created_at: String
)

data class SimpleUserResponse(
  val id: Int,
  val firstname: String?,
  val lastname: String?,
  val avatar: String?
)
data class MediaUploadResponse(val media: String)
data class CreateMessageRequest(
  @SerializedName("receiver_id") val receiverId: Int,
  val text: String? = null
)

data class EditMessageRequest(
  val text: String
)

data class MatchStatusRequest(
  val status: Int
)

interface ZooMatchApi {
  @POST("register/")
  suspend fun registerUser(@Body user: RegUser): Response<RegUserResponse>

  @POST("jwt/create/")
  suspend fun loginUser(@Body body: LoginRequest): Response<JwtResponse>

  @POST("jwt/refresh/")
  suspend fun refreshToken(@Body body: RefreshRequest): Response<RefreshResponse>

  @GET("me/")
  suspend fun getProfile(
    @Header("Authorization") auth: String
  ): Response<LoginResponse>

  @PATCH("me/")
  suspend fun updateProfile(
    @Header("Authorization") auth: String,
    @Body body: UserEditUI
  ): Response<UserEditResponse>

  @PATCH("me/change-password/")
  suspend fun updatePass(
    @Header("Authorization") auth: String,
    @Body body: EditPassRequest
  ): Response<EditPassResponse>

  @DELETE("me/")
  suspend fun delete(
    @Header("Authorization") auth: String,
  ): Response<String>

  @GET("animal-type/")
  suspend fun getAnimalTypes(
    @Header("Authorization") auth: String
  ): Response<List<AnimalTypeResponse>>

  @GET("breed/")
  suspend fun getBreeds(
    @Header("Authorization") auth: String
  ): Response<List<BreedResponse>>

  @POST("pets/")
  suspend fun createPet(
    @Header("Authorization") auth: String,
    @Body body: PetCreateRequest
  ): Response<PetCreateResponse>

  @PATCH("pets/{id}/")
  suspend fun updatePet(
    @Header("Authorization") auth: String,
    @Path("id") id: Int,
    @Body body: PetUpdateRequest
  ): Response<PetCreateResponse>

  @GET("pets/")
  suspend fun getActivePets(
    @Header("Authorization") auth: String,
    @Query("animal_type") animalType: Int? = null
  ): Response<List<PetLongResponse>>

  @GET("pets/me/")
  suspend fun getMyPets(
    @Header("Authorization") auth: String
  ): Response<List<PetResponse>>

  @POST("matches/")
  suspend fun createMatch(
    @Header("Authorization") auth: String,
    @Body body: MatchRequest
  ): Response<MatchResponse>

  @DELETE("pets/{id}/")
  suspend fun deletePet(
    @Header("Authorization") auth: String,
    @Path("id") id: Int
  ): Response<Void>

  @GET("matches/")
  suspend fun getMatchesList(
    @Header("Authorization") auth: String,
    @Query("type") type: String? = null
  ): Response<List<MatchResponse>>

  @PATCH("matches/{id}/")
  suspend fun updateMatchStatus(
    @Header("Authorization") auth: String,
    @Path("id") matchId: Int,
    @Body body: MatchStatusRequest
  ): Response<MatchResponse>

  @GET("pets/{id}/")
  suspend fun getPetById(
    @Header("Authorization") auth: String,
    @Path("id") id: Int
  ): Response<PetLongResponse>

  @GET("chats/")
  suspend fun getChats(
    @Header("Authorization") auth: String
  ): Response<List<ChatResponse>>

  @GET("messages/")
  suspend fun getMessages(
    @Header("Authorization") auth: String,
    @Query("receiver") receiverId: Int,
    @Query("cursor") cursor: String? = null
  ): Response<MessagesPaginationResponse>

  @POST("messages/")
  suspend fun createMessage(
    @Header("Authorization") auth: String,
    @Body body: CreateMessageRequest
  ): Response<ServerMessageResponse>

  @PATCH("messages/{id}/")
  suspend fun editMessage(
    @Header("Authorization") auth: String,
    @Path("id") messageId: Int,
    @Body body: EditMessageRequest
  ): Response<ServerMessageResponse>

  @DELETE("messages/{id}/")
  suspend fun deleteMessage(
    @Header("Authorization") auth: String,
    @Path("id") messageId: Int
  ): Response<Void>

  @POST("messages/{id}/read/")
  suspend fun markMessageAsRead(
    @Header("Authorization") auth: String,
    @Path("id") messageId: Int
  ): Response<Void>

  @Multipart
  @POST("messages/{id}/media/")
  suspend fun uploadMessageMedia(
    @Header("Authorization") auth: String,
    @Path("id") messageId: Int,
    @Part file: MultipartBody.Part
  ): Response<MediaUploadResponse>

  @Multipart
  @PATCH("me/avatar/")
  suspend fun uploadUserAvatar(
    @Header("Authorization") auth: String,
    @Part avatar: MultipartBody.Part
  ): Response<AvatarResponse>

  @Multipart
  @PATCH("pets/{id}/avatar/")
  suspend fun uploadPetAvatar(
    @Header("Authorization") auth: String,
    @Path("id") petId: Int,
    @Part avatar: MultipartBody.Part
  ): Response<AvatarResponse>

  @Multipart
  @PATCH("pets/{id}/documents/")
  suspend fun uploadPetDocuments(
    @Header("Authorization") auth: String,
    @Path("id") petId: Int,
    @Part documents: MultipartBody.Part
  ): Response<PedigreeResponse>

  @DELETE("pets/{id}/documents/")
  suspend fun deletePetDocuments(
    @Header("Authorization") auth: String,
    @Path("id") petId: Int
  ): Response<Void>

  @GET("recommend/recommend/")
  suspend fun getRecommendations(
    @Header("Authorization") auth: String,
    @Query("pet_id") petId: Int,
    @Query("radius_km") radiusKm: Int? = null,
    @Query("requires_pedigree") requiresPedigree: Boolean? = null,
    @Query("min_age") minAge: Int? = null,
    @Query("max_age") maxAge: Int? = null,
    @Query("max_months_since_mating") maxMonthsSinceMating: Int? = null
  ): Response<RecommendationResponse>

  @GET("geo/distance/")
  suspend fun getDistance(
    @Header("Authorization") auth: String,
    @Query("lat1") lat1: Double,
    @Query("lon1") lon1: Double,
    @Query("lat2") lat2: Double,
    @Query("lon2") lon2: Double
  ): Response<DistanceResponse>
}

data class DistanceResponse(
  @com.google.gson.annotations.SerializedName("distance_km") val distanceKm: Double
)

data class AvatarResponse(val avatar: String)
data class PedigreeResponse(val pedigree_documents: String)
data class RecommendationResponse(
  val results: List<com.example.zoomatch.data.homeScreen.search.PetShortRecommendation>,
  @com.google.gson.annotations.SerializedName("suggest_expand") val suggestExpand: Boolean
)

object Network {
  private val retrofit = Retrofit.Builder()
    .baseUrl("https://zoomatch.ru/api/v1/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

  val zooMatchApi: ZooMatchApi = retrofit.create(ZooMatchApi::class.java)
}
