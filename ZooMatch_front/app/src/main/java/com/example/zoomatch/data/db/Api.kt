package com.example.zoomatch.data.db

import com.example.zoomatch.data.startScreen.JwtResponse
import com.example.zoomatch.data.startScreen.LoginRequest
import com.example.zoomatch.data.startScreen.RegUser
import com.example.zoomatch.data.startScreen.RegUserResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

data class RefreshRequest(val refresh: String)

data class RefreshResponse(val access: String)  // иногда приходит и новый refresh


interface ZooMatchApi {
  @POST("register/")
  suspend fun registerUser(@Body user: RegUser): Response<RegUserResponse>

  @POST("jwt/create/")
  suspend fun loginUser(@Body credentials: LoginRequest): Response<JwtResponse>

  @POST("jwt/refresh/")
  suspend fun refreshToken(@Body body: RefreshRequest): Response<RefreshResponse>

  @GET("me/")
  suspend fun getProfile(@Header("Authorization") auth: String): Response<UserEntity>
}

object Network {

  private val retrofit = Retrofit.Builder()
    .baseUrl("http://192.168.0.126:8000/api/v1/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

  val zooMatchApi: ZooMatchApi = retrofit.create(ZooMatchApi::class.java)
}
