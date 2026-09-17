package com.ansarweb.telegramforwarderv2

import android.os.Bundle
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView
    private lateinit var apiIdInput: EditText
    private lateinit var apiHashInput: EditText
    private lateinit var phoneInput: EditText
    private lateinit var codeInput: EditText
    private lateinit var passwordInput: EditText

    private var client: Client? = null

    private var parametersSent = false
    private var phoneSent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Client.execute(
            TdApi.SetLogVerbosityLevel(0)
        )

        buildUi()

        client = Client.create(
            { update ->
                handleUpdate(update)
            },
            { error ->
                runOnUiThread {
                    status.text = "خطای TDLib: ${error.message}"
                }
            },
            null
        )
    }

    private fun buildUi() {

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "Telegram Forwarder V2"
            textSize = 24f
        }

        apiIdInput = EditText(this).apply {
            hint = "API ID"
            inputType = InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
        }

        apiHashInput = EditText(this).apply {
            hint = "API Hash"
            setSingleLine(true)
        }

        phoneInput = EditText(this).apply {
            hint = "شماره تلفن تلگرام"
            inputType = InputType.TYPE_CLASS_PHONE
            setSingleLine(true)
        }

        val loginButton = Button(this).apply {
            text = "شروع ورود به تلگرام"

            setOnClickListener {
                startTelegramLogin()
            }
        }

        codeInput = EditText(this).apply {
            hint = "کد تأیید تلگرام"
            inputType = InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
        }

        val codeButton = Button(this).apply {
            text = "تأیید کد"

            setOnClickListener {
                val code = codeInput.text.toString().trim()

                if (code.isBlank()) {
                    status.text = "کد تأیید را وارد کن."
                    return@setOnClickListener
                }

                client?.send(
                    TdApi.CheckAuthenticationCode(code)
                ) {
                    runOnUiThread {
                        status.text = "کد بررسی شد..."
                    }
                }
            }
        }

        passwordInput = EditText(this).apply {
            hint = "رمز دومرحله‌ای تلگرام"
            inputType =
                InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
            setSingleLine(true)
        }

        val passwordButton = Button(this).apply {
            text = "تأیید رمز دومرحله‌ای"

            setOnClickListener {
                val password = passwordInput.text.toString()

                if (password.isBlank()) {
                    status.text = "رمز دومرحله‌ای را وارد کن."
                    return@setOnClickListener
                }

                client?.send(
                    TdApi.CheckAuthenticationPassword(password)
                ) {
                    runOnUiThread {
                        status.text = "رمز بررسی شد..."
                    }
                }
            }
        }

        status = TextView(this).apply {
            text = "وضعیت: در انتظار..."
            textSize = 16f
        }

        root.addView(title)
        root.addView(apiIdInput)
        root.addView(apiHashInput)
        root.addView(phoneInput)
        root.addView(loginButton)
        root.addView(codeInput)
        root.addView(codeButton)
        root.addView(passwordInput)
        root.addView(passwordButton)
        root.addView(status)

        setContentView(root)
    }

    private fun startTelegramLogin() {

        val apiIdText = apiIdInput.text.toString().trim()
        val apiHash = apiHashInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()

        if (apiIdText.isBlank() ||
            apiHash.isBlank() ||
            phone.isBlank()
        ) {
            status.text =
                "API ID، API Hash و شماره تلفن را وارد کن."
            return
        }

        val apiId = apiIdText.toIntOrNull()

        if (apiId == null) {
            status.text = "API ID باید عددی باشد."
            return
        }

        parametersSent = false
        phoneSent = false

        status.text = "در حال اتصال به TDLib..."
    }

    private fun handleUpdate(update: TdApi.Object) {

        if (update is TdApi.UpdateAuthorizationState) {

            when (val state = update.authorizationState) {

                is TdApi.AuthorizationStateWaitTdlibParameters -> {

                    if (!parametersSent) {

                        parametersSent = true

                        val apiId =
                            apiIdInput.text.toString()
                                .trim()
                                .toIntOrNull()

                        val apiHash =
                            apiHashInput.text.toString()
                                .trim()

                        if (apiId == null || apiHash.isBlank()) {
                            runOnUiThread {
                                status.text =
                                    "ابتدا API ID و API Hash را وارد کن."
                            }
                            return
                        }

                        val parameters =
                            TdApi.SetTdlibParameters()

                        parameters.useTestDc = false
                        parameters.databaseDirectory =
                            filesDir.absolutePath + "/tdlib"
                        parameters.filesDirectory =
                            filesDir.absolutePath + "/tdlib_files"
                        parameters.databaseEncryptionKey =
                            ByteArray(0)
                        parameters.useFileDatabase = true
                        parameters.useChatInfoDatabase = true
                        parameters.useMessageDatabase = true
                        parameters.useSecretChats = false
                        parameters.apiId = apiId
                        parameters.apiHash = apiHash
                        parameters.systemLanguageCode = "fa"
                        parameters.deviceModel = "Android"
                        parameters.systemVersion =
                            android.os.Build.VERSION.RELEASE
                                ?: "Android"
                        parameters.applicationVersion = "2.1.0"

                        client?.send(parameters) {
                            runOnUiThread {
                                status.text =
                                    "پارامترهای TDLib ارسال شد..."
                            }
                        }
                    }
                }

                is TdApi.AuthorizationStateWaitPhoneNumber -> {

                    if (!phoneSent) {

                        phoneSent = true

                        val phone =
                            phoneInput.text.toString().trim()

                        if (phone.isBlank()) {
                            runOnUiThread {
                                status.text =
                                    "شماره تلفن را وارد کن."
                            }
                            return
                        }

                        client?.send(
                            TdApi.SetAuthenticationPhoneNumber(
                                phone,
                                null
                            )
                        ) {
                            runOnUiThread {
                                status.text =
                                    "کد تأیید تلگرام ارسال شد."
                            }
                        }
                    }
                }

                is TdApi.AuthorizationStateWaitCode -> {
                    runOnUiThread {
                        status.text =
                            "کد ارسال‌شده از تلگرام را وارد کن."
                    }
                }

                is TdApi.AuthorizationStateWaitPassword -> {
                    runOnUiThread {
                        status.text =
                            "رمز دومرحله‌ای تلگرام را وارد کن."
                    }
                }

                is TdApi.AuthorizationStateReady -> {
                    runOnUiThread {
                        status.text =
                            "✅ ورود موفق بود — حساب تلگرام آماده است."
                    }
                }

                is TdApi.AuthorizationStateLoggingOut -> {
                    runOnUiThread {
                        status.text = "در حال خروج..."
                    }
                }

                is TdApi.AuthorizationStateClosing -> {
                    runOnUiThread {
                        status.text = "در حال بستن TDLib..."
                    }
                }

                is TdApi.AuthorizationStateClosed -> {
                    runOnUiThread {
                        status.text = "TDLib بسته شد."
                    }
                }

                else -> Unit
            }
        }
    }

    override fun onDestroy() {
        client?.send(TdApi.Close()) {}
        super.onDestroy()
    }
}
