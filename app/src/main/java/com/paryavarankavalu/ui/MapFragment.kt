package com.paryavarankavalu.ui

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.paryavarankavalu.ParyavaranApp
import com.paryavarankavalu.data.Report

class MapFragment : Fragment(), OnMapReadyCallback {
    private val markerReports = mutableMapOf<Marker, Report>()
    private var map: GoogleMap? = null
    private val focusedReportId: Long?
        get() = arguments?.getLong(ARG_REPORT_ID)?.takeIf { it > 0L }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        val context = requireContext()
        val mapId = View.generateViewId()
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            addView(titleText(context, "Cleanup Map", 22f).apply {
                setPadding(context.dp(16), context.dp(16), context.dp(16), context.dp(8))
            })
            addView(FrameLayout(context).apply { id = mapId }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            ))
            post {
                childFragmentManager.beginTransaction()
                    .replace(mapId, SupportMapFragment.newInstance())
                    .commitNow()
                (childFragmentManager.findFragmentById(mapId) as SupportMapFragment).getMapAsync(this@MapFragment)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        googleMap.uiSettings.isZoomControlsEnabled = true
        googleMap.uiSettings.isCompassEnabled = true
        googleMap.uiSettings.isMapToolbarEnabled = true
        googleMap.uiSettings.isMyLocationButtonEnabled = true
        loadMarkers()
        googleMap.setOnMarkerClickListener { marker ->
            markerReports[marker]?.let { showReportDialog(it) }
            true
        }
    }

    private fun loadMarkers() {
        val googleMap = map ?: return
        val repo = (requireContext().applicationContext as ParyavaranApp).reports
        val reports = focusedReportId?.let { id -> repo.byId(id)?.let { listOf(it) } } ?: repo.all()
        googleMap.clear()
        markerReports.clear()
        val fallback = LatLng(12.9716, 77.5946)
        var first = fallback
        reports.forEachIndexed { index, report ->
            val position = LatLng(report.latitude, report.longitude)
            if (index == 0) first = position
            val marker = googleMap.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(report.category)
                    .snippet(if (report.isCleaned) "Cleaned" else "Pending cleanup")
                    .icon(BitmapDescriptorFactory.defaultMarker(if (report.isCleaned) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED))
            )
            if (marker != null) markerReports[marker] = report
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(first, if (reports.isEmpty()) 11f else 17f))
    }

    private fun showReportDialog(report: Report) {
        val context = requireContext()
        val message = buildString {
            appendLine(report.notes.ifBlank { "No notes" })
            appendLine()
            appendLine("Reporter: ${report.reporterName}")
            appendLine("Status: ${if (report.isCleaned) "Cleaned" else "Pending cleanup"}")
            append("Coordinates: %.6f, %.6f".format(report.latitude, report.longitude))
        }
        AlertDialog.Builder(context)
            .setTitle(report.category)
            .setMessage(message)
            .setPositiveButton("Open Details") { _, _ ->
                (activity as? MainActivity)?.show(ReportDetailFragment.forReport(report.id))
            }
            .setNeutralButton("Directions") { _, _ -> openDirections(report.latitude, report.longitude) }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun openDirections(latitude: Double, longitude: Double) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=$latitude,$longitude")).apply {
            setPackage("com.google.android.apps.maps")
        }
        runCatching { startActivity(intent) }.onFailure {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")))
        }
    }

    companion object {
        private const val ARG_REPORT_ID = "report_id"

        fun forReport(id: Long) = MapFragment().apply {
            arguments = Bundle().apply { putLong(ARG_REPORT_ID, id) }
        }
    }
}
