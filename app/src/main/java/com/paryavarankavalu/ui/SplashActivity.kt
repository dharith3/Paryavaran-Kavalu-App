package com.paryavarankavalu.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.paryavarankavalu.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = vertical(this, 24).apply {
            gravity = Gravity.CENTER
            setBackgroundResource(R.color.leaf_wash)
            addView(ImageView(this@SplashActivity).apply {
                setImageResource(R.drawable.ic_leaf)
                layoutParams = LinearLayout.LayoutParams(dp(120), dp(120)).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    setMargins(0, 0, 0, dp(16))
                }
            })
            addView(titleText(this@SplashActivity, "Paryavaran Kavalu", 28f).apply {
                gravity = Gravity.CENTER
                textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            addView(bodyText(this@SplashActivity, "Guard Your Environment").apply {
                gravity = Gravity.CENTER
                textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
        setContentView(root)

        Handler(Looper.getMainLooper()).postDelayed({
            val hasName = getSharedPreferences("ParyavaranPrefs", MODE_PRIVATE)
                .getString("current_user_name", null)
                ?.isNotBlank() == true
            startActivity(Intent(this, if (hasName) MainActivity::class.java else OnboardingActivity::class.java))
            finish()
        }, 1200)
    }
}
