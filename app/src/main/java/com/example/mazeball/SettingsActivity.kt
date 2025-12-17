package com.example.mazeball

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import androidx.core.content.edit

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val bgGroup = findViewById<RadioGroup>(R.id.bgColorGroup)
        val ballGroup = findViewById<RadioGroup>(R.id.ballColorGroup)
        val backButton = findViewById<Button>(R.id.backButton)

        loadSettings(bgGroup, ballGroup)

        // Background
        findViewById<RadioButton>(R.id.bgBlue).setOnClickListener { saveBgColor("BLUE") }
        findViewById<RadioButton>(R.id.bgGreen).setOnClickListener { saveBgColor("GREEN") }
        findViewById<RadioButton>(R.id.bgRed).setOnClickListener { saveBgColor("RED") }
        findViewById<RadioButton>(R.id.bgWhite).setOnClickListener { saveBgColor("WHITE") }
        findViewById<RadioButton>(R.id.bgCream).setOnClickListener { saveBgColor("CREAM") }
        findViewById<RadioButton>(R.id.bgOrange).setOnClickListener { saveBgColor("ORANGE") }

        // Ball
        findViewById<RadioButton>(R.id.ballBlue).setOnClickListener { saveBallColor("BLUE") }
        findViewById<RadioButton>(R.id.ballGreen).setOnClickListener { saveBallColor("GREEN") }
        findViewById<RadioButton>(R.id.ballRed).setOnClickListener { saveBallColor("RED") }
        findViewById<RadioButton>(R.id.ballWhite).setOnClickListener { saveBallColor("WHITE") }
        findViewById<RadioButton>(R.id.ballCream).setOnClickListener { saveBallColor("CREAM") }
        findViewById<RadioButton>(R.id.ballOrange).setOnClickListener { saveBallColor("ORANGE") }

        backButton.setOnClickListener { finish() }
    }

    private fun loadSettings(bgGroup: RadioGroup, ballGroup: RadioGroup) {
        val prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)
        val bgColor = prefs.getString("bg_color", "WHITE") ?: "WHITE"
        val ballColor = prefs.getString("ball_color", "RED") ?: "RED"

        bgGroup.check(getBgRadioId(bgColor))
        ballGroup.check(getBallRadioId(ballColor))
    }

    private fun saveBgColor(colorKey: String) {
        val prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)
        prefs.edit {
            putString("bg_color", colorKey)
        }
    }

    private fun saveBallColor(colorKey: String) {
        val prefs = getSharedPreferences("settings_prefs", MODE_PRIVATE)
        val bgColor = prefs.getString("bg_color", "WHITE") ?: "WHITE"

        if (colorKey == bgColor) {
            Toast.makeText(this, "Kolor piłki nie może być taki sam jak tło", Toast.LENGTH_SHORT).show()
            return
        }

        prefs.edit {
            putString("ball_color", colorKey)
        }
    }

    private fun getBgRadioId(colorKey: String): Int {
        return when (colorKey) {
            "BLUE" -> R.id.bgBlue
            "GREEN" -> R.id.bgGreen
            "RED" -> R.id.bgRed
            "WHITE" -> R.id.bgWhite
            "CREAM" -> R.id.bgCream
            "ORANGE" -> R.id.bgOrange
            else -> View.NO_ID
        }
    }

    private fun getBallRadioId(colorKey: String): Int {
        return when (colorKey) {
            "BLUE" -> R.id.ballBlue
            "GREEN" -> R.id.ballGreen
            "RED" -> R.id.ballRed
            "WHITE" -> R.id.ballWhite
            "CREAM" -> R.id.ballCream
            "ORANGE" -> R.id.ballOrange
            else -> View.NO_ID
        }
    }
}
