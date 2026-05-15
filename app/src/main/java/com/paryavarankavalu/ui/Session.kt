package com.paryavarankavalu.ui

import android.content.Context
import android.content.Intent
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout

fun logoutButton(context: Context): Button =
    Button(context).apply {
        text = "Logout"
        setOnClickListener {
            context.getSharedPreferences("ParyavaranPrefs", Context.MODE_PRIVATE)
                .edit()
                .remove("current_user_name")
                .remove("current_user_role")
                .apply()
            context.startActivity(Intent(context, OnboardingActivity::class.java))
            (context as? MainActivity)?.finish()
        }
    }

fun headerWithLogout(context: Context, title: String): LinearLayout =
    LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        addView(titleText(context, title), LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
        addView(logoutButton(context), LinearLayout.LayoutParams(context.dp(108), context.dp(48)))
    }
