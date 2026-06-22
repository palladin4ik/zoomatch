package com.example.zoomatch.ui.location

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import com.example.zoomatch.R
import com.example.zoomatch.databinding.ActivityLocationPickerBinding
import com.example.zoomatch.ui.applySystemBarsPadding
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import java.util.Locale

class LocationPickerActivity : AppCompatActivity() {

  private lateinit var binding: ActivityLocationPickerBinding
  private lateinit var mapView: MapView
  private var selectedPoint: Point? = null
  private var placemark: PlacemarkMapObject? = null

  private val mapInputListener = object : InputListener {
    override fun onMapTap(map: Map, point: Point) {
      selectedPoint = point
      updatePlacemark(point)
      reverseGeocode(point)
    }

    override fun onMapLongTap(map: Map, point: Point) {}
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    MapKitFactory.initialize(this)
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    binding = ActivityLocationPickerBinding.inflate(layoutInflater)
    setContentView(binding.root)
    binding.root.applySystemBarsPadding()

    mapView = binding.mapView
    mapView.map.addInputListener(mapInputListener)

    val lat = intent.getDoubleExtra("LATITUDE", 55.7558)
    val lon = intent.getDoubleExtra("LONGITUDE", 37.6173)
    val initialPoint = Point(lat, lon)
    mapView.map.move(
      CameraPosition(initialPoint, 14f, 0f, 0f),
      Animation(Animation.Type.SMOOTH, 0.5f),
      null
    )

    binding.confirmButton.setOnClickListener {
      val point = selectedPoint ?: Point(lat, lon)
      val address = binding.addressPreview.text?.toString() ?: ""
      val resultIntent = Intent().apply {
        putExtra("LATITUDE", point.latitude)
        putExtra("LONGITUDE", point.longitude)
        putExtra("ADDRESS", address)
      }
      setResult(RESULT_OK, resultIntent)
      finish()
    }

    binding.backButton.setOnClickListener { finish() }
  }

  private fun vectorToBitmap(drawableRes: Int): Bitmap {
    val drawable = AppCompatResources.getDrawable(this, drawableRes)!!
    val size = 80
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, size, size)
    drawable.draw(canvas)
    return bitmap
  }

  private fun updatePlacemark(point: Point) {
    placemark?.let { mapView.map.mapObjects.remove(it) }
    val bitmap = vectorToBitmap(R.drawable.ic_location_pin)
    placemark = mapView.map.mapObjects.addPlacemark(
      point,
      ImageProvider.fromBitmap(bitmap)
    )
  }

  private fun reverseGeocode(point: Point) {
    try {
      val geocoder = Geocoder(this, Locale("ru", "RU"))
      @Suppress("DEPRECATION")
      val addresses = geocoder.getFromLocation(point.latitude, point.longitude, 1)
      if (!addresses.isNullOrEmpty()) {
        val addr = addresses[0]
        val parts = mutableListOf<String>()
        addr.thoroughfare?.let { parts.add(it) }
        addr.subThoroughfare?.let { parts.add(it) }
        addr.locality?.let { if (parts.isEmpty()) parts.add(it) }
        val address = parts.joinToString(", ").ifBlank { "${point.latitude}, ${point.longitude}" }
        binding.addressPreview.text = address
      } else {
        binding.addressPreview.text = "${point.latitude}, ${point.longitude}"
      }
    } catch (e: Exception) {
      binding.addressPreview.text = "${point.latitude}, ${point.longitude}"
    }
  }

  override fun onStart() {
    super.onStart()
    MapKitFactory.getInstance().onStart()
    mapView.onStart()
  }

  override fun onStop() {
    mapView.onStop()
    MapKitFactory.getInstance().onStop()
    super.onStop()
  }
}
