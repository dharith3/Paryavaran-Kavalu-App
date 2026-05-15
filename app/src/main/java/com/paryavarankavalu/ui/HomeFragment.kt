package com.paryavarankavalu.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.paryavarankavalu.ParyavaranApp

class HomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        val context = requireContext()
        val prefs = context.getSharedPreferences("ParyavaranPrefs", 0)
        val name = prefs.getString("current_user_name", "Guardian") ?: "Guardian"
        val repo = (context.applicationContext as ParyavaranApp).reports
        val user = repo.user(name)
        val reports = repo.byReporter(name)
        val pendingReports = reports.filter { !it.isCleaned }
        val cleanedReports = reports.filter { it.isCleaned }

        return ScrollView(context).apply {
            addView(vertical(context).apply {
                addView(headerWithLogout(context, "Namaste, $name"))
                addView(card(context, vertical(context, 16).apply {
                    addView(titleText(context, "${user?.karma ?: 0} Eco-Karma", 22f))
                    addView(bodyText(context, "${reports.size} reports | ${pendingReports.size} pending | ${cleanedReports.size} cleaned"))
                }))
                addView(Button(context).apply {
                    text = "Report a Blackspot"
                    setOnClickListener { (activity as? MainActivity)?.show(NewReportFragment()) }
                }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, context.dp(52)))
                addView(titleText(context, "Pending Reports", 20f))
                if (pendingReports.isEmpty()) {
                    addView(bodyText(context, "No reports yet. Start by reporting a waste blackspot nearby."))
                } else {
                    pendingReports.forEach { addView(reportCard(it)) }
                }
                addView(titleText(context, "Cleaned Reports", 20f))
                if (cleanedReports.isEmpty()) addView(bodyText(context, "Cleaned reports will appear here."))
                cleanedReports.forEach { addView(reportCard(it)) }
            })
        }
    }

    private fun reportCard(report: com.paryavarankavalu.data.Report): View {
        val context = requireContext()
        return card(context, vertical(context, 12).apply {
            addView(titleText(context, report.category, 18f))
            addView(bodyText(context, if (report.isCleaned) "Cleaned" else "Pending cleanup"))
            addView(bodyText(context, "%.6f, %.6f".format(report.latitude, report.longitude)))
        }).apply {
            setOnClickListener { (activity as? MainActivity)?.show(ReportDetailFragment.forReport(report.id)) }
        }
    }
}
