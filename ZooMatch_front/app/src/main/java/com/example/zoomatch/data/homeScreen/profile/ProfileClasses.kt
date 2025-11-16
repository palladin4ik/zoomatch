package com.example.zoomatch.data.homeScreen.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

data class UserUI(
  val avatar: String,
  var name: String,
  val geo: String?,
  val status: String?,
)

data class UserEditUI(
  val avatar: String? = null,
  var name: String,
  val location: String?,
  val status: String?,
  val email: String,
  val phone_number: String?
)

data class UserEditResponse(
  val id: Int,
  val avatar: String?,
  var name: String,
  val location: String,
  val status: String,
  val email: String,
  val phone_number: String
)

data class EditProfileFormState(
  val nameError: Int? = null,
  val emailError: Int? = null,
  val phoneError: Int? = null,
  val isDataValid: Boolean = false
)

object ImageUtils {

  fun bitmapToBase64(bitmap: Bitmap): String {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
    val bytes = stream.toByteArray()
    return Base64.encodeToString(bytes, Base64.NO_WRAP)
  }

  fun base64ToBitmap(base64: String?): Bitmap? {
    return try {
      val bytes = Base64.decode(base64, Base64.NO_WRAP)
      BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
      null
    }
  }
}