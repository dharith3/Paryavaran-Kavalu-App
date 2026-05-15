package com.paryavarankavalu.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import com.paryavarankavalu.ParyavaranApp
import com.paryavarankavalu.data.Report

class WorkerHomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        val context = requireContext()
        val repo = (context.applicationContext as ParyavaranApp).reports
        val pending = repo.pending()
        val cleaned = repo.cleaned()

        return ScrollView(context).apply {
            addView(vertical(context).apply {
                addView(headerWithLogout(context, "Community Worker"))
                addView(bodyText(context, "Open a pending report, inspect the photo and coordinates, navigate to the spot, then confirm cleanup with a fresh photo."))
                addView(titleText(context, "Not Cleaned", 20f))
                if (pending.isEmpty()) addView(bodyText(context, "No pending reports right now."))
                pending.forEach { addView(reportCard(it)) }
                addView(titleText(context, "Cleaned", 20f))
                if (cleaned.isEmpty()) addView(bodyText(context, "Completed cleanup reports will appear here."))
                cleaned.forEach { addView(reportCard(it)) }
            })
        }
    }

    private fun reportCard(report: Report): View {
        val context = requireContext()
        return card(context, vertical(context, 12).apply {
            addView(titleText(context, report.category, 18f))
            addView(bodyText(context, "Reporter: ${report.reporterName}"))
            addView(bodyText(context, if (report.isCleaned) "Cleaned" else "Pending cleanup"))
            addView(bodyText(context, "%.6f, %.6f".format(report.latitude, report.longitude)))
        }).apply {
            setOnClickListener { (activity as? MainActivity)?.show(ReportDetailFragment.forReport(report.id)) }
        }
    }
}
