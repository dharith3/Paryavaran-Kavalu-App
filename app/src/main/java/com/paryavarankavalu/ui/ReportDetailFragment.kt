package com.paryavarankavalu.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.paryavarankavalu.ParyavaranApp
import com.paryavarankavalu.data.UserRole
import java.io.File

class ReportDetailFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        val context = requireContext()
        val reportId = requireArguments().getLong(ARG_REPORT_ID)
        val report = (context.applicationContext as ParyavaranApp).reports.byId(reportId)

        return ScrollView(context).apply {
            addView(vertical(context).apply {
                if (report == null) {
                    addView(titleText(context, "Report not found"))
                } else {
                    addView(titleText(context, report.category))
                    val photo = File(report.photoPath)
                    if (photo.exists()) {
                        addView(ImageView(context).apply {
                            setImageURI(Uri.fromFile(photo))
                            adjustViewBounds = true
                            minimumHeight = context.dp(220)
                        })
                    }
                    addView(bodyText(context, "Reporter: ${report.reporterName}"))
                    addView(bodyText(context, "Status: ${if (report.isCleaned) "Cleaned" else "Pending cleanup"}"))
                    addView(bodyText(context, "Coordinates: %.6f, %.6f".format(report.latitude, report.longitude)))
                    addView(bodyText(context, "Description: ${report.notes.ifBlank { "No description" }}"))

                addView(Button(context).apply {
                    text = "Get Directions"
                    setOnClickListener { openDirections(report.latitude, report.longitude) }
                    }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, context.dp(52)))

                    val role = context.getSharedPreferences("ParyavaranPrefs", 0)
                        .getString("current_user_role", UserRole.CITIZEN)
                    if (role == UserRole.WORKER && !report.isCleaned) {
                        addView(Button(context).apply {
                            text = "Clean This Report"
                            setOnClickListener { (activity as? MainActivity)?.show(CleanupFragment.forReport(report.id)) }
                        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, context.dp(52)))
                    }
                }
            })
        }
    }

    private fun openDirections(latitude: Double, longitude: Double) {
        val uri = Uri.parse("google.navigation:q=$latitude,$longitude")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
        }
        runCatching { startActivity(intent) }.onFailure {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")))
        }
    }

    companion object {
        private const val ARG_REPORT_ID = "report_id"

        fun forReport(id: Long) = ReportDetailFragment().apply {
            arguments = Bundle().apply { putLong(ARG_REPORT_ID, id) }
        }
    }
}
