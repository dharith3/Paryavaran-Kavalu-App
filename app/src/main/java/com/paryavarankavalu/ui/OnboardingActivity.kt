package com.paryavarankavalu.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.paryavarankavalu.ParyavaranApp
import com.paryavarankavalu.data.User
import com.paryavarankavalu.data.UserRole

class OnboardingActivity : AppCompatActivity() {
    private var pendingUser: User? = null
    private val permissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        openHome()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nameInput = EditText(this).apply {
            hint = "Your name"
            setSingleLine(true)
        }
        val passwordInput = EditText(this).apply {
            hint = "Password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine(true)
        }
        val roleInput = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@OnboardingActivity,
                android.R.layout.simple_spinner_dropdown_item,
                listOf(UserRole.CITIZEN, UserRole.WORKER)
            )
        }
        val root = vertical(this, 24).apply {
            gravity = Gravity.CENTER
            addView(titleText(this@OnboardingActivity, "Paryavaran Kavalu", 28f))
            addView(bodyText(this@OnboardingActivity, "Sign up or log in as an Ecolove Citizen or Community Worker."))
            addView(nameInput, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, dp(24), 0, dp(12))
            })
            addView(passwordInput)
            addView(roleInput)
            addView(MaterialButton(this@OnboardingActivity).apply {
                text = "Log In"
                setOnClickListener {
                    val name = nameInput.text.toString().trim()
                    val password = passwordInput.text.toString()
                    val role = roleInput.selectedItem.toString()
                    if (name.isBlank() || password.isBlank()) {
                        Toast.makeText(this@OnboardingActivity, "Enter name and password", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    val user = (applicationContext as ParyavaranApp).reports.login(name, password, role)
                    if (user == null) {
                        Toast.makeText(this@OnboardingActivity, "No matching account. Use Sign Up first.", Toast.LENGTH_LONG).show()
                    } else {
                        saveSession(user)
                        requestPermissionsThenHome(user)
                    }
                }
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(56)))
            addView(MaterialButton(this@OnboardingActivity).apply {
                text = "Sign Up"
                setOnClickListener {
                    val name = nameInput.text.toString().trim()
                    val password = passwordInput.text.toString()
                    val role = roleInput.selectedItem.toString()
                    if (name.isBlank() || password.isBlank()) {
                        Toast.makeText(this@OnboardingActivity, "Enter name and password", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    val created = (applicationContext as ParyavaranApp).reports.createUser(name, password, role)
                    if (!created) {
                        Toast.makeText(this@OnboardingActivity, "That name already exists. Log in instead.", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    val user = User(name, password, role, 0)
                    saveSession(user)
                    requestPermissionsThenHome(user)
                }
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(56)))
        }
        setContentView(root)
    }

    private fun saveSession(user: User) {
        getSharedPreferences("ParyavaranPrefs", MODE_PRIVATE).edit()
            .putString("current_user_name", user.name)
            .putString("current_user_role", user.role)
            .apply()
    }

    private fun requestPermissionsThenHome(user: User) {
        pendingUser = user
        if (user.role == UserRole.WORKER) {
            openHome()
            return
        }
        val needed = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (needed.isEmpty()) openHome() else permissions.launch(needed.toTypedArray())
    }

    private fun openHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
