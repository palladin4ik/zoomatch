package com.example.zoomatch.data.startScreen

interface UserDisplay {
  val displayName: String
}
sealed class Result<out T> {
  data class Success<T>(val data: T) : Result<T>()
  data class Error(val message: String) : Result<Nothing>()
}