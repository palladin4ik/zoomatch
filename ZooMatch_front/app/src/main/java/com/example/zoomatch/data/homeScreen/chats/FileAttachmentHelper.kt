package com.example.zoomatch.data.homeScreen.chats

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

class FileAttachmentHelper @Inject constructor(
  @ApplicationContext private val context: Context
) {

  data class FileResult(
    val path: String,
    val fileType: String,
    val originalName: String,
    val mimeType: String
  )

  fun copyFileToAppStorage(uri: Uri): FileResult? {
    return try {
      val fileName = getFileName(uri) ?: "file_${UUID.randomUUID()}"
      val fileType = getFileType(fileName)
      val mimeType = getMimeType(fileName)

      val destination = File(context.filesDir, "chat_media/$fileName")
      destination.parentFile?.mkdirs()

      context.contentResolver.openInputStream(uri)?.use { input ->
        destination.outputStream().use { output ->
          input.copyTo(output)
        }
      }

      FileResult(destination.absolutePath, fileType, fileName, mimeType)
    } catch (e: Exception) {
      null
    }
  }

  private fun getFileName(uri: Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
      if (cursor.moveToFirst()) {
        val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (index >= 0) name = cursor.getString(index)
      }
    }
    return name
  }

  private fun getFileType(fileName: String): String {
    return when {
      fileName.endsWith(".jpg", true) || fileName.endsWith(".png", true) -> "image"
      fileName.endsWith(".pdf", true) -> "pdf"
      fileName.endsWith(".docx", true) -> "docx"
      else -> "file"
    }
  }

  private fun getMimeType(fileName: String): String {
    val ext = fileName.substringAfterLast('.', "").lowercase()
    return MIME_MAP[ext] ?: "application/octet-stream"
  }

  companion object {
    private val MIME_MAP = mapOf(
      "jpg" to "image/jpeg",
      "jpeg" to "image/jpeg",
      "png" to "image/png",
      "webp" to "image/webp",
      "gif" to "image/gif",
      "mp4" to "video/mp4",
      "mov" to "video/quicktime",
      "pdf" to "application/pdf",
      "mp3" to "audio/mpeg",
      "ogg" to "audio/ogg",
      "xlsx" to "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
      "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
      "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
      "xls" to "application/vnd.ms-excel",
      "doc" to "application/msword",
      "ppt" to "application/vnd.ms-powerpoint",
      "zip" to "application/zip",
      "rar" to "application/x-rar-compressed",
      "txt" to "text/plain"
    )
  }
}