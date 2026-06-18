package com.example.zoomatch.data.homeScreen.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

data class UserUI(
  val avatar: String,
  var firstname: String,
  var lastname: String,
  val geo: String?,
  val description: String?,
  val email: String,
  val phone_number: String?,
  val organization: String?
)

data class UserEditUI(
  val avatar: String?,
  var firstname: String,
  var lastname: String,
  val email: String,
  val description: String?,
  val phone_number: String?,
  val organization: String?
)

data class UserEditResponse(
  val id: Int,
  val firstname: String,
  val lastname: String,
  val email: String,
  val avatar: String?,
  val location: String?,
  val description: String?,
  val phone_number: String?,
  val role: Int,
  val organization: String?,
  val last_seen: String?,
  val is_active: Boolean
)

data class EditProfileFormState(
  val firstnameError: Int? = null,
  val lastnameError: Int? = null,
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
      var data = base64 ?: return null
      if (data.startsWith("data:image/")) {
        data = data.substringAfter(",")
      }
      val bytes = Base64.decode(data, Base64.NO_WRAP)
      BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
      null
    }
  }
}
