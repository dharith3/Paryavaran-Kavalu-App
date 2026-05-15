package com.paryavarankavalu.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.paryavarankavalu.ParyavaranApp
import java.io.File

class CleanupFragment : Fragment() {
    private var cleanupPhoto: File? = null
    private var cleanupPhotoUri: Uri? = null
    private var imageView: ImageView? = null

    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) imageView?.setImageURI(cleanupPhotoUri)
    }

    private val permissionRequest = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) captureCleanedPhoto()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        val context = requireContext()
        val reportId = requireArguments().getLong(ARG_REPORT_ID)
        val report = (context.applicationContext as ParyavaranApp).reports.byId(reportId)
        return ScrollView(context).apply {
            addView(vertical(context).apply {
                addView(titleText(context, "Confirm Cleanup"))
                addView(bodyText(context, "Capture the cleaned area before marking this report as cleaned."))
                if (report != null) {
                    addView(bodyText(context, "${report.category} at %.6f, %.6f".format(report.latitude, report.longitude)))
                }
                imageView = ImageView(context).apply {
                    setImageResource(android.R.drawable.ic_menu_camera)
                    adjustViewBounds = true
                    minimumHeight = context.dp(220)
                }
                addView(imageView)
                addView(Button(context).apply {
                    text = "Capture Cleaned Area"
                    setOnClickListener { captureCleanedPhoto() }
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, context.dp(52)))
                addView(Button(context).apply {
                    text = "Mark As Clean"
                    setOnClickListener { markClean(reportId) }
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, context.dp(52)))
            })
        }
    }

    private fun captureCleanedPhoto() {
        val context = requireContext()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionRequest.launch(Manifest.permission.CAMERA)
            return
        }
        val dir = File(context.cacheDir, "cleanup_photos").apply { mkdirs() }
        cleanupPhoto = File(dir, "cleaned_${System.currentTimeMillis()}.jpg")
        cleanupPhotoUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", cleanupPhoto!!)
        takePicture.launch(cleanupPhotoUri)
    }

    private fun markClean(reportId: Long) {
        val context = requireContext()
        if (cleanupPhoto?.exists() != true) {
            Toast.makeText(context, "Capture the cleaned area first", Toast.LENGTH_SHORT).show()
            return
        }
        val repo = (context.applicationContext as ParyavaranApp).reports
        val report = repo.byId(reportId) ?: return
        val workerName = context.getSharedPreferences("ParyavaranPrefs", 0)
            .getString("current_user_name", "Community Worker") ?: "Community Worker"
        if (!report.isCleaned) {
            repo.markCleaned(reportId, workerName, cleanupPhoto!!.absolutePath)
            repo.addKarma(report.reporterName, 1)
        }
        Toast.makeText(context, "Marked cleaned. Reporter earned +1 Eco-Karma.", Toast.LENGTH_LONG).show()
        (activity as? MainActivity)?.show(WorkerHomeFragment())
    }

    companion object {
        private const val ARG_REPORT_ID = "report_id"

        fun forReport(id: Long) = CleanupFragment().apply {
            arguments = Bundle().apply { putLong(ARG_REPORT_ID, id) }
        }
    }
}
