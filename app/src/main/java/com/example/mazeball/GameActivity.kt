package com.example.mazeball

import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var ball: View
    private var ballX = 0f
    private var ballY = 0f
    private var maxX = 0f
    private var maxY = 0f
    private var minX = 0f
    private var minY = 0f
    private lateinit var pauseOverlay: LinearLayout
    private var isPaused = false // Stan gry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock orientation programmatically
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContentView(R.layout.activity_game)

        ball = findViewById(R.id.ball)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Inicjalizacja nowych widoków
        val pauseButton: Button = findViewById(R.id.pause_button)
        val resumeButton: Button = findViewById(R.id.resume_button)
        val menuButton: Button = findViewById(R.id.menu_button)
        val settingsButton: Button = findViewById(R.id.settings_button)
        pauseOverlay = findViewById(R.id.pause_overlay)

        // Nasłuchiwanie przycisków
        pauseButton.setOnClickListener {
            setPaused(true)
        }
        resumeButton.setOnClickListener {
            setPaused(false)
        }
        menuButton.setOnClickListener {
            // Tutaj logika powrotu do menu (np. finish() lub Intent)
            finish()
        }
        settingsButton.setOnClickListener {
            // Tutaj logika otwierania ustawień
            // Możesz wyświetlić Toast, na razie
        }

        // Wait until layout is ready to get screen limits
        val content = ball.parent as View
        content.viewTreeObserver.addOnGlobalLayoutListener {
            maxX = (content.width / 2 - ball.width / 2).toFloat()
            maxY = (content.height / 2 - ball.height / 2).toFloat()

            minX = (- content.width / 2 + ball.width / 2).toFloat()
            minY = (- content.height / 2 + ball.height / 2).toFloat()
        }
    }

    private fun setPaused(paused: Boolean) {
        isPaused = paused
        if (isPaused) {
            // ZATRZYMANIE RUCHU: Ukrywamy nakładkę i wyrejestrowujemy sensor
            pauseOverlay.visibility = View.VISIBLE
            sensorManager.unregisterListener(this)
        } else {
            // WZNOWIENIE RUCHU: Ukrywamy nakładkę i rejestrujemy sensor
            pauseOverlay.visibility = View.GONE
            sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {

            // Odczyt z akcelerometru
            val accX = event.values[0]
            val accY = event.values[1]

            // Stała prędkość/czułość - możesz ją dostosować
            val SPEED_FACTOR = 3.5f

            // W Androidzie:
            // - Wzrost X to RUCH W PRAWO
            // - Wzrost Y to RUCH W DÓŁ

            // Ruch X: Przechylenie w PRAWO (ujemne accX) ma przesunąć kulkę W PRAWO (dodatnie dX)
            // Ruch Y: Przechylenie DO PRZODU (ujemne accY) ma przesunąć kulkę W GÓRĘ (ujemne dY)

            //
            val dX = -accX * SPEED_FACTOR
            val dY = accY * SPEED_FACTOR // Wystarczy odwrócić Y, tak by ujemne accY (pochylenie do przodu) dawało ujemne dY (ruch w górę)

            //
            ballX += dX
            ballY += dY

            //
            ballX = ballX.coerceIn(minX, maxX)
            ballY = ballY.coerceIn(minY, maxY)

            //
            ball.translationX = ballX
            ball.translationY = ballY
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
