package com.paryavarankavalu.ui

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.card.MaterialCardView
import com.paryavarankavalu.R

fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

fun titleText(context: Context, text: String, size: Float = 24f): TextView =
    TextView(context).apply {
        this.text = text
        textSize = size
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(ContextCompat.getColor(context, R.color.forest_green))
    }

fun bodyText(context: Context, text: String): TextView =
    TextView(context).apply {
        this.text = text
        textSize = 16f
        setTextColor(ContextCompat.getColor(context, R.color.earth_brown))
    }

fun vertical(context: Context, padding: Int = 16): LinearLayout =
    LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(context.dp(padding), context.dp(padding), context.dp(padding), context.dp(padding))
        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

fun card(context: Context, child: View): MaterialCardView =
    MaterialCardView(context).apply {
        radius = context.dp(16).toFloat()
        cardElevation = context.dp(2).toFloat()
        setCardBackgroundColor(ContextCompat.getColor(context, R.color.clean_white))
        strokeWidth = context.dp(1)
        strokeColor = ContextCompat.getColor(context, R.color.mint_leaf)
        addView(child)
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, context.dp(10), 0, context.dp(10)) }
    }
