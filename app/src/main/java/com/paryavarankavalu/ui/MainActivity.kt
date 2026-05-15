package com.paryavarankavalu.ui

import android.os.Bundle
import android.view.Menu
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.paryavarankavalu.R
import com.paryavarankavalu.data.UserRole

class MainActivity : AppCompatActivity() {
    private val containerId = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val role = getSharedPreferences("ParyavaranPrefs", MODE_PRIVATE)
            .getString("current_user_role", UserRole.CITIZEN)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(FrameLayout(this@MainActivity).apply {
                id = containerId
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f))
            addView(BottomNavigationView(this@MainActivity).apply {
                menu.add(Menu.NONE, HOME, Menu.NONE, if (role == UserRole.WORKER) "Work" else "Home").setIcon(android.R.drawable.ic_menu_view)
                if (role == UserRole.CITIZEN) {
                    menu.add(Menu.NONE, REPORT, Menu.NONE, "Report").setIcon(android.R.drawable.ic_menu_camera)
                }
                menu.add(Menu.NONE, PROFILE, Menu.NONE, "Profile").setIcon(android.R.drawable.ic_menu_myplaces)
                setOnItemSelectedListener {
                    show(
                        when (it.itemId) {
                            REPORT -> NewReportFragment()
                            PROFILE -> ProfileFragment()
                            else -> if (role == UserRole.WORKER) WorkerHomeFragment() else HomeFragment()
                        }
                    )
                    true
                }
            })
        }
        setContentView(root)
        if (savedInstanceState == null) show(if (role == UserRole.WORKER) WorkerHomeFragment() else HomeFragment())
    }

    fun show(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(containerId, fragment)
            .commit()
    }

    companion object {
        private const val HOME = 1
        private const val REPORT = 2
        private const val PROFILE = 4
    }
}
