package com.otpless.androidotplessdemo

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.otpless.dto.HeadlessChannelType
import com.otpless.dto.HeadlessRequest
import com.otpless.dto.HeadlessResponse
import com.otpless.main.OtplessManager
import com.otpless.main.OtplessView


class HeadlessActivity : AppCompatActivity() {
    private lateinit var authSelector: View
    private lateinit var llPhone: LinearLayout
    private lateinit var llEmail: LinearLayout
    private lateinit var llSSO: LinearLayout
    private lateinit var startPhoneAuthButton: Button
    private lateinit var startEmailAuthButton: Button
    private lateinit var startSSOButton: Button
    private lateinit var verifyPhoneOtpButton: Button
    private lateinit var verifyEmailOtpButton: Button
    private lateinit var showChannelsButton: Button
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhoneOtp: EditText
    private lateinit var etEmailOtp: EditText
    private lateinit var etCountryCode: EditText
    private lateinit var otplessResponseHandler: View
    private lateinit var otplessView: OtplessView
    private lateinit var responseTV: TextView
    private var selectedAuthType: String = "P"
    private var selectedChannel: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_headless)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.headless_activity)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeOtpless(savedInstanceState = savedInstanceState)
        initializeViews()
        setOnClickListeners()
    }

    private fun initializeOtpless(savedInstanceState: Bundle?) {
        otplessView = OtplessManager.getInstance().getOtplessView(this)
        otplessView.initHeadless(ConfigurationSettings.DEMO_APP_ID)
        otplessView.setHeadlessCallback(this::onHeadlessCallback)
    }

    private fun startEmailAuth() {
        val request = HeadlessRequest()
        request.setEmail(etEmail.text.toString())
        otplessView.startHeadless(request, ::onHeadlessCallback)
    }

    private fun verifyEmailOTP() {
        val request = HeadlessRequest()
        request.setEmail(etEmail.text.toString())
        request.setOtp(etPhoneOtp.text.toString())
        otplessView.startHeadless(request, ::onHeadlessCallback)
    }

    private fun startPhoneAuth() {
        val request = HeadlessRequest()
        request.setPhoneNumber(etCountryCode.text.toString(), etPhone.text.toString())
        otplessView.startHeadless(request, ::onHeadlessCallback)
    }

    private fun verifyPhoneOTP() {
        val request = HeadlessRequest()
        request.setPhoneNumber(etCountryCode.text.toString(), etPhone.text.toString())
        request.setOtp(etPhoneOtp.text.toString())
        otplessView.startHeadless(request, ::onHeadlessCallback)
    }

    private fun startSSOAuth() {
        val request = HeadlessRequest()
        request.setChannelType(selectedChannel)
        otplessView.startHeadless(request, ::onHeadlessCallback)
    }

    private fun onHeadlessCallback(response: HeadlessResponse) {
        when(response.responseType) {
            "OTP_AUTO_READ" -> {
                response.response?.optString("otp")?.let {
                    etPhoneOtp.setText(it)
                }

            }
        }
        otplessResponseHandler.visibility = View.VISIBLE
        responseTV.text = response.toString()
    }

    override fun onBackPressed() {
        if (otplessView.onBackPressed()) return;
        super.onBackPressed()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        otplessView.onNewIntent(intent)
    }

    private fun setOnClickListeners() {
        startPhoneAuthButton.setOnClickListener {
            startPhoneAuth()
        }

        startEmailAuthButton.setOnClickListener {
            startEmailAuth()
        }

        startSSOButton.setOnClickListener {
            startSSOAuth()
        }

        verifyPhoneOtpButton.setOnClickListener {
            verifyPhoneOTP()
        }

        verifyEmailOtpButton.setOnClickListener {
            verifyEmailOTP()
        }

        authSelector.findViewById<TextView>(R.id.tv_phone).setOnClickListener {
            if (selectedAuthType == "P") return@setOnClickListener
            selectedAuthType = "P"
            handleAuthSelection(showAuthView = llPhone)
            handleAuthSelection(
                showAuthView = authSelector.findViewById(R.id.divider_phone),
                hideViews = arrayOf(
                    authSelector.findViewById(R.id.divider_email),
                    authSelector.findViewById(R.id.divider_sso),
                    otplessResponseHandler,
                    llEmail,
                    llSSO
                )
            )
        }

        authSelector.findViewById<TextView>(R.id.tv_email).setOnClickListener {
            if (selectedAuthType == "E") return@setOnClickListener
            selectedAuthType = "E"
            handleAuthSelection(showAuthView = llEmail)
            handleAuthSelection(
                showAuthView = authSelector.findViewById(R.id.divider_email),
                hideViews = arrayOf(
                    authSelector.findViewById(R.id.divider_sso),
                    authSelector.findViewById(R.id.divider_phone),
                    otplessResponseHandler,
                    llPhone,
                    llSSO
                )
            )
        }

        authSelector.findViewById<TextView>(R.id.tv_sso).setOnClickListener {
            if (selectedAuthType == "SSO") return@setOnClickListener
            selectedAuthType = "SSO"
            handleAuthSelection(showAuthView = llSSO)
            handleAuthSelection(
                showAuthView = authSelector.findViewById(R.id.divider_sso),
                hideViews = arrayOf(
                    authSelector.findViewById(R.id.divider_email),
                    authSelector.findViewById(R.id.divider_phone),
                    otplessResponseHandler,
                    llEmail,
                    llPhone
                )
            )
        }

        showChannelsButton.setOnClickListener { view ->
            val popupMenu = PopupMenu(this, view)

            for (channelType in HeadlessChannelType.entries) {
                popupMenu.menu.add(Menu.NONE, channelType.ordinal, Menu.NONE, channelType.name)
            }

            popupMenu.setOnMenuItemClickListener { menuItem ->
                selectedChannel = HeadlessChannelType.entries[menuItem.itemId].toString()
                showChannelsButton.text =
                    HeadlessChannelType.entries[menuItem.itemId].toString().uppercase()
                true
            }

            popupMenu.show()
        }

        otplessResponseHandler.findViewById<Button>(R.id.btn_copy_response).setOnClickListener {
            val clipboard: ClipboardManager =
                getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Otpless Response", responseTV.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Response Copied!", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.back_button).setOnClickListener {
            finish()
        }
    }

    private fun handleAuthSelection(
        showAuthView: View,
        vararg hideViews: View = arrayOf()
    ) {
        showAuthView.visibility = View.VISIBLE
        for (view in hideViews) {
            view.visibility = View.GONE
        }
    }

    private fun initializeViews() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            window.decorView.getWindowInsetsController()?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        }
        authSelector = findViewById(R.id.auth_type_selector)
        llPhone = findViewById(R.id.ll_phone)
        llEmail = findViewById(R.id.ll_email)
        llSSO = findViewById(R.id.ll_sso)
        startPhoneAuthButton = findViewById(R.id.btn_start_phone_headless)
        startEmailAuthButton = findViewById(R.id.btn_start_email_headless)
        startSSOButton = findViewById(R.id.btn_start_sso_headless)
        etPhone = findViewById(R.id.et_phone)
        etEmail = findViewById(R.id.et_email)
        etPhoneOtp = findViewById(R.id.et_phone_otp)
        etEmailOtp = findViewById(R.id.et_email_otp)
        etCountryCode = findViewById(R.id.et_country_code)
        showChannelsButton = findViewById(R.id.btn_show_channels)
        otplessResponseHandler = findViewById(R.id.otpless_response_handler_headless_activity)
        responseTV = otplessResponseHandler.findViewById(R.id.tv_response)
        verifyEmailOtpButton = findViewById(R.id.btn_verify_email_otp)
        verifyPhoneOtpButton = findViewById(R.id.btn_verify_phone_otp)
    }
}