package com.paryavarankavalu.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.google.android.gms.location.Priority
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.paryavarankavalu.ParyavaranApp
import com.paryavarankavalu.data.Report
import java.io.File

class NewReportFragment : Fragment() {
    private var photoFile: File? = null
    private var photoUri: Uri? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var accuracyMeters: Float? = null
    private var imageView: ImageView? = null
    private var locationLabel: android.widget.TextView? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) {
            imageView?.setImageURI(photoUri)
            fetchFreshLocation("Photo captured. Capturing exact GPS position...")
        }
    }

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        fetchLocation()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        val context = requireContext()
        val notes = EditText(context).apply {
            hint = "Notes, landmark, or cleanup instructions"
            minLines = 3
        }
        val categories = RadioGroup(context).apply {
            listOf("Plastic", "Construction Waste", "Food Waste", "Mixed Garbage", "Hazardous").forEachIndexed { index, label ->
                addView(RadioButton(context).apply {
                    text = label
                    id = index + 10
                })
            }
            check(10)
        }
        imageView = ImageView(context).apply {
            setImageResource(android.R.drawable.ic_menu_camera)
            adjustViewBounds = true
            minimumHeight = context.dp(180)
        }
        locationLabel = bodyText(context, "Location not captured yet")

        return ScrollView(context).apply {
            addView(vertical(context).apply {
                addView(titleText(context, "New Waste Blackspot"))
                addView(imageView)
                addView(Button(context).apply {
                    text = "Capture Photo"
                    setOnClickListener { capturePhoto() }
                })
                addView(titleText(context, "Waste Category", 18f))
                addView(categories)
                addView(notes)
                addView(locationLabel)
                addView(Button(context).apply {
                    text = "Capture Current GPS"
                    setOnClickListener { requestLocationPermission() }
                })
                addView(Button(context).apply {
                    text = "Submit Report"
                    setOnClickListener { submit(categories, notes.text.toString()) }
                })
            })
        }
    }

    override fun onResume() {
        super.onResume()
        requestLocationPermission()
    }

    private fun capturePhoto() {
        val context = requireContext()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionRequest.launch(arrayOf(Manifest.permission.CAMERA))
            return
        }
        val dir = File(context.cacheDir, "captured_photos").apply { mkdirs() }
        photoFile = File(dir, "blackspot_${System.currentTimeMillis()}.jpg")
        photoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile!!)
        takePicture.launch(photoUri)
    }

    private fun requestLocationPermission() {
        val context = requireContext()
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine == PackageManager.PERMISSION_GRANTED) {
            fetchFreshLocation("Capturing current high-accuracy GPS...")
        } else if (coarse == PackageManager.PERMISSION_GRANTED) {
            fetchFreshLocation("Capturing current GPS...")
        } else {
            permissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private fun fetchLocation() {
        fetchFreshLocation("Capturing current GPS...")
    }

    private fun fetchFreshLocation(message: String) {
        val context = requireContext()
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!hasFine && !hasCoarse) return

        locationLabel?.text = message
        if (!hasFine) {
            locationLabel?.text = "Only approximate location is allowed. Enable Precise location for this app, then tap Capture Current GPS."
        }
        val priority = if (hasFine) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY
        val token = CancellationTokenSource()
        LocationServices.getFusedLocationProviderClient(context)
            .getCurrentLocation(priority, token.token)
            .addOnSuccessListener { location ->
                if (location == null) {
                    latitude = null
                    longitude = null
                    accuracyMeters = null
                    locationLabel?.text = "Could not get GPS. Move outdoors, enable location, then tap Capture Current GPS."
                    return@addOnSuccessListener
                }
                latitude = location.latitude
                longitude = location.longitude
                accuracyMeters = if (location.hasAccuracy()) location.accuracy else null
                val accuracy = accuracyMeters?.let { " accuracy +/- %.1fm".format(it) } ?: " accuracy unknown"
                val quality = when {
                    accuracyMeters == null -> "Please retry until accuracy is shown."
                    accuracyMeters!! <= REQUIRED_ACCURACY_METERS -> "Ready to submit."
                    else -> "Too inaccurate. Move outdoors and tap Capture Current GPS again."
                }
                locationLabel?.text = "GPS: %.6f, %.6f%s\n%s".format(latitude, longitude, accuracy, quality)
            }
            .addOnFailureListener {
                latitude = null
                longitude = null
                accuracyMeters = null
                locationLabel?.text = "GPS failed. Enable precise location and try again."
            }
    }

    private fun submit(categories: RadioGroup, notes: String) {
        val context = requireContext()
        val source = photoFile
        if (source == null || !source.exists()) {
            Toast.makeText(context, "Please capture a photo first", Toast.LENGTH_SHORT).show()
            return
        }
        if (latitude == null || longitude == null) {
            fetchFreshLocation("Capturing current GPS before submit...")
            Toast.makeText(context, "Wait for exact GPS coordinates before submitting", Toast.LENGTH_LONG).show()
            return
        }
        val accuracy = accuracyMeters
        if (accuracy == null || accuracy > REQUIRED_ACCURACY_METERS) {
            Toast.makeText(context, "GPS accuracy must be within ${REQUIRED_ACCURACY_METERS.toInt()} meters. Capture GPS again outdoors.", Toast.LENGTH_LONG).show()
            return
        }

        val category = categories.findViewById<RadioButton>(categories.checkedRadioButtonId).text.toString()
        val prefs = context.getSharedPreferences("ParyavaranPrefs", 0)
        val reporter = prefs.getString("current_user_name", "Guardian") ?: "Guardian"
        val compressed = ImageTools.compressUnder500Kb(source)
        (context.applicationContext as ParyavaranApp).reports.add(
            Report(
                reporterName = reporter,
                category = category,
                notes = notes,
                photoPath = compressed.absolutePath,
                latitude = latitude!!,
                longitude = longitude!!,
                isCleaned = false,
                createdAt = System.currentTimeMillis()
            )
        )
        Toast.makeText(context, "Report submitted. Eco-Karma is awarded after cleanup.", Toast.LENGTH_LONG).show()
        (activity as? MainActivity)?.show(HomeFragment())
    }

    companion object {
        private const val REQUIRED_ACCURACY_METERS = 35f
    }
}
