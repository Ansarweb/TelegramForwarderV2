package com.ansarweb.telegramforwarderv2

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = TextView(this).apply {
            text = "Telegram Forwarder V2"
            textSize = 24f
        }

        val usernameInput = EditText(this).apply {
            hint = "@username"
            singleLine = true
        }

        val status = TextView(this).apply {
            text = "آماده"
            textSize = 16f
        }

        val checkButton = Button(this).apply {
            text = "بررسی Username"

            setOnClickListener {
                val value = usernameInput.text.toString().trim()

                status.text = when {
                    value.isBlank() ->
                        "لطفاً Username را وارد کنید."

                    value.startsWith("@") ->
                        "مقصد: $value"

                    else ->
                        "مقصد: @$value"
                }
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)

            addView(title)
            addView(usernameInput)
            addView(checkButton)
            addView(status)
        }

        setContentView(root)
    }
}
