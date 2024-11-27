package com.otpless.androidotplessdemo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.otpless.dto.OtplessRequest
import com.otpless.dto.OtplessResponse
import com.otpless.main.OtplessManager
import com.otpless.main.OtplessView


class MainActivity : AppCompatActivity() {
    private lateinit var showPreBuildUIButton: Button
    private lateinit var navigateToHeadlessActivity: Button
    private lateinit var otplessResponseHandler: View
    private lateinit var otplessView: OtplessView
    private lateinit var responseTV: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeOtpless()
        initializeViews()
        setOnClickListeners()
    }

    private fun initializeOtpless() {
        // Initialise OtplessView in case of both Activity and Fragment
        otplessView = OtplessManager.getInstance().getOtplessView(this)
    }

    private fun initializeViews() {
        // Set the status bar icon color to black
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            window.decorView.getWindowInsetsController()?.setSystemBarsAppearance(APPEARANCE_LIGHT_STATUS_BARS, APPEARANCE_LIGHT_STATUS_BARS)
        }

        showPreBuildUIButton = findViewById(R.id.btn_pre_built_ui)
        navigateToHeadlessActivity = findViewById(R.id.btn_headless_activity)
        otplessResponseHandler = findViewById(R.id.otpless_response_handler_pre_built_ui)
        responseTV = otplessResponseHandler.findViewById(R.id.tv_response)
    }

    private fun setOnClickListeners() {
        showPreBuildUIButton.setOnClickListener {
            showPreBuiltUI()
        }

        navigateToHeadlessActivity.setOnClickListener {
            startActivity(Intent(this, HeadlessActivity::class.java))
        }

        otplessResponseHandler.findViewById<Button>(R.id.btn_copy_response).setOnClickListener {
            val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Otpless Response", responseTV.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Response Copied!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPreBuiltUI() {
        val request = OtplessRequest(ConfigurationSettings.DEMO_APP_ID)
        otplessView.setCallback(request, this::onOtplessCallback)
        otplessView.showOtplessLoginPage(request, this::onOtplessCallback)
    }

    private fun onOtplessCallback(response: OtplessResponse) {
        otplessResponseHandler.visibility = View.VISIBLE
        responseTV.text = if (response.errorMessage != null) response.errorMessage else response.data.toString()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        otplessView.onNewIntent(intent)
    }

    override fun onBackPressed() {
        // make sure you call this code before super.onBackPressed();
        if (otplessView.onBackPressed()) return
        super.onBackPressed()
    }
}