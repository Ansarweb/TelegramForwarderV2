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

    private var client: Client? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Client.execute(
            TdApi.SetLogVerbosityLevel(0)
        )

        buildUi()

        Client.create(
            { update ->
                handleUpdate(update)
            },
            { error ->
                runOnUiThread {
                    status.text = "خطای TDLib: ${error.message}"
                }
            },
            null
        ).also {
            client = it
        }
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
            text = "ورود به تلگرام"

            setOnClickListener {
                startTelegramLogin()
            }
        }

        status = TextView(this).apply {
            text = "وضعیت: آماده"
            textSize = 16f
        }

        root.addView(title)
        root.addView(apiIdInput)
        root.addView(apiHashInput)
        root.addView(phoneInput)
        root.addView(loginButton)
        root.addView(status)

        setContentView(root)
    }

    private fun startTelegramLogin() {

        val apiId = apiIdInput.text.toString().trim()
        val apiHash = apiHashInput.text.toString().trim()
        val phone = phoneInput.text.toString().trim()

        if (apiId.isBlank() || apiHash.isBlank() || phone.isBlank()) {
            status.text = "API ID، API Hash و شماره تلفن را وارد کن."
            return
        }

        val id = apiId.toIntOrNull()

        if (id == null) {
            status.text = "API ID باید عددی باشد."
            return
        }

        client?.send(
            TdApi.SetTdlibParameters(
                false,
                true,
                true,
                false,
                "",
                "",
                "",
                "fa",
                "Telegram Forwarder V2",
                "2.1.0",
                "Android",
                "16",
                id,
                apiHash
            )
        ) {
            runOnUiThread {
                status.text = "درخواست ورود ارسال شد..."
            }
        }

        client?.send(
            TdApi.SetAuthenticationPhoneNumber(
                phone,
                null
            )
        ) {
            runOnUiThread {
                status.text = "کد تأیید تلگرام را وارد کن."
            }
        }
    }

    private fun handleUpdate(update: TdApi.Object) {

        if (update is TdApi.UpdateAuthorizationState) {

            when (val state = update.authorizationState) {

                is TdApi.AuthorizationStateWaitPhoneNumber -> {
                    runOnUiThread {
                        status.text = "شماره تلفن را وارد کن."
                    }
                }

                is TdApi.AuthorizationStateWaitCode -> {
                    runOnUiThread {
                        status.text =
                            "کد ارسال‌شده از طرف تلگرام را وارد کن."
                    }
                }

                is TdApi.AuthorizationStateWaitPassword -> {
                    runOnUiThread {
                        status.text =
                            "رمز دومرحله‌ای تلگرام لازم است."
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
