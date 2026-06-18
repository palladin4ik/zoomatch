package com.example.zoomatch.data.homeScreen.chats

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.Buffer
import okio.BufferedSink
import okio.ForwardingSink

class ProgressRequestBody(
  private val delegate: RequestBody,
  private val onProgress: (Int) -> Unit
) : RequestBody() {

  override fun contentType(): MediaType? = delegate.contentType()

  override fun contentLength(): Long = delegate.contentLength()

  override fun writeTo(sink: BufferedSink) {
    val totalBytes = contentLength()
    var uploadedBytes = 0L

    val countingSink = object : ForwardingSink(sink) {
      override fun write(source: Buffer, byteCount: Long) {
        super.write(source, byteCount)
        uploadedBytes += byteCount
        val progress = if (totalBytes > 0) (uploadedBytes * 100 / totalBytes).toInt() else 0
        onProgress(progress)
      }
    }

    val bufferedSink = Buffer()
    delegate.writeTo(bufferedSink)
    countingSink.write(bufferedSink, bufferedSink.size)
  }
}
