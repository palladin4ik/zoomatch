package com.example.zoomatch.data.db

import com.example.zoomatch.data.homeScreen.pets.PetCreateRequest
import com.example.zoomatch.data.homeScreen.pets.PetResponse
import com.example.zoomatch.data.homeScreen.pets.PetUpdateRequest
import com.example.zoomatch.data.homeScreen.profile.UserEditResponse
import com.example.zoomatch.data.homeScreen.profile.UserEditUI
import com.example.zoomatch.data.homeScreen.search.MatchRequest
import com.example.zoomatch.data.homeScreen.search.MatchResponse
import com.example.zoomatch.data.homeScreen.search.MatchesListResponse
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
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class RefreshRequest(val refresh: String)

data class RefreshResponse(val access: String)


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
  ): Response<PetResponse>

  @PATCH("pets/{id}/")
  suspend fun updatePet(
    @Header("Authorization") auth: String,
    @Path("id") id: Int,
    @Body body: PetUpdateRequest
  ): Response<PetResponse>

  @GET("pets/")
  suspend fun getActivePets(
    @Header("Authorization") auth: String,
    @Query("animal_type") animalType: Int? = null
  ): Response<List<PetLongResponse>>

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

  @GET("matches/{id}/list-matches/")
  suspend fun getMatches(
    @Header("Authorization") auth: String,
    @Path("id") id: Int
  ): Response<MatchesListResponse>

  @GET("pets/{id}/")
  suspend fun getPetById(
    @Header("Authorization") auth: String,
    @Path("id") id: Int
  ): Response<PetLongResponse>

}

object Network {

  private val retrofit = Retrofit.Builder()
    .baseUrl("http://192.168.0.126:8000/api/v1/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

  val zooMatchApi: ZooMatchApi = retrofit.create(ZooMatchApi::class.java)
}
