package com.paryavarankavalu.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.paryavarankavalu.ParyavaranApp
import com.paryavarankavalu.data.Report
import com.paryavarankavalu.data.UserRole
import java.io.File

class ProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        val context = requireContext()
        val prefs = context.getSharedPreferences("ParyavaranPrefs", 0)
        val name = prefs.getString("current_user_name", "Guardian") ?: "Guardian"
        val role = prefs.getString("current_user_role", UserRole.CITIZEN)
        val repo = (context.applicationContext as ParyavaranApp).reports
        val myReports = if (role == UserRole.WORKER) repo.cleanedBy(name) else repo.byReporter(name)
        val points = repo.user(name)?.karma ?: 0

        return ScrollView(context).apply {
            addView(vertical(context).apply {
                addView(headerWithLogout(context, name))
                addView(bodyText(context, "$points Eco-Karma points"))
                addView(titleText(context, if (role == UserRole.WORKER) "Cleanups You Completed" else "Your Reports", 20f))
                if (myReports.isEmpty()) {
                    addView(bodyText(context, if (role == UserRole.WORKER) "No completed cleanups yet." else "No reports submitted yet."))
                }
                myReports.forEach { report ->
                    addView(reportCard(report, role == UserRole.WORKER))
                }
            })
        }
    }

    private fun reportCard(report: Report, showCleanedPhoto: Boolean): View {
        val context = requireContext()
        return card(context, vertical(context, 12).apply {
            addView(titleText(context, report.category, 18f))
            if (showCleanedPhoto) {
                val photo = report.cleanedPhotoPath?.let { File(it) }
                if (photo?.exists() == true) {
                    addView(ImageView(context).apply {
                        setImageURI(android.net.Uri.fromFile(photo))
                        adjustViewBounds = true
                        minimumHeight = context.dp(180)
                    })
                }
            }
            addView(bodyText(context, if (report.isCleaned) "Cleaned" else "Pending cleanup"))
            if (showCleanedPhoto) addView(bodyText(context, "Reported by: ${report.reporterName}"))
            addView(bodyText(context, report.notes.ifBlank { "No description" }))
            addView(bodyText(context, "%.6f, %.6f".format(report.latitude, report.longitude)))
        }).apply {
            setOnClickListener { (activity as? MainActivity)?.show(ReportDetailFragment.forReport(report.id)) }
        }
    }
}
