package com.paryavarankavalu

import android.app.Application
import com.paryavarankavalu.data.ReportRepository

class ParyavaranApp : Application() {
    lateinit var reports: ReportRepository
        private set

    override fun onCreate() {
        super.onCreate()
        reports = ReportRepository(this)
    }
}
