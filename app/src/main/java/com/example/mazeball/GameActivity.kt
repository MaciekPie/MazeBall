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
import kotlin.math.max
import kotlin.math.min

class GameActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var mazeView: MazeView
    private lateinit var maze: Maze
    private var mazeInitialized = false
    private var ballX = 0f
    private var ballY = 0f
    private var maxX = 0f
    private var maxY = 0f
    private var minX = 0f
    private var minY = 0f
    private var ballRadius = 0f
    private var cellSize = 0f
    private lateinit var pauseOverlay: LinearLayout
    private var isPaused = false // Stan gry

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Lock orientation programmatically
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        setContentView(R.layout.activity_game)

        mazeView = findViewById(R.id.maze_view)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Initiation
        val pauseButton: Button = findViewById(R.id.pause_button)
        val resumeButton: Button = findViewById(R.id.resume_button)
        val menuButton: Button = findViewById(R.id.menu_button)
        val settingsButton: Button = findViewById(R.id.settings_button)
        pauseOverlay = findViewById(R.id.pause_overlay)

        // Checking buttons
        pauseButton.setOnClickListener {
            setPaused(true)
        }

        // Resume
        resumeButton.setOnClickListener {
            setPaused(false)
        }

        // Menu
        menuButton.setOnClickListener {
            finish()
        }

        // Settings
        settingsButton.setOnClickListener {
            //
        }

        // Wait until layout is ready to get screen limits

        mazeView.viewTreeObserver.addOnGlobalLayoutListener {
            if (!mazeInitialized) {
                mazeInitialized = true

                // Wybierz liczbę kolumn (np. 10) i policz wielkość komórki tak, aby zmieścić w pionie całe rzędy
                val cols = 10
                cellSize = mazeView.width / cols.toFloat()
                val rows = max(1, (mazeView.height / cellSize).toInt())

                maze = Maze(cols, rows)
                mazeView.setMaze(maze)

                ballRadius = cellSize * 0.9f
                ballX = (maze.startX + 0.5f) * cellSize
                ballY = (maze.startY + 0.5f) * cellSize

                // Ustal granice (top-left coordinates dla ballX/ballY)
                minX = ballRadius / 2
                minY = ballRadius / 2
                maxX = (mazeView.width - ballRadius / 2)
                maxY = (mazeView.height - ballRadius / 2)

                ballX = ballX.coerceIn(minX, maxX)
                ballY = ballY.coerceIn(minY, maxY)

                // Przekaż labirynt i pozycję do MazeView (załóżmy, że masz setMaze(maze, initialX, initialY))
                mazeView.updateBallPosition(ballX, ballY)
            }
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

            // Move the ball
            ballX += dX
            ballY += dY

            // Apply the boundaries
            ballX = ballX.coerceIn(minX, maxX)
            ballY = ballY.coerceIn(minY, maxY)

            // Check maze wall collisions
            val corrected = checkCollision(ballX, ballY)
            ballX = corrected.first
            ballY = corrected.second

            mazeView.updateBallPosition(ballX, ballY)
        }
    }

    private fun checkCollision(newX: Float, newY: Float): Pair<Float, Float> {
        val cellWidth = mazeView.width / maze.width.toFloat()
        val cellHeight = mazeView.height / maze.height.toFloat()

        val ballR = ballRadius / 2f // promień liczony od środka
        var correctedX = newX
        var correctedY = newY

        // Oblicz w której komórce znajduje się środek kulki
        val cellX = (newX / cellWidth).toInt().coerceIn(0, maze.width - 1)
        val cellY = (newY / cellHeight).toInt().coerceIn(0, maze.height - 1)
        val cell = maze.grid[cellY][cellX]

        // Współrzędne aktualnej komórki
        val left = cellX * cellWidth
        val right = (cellX + 1) * cellWidth
        val top = cellY * cellHeight
        val bottom = (cellY + 1) * cellHeight

        // --- Sprawdzenie kolizji ze ścianami aktualnej lub sąsiednich komórek ---
        val eps = 0.5f // mały margines

        // Lewa ściana
        if (cell.wallLeft && newX - ballR < left) {
            correctedX = left + ballR + eps
        } else if (cellX > 0 && maze.grid[cellY][cellX - 1].wallRight && newX - ballR < left) {
            correctedX = left + ballR + eps
        }

        // Prawa ściana
        if (cell.wallRight && newX + ballR > right) {
            correctedX = right - ballR - eps
        } else if (cellX < maze.width - 1 && maze.grid[cellY][cellX + 1].wallLeft && newX + ballR > right) {
            correctedX = right - ballR - eps
        }

        // Górna ściana
        if (cell.wallTop && newY - ballR < top) {
            correctedY = top + ballR + eps
        } else if (cellY > 0 && maze.grid[cellY - 1][cellX].wallBottom && newY - ballR < top) {
            correctedY = top + ballR + eps
        }

        // Dolna ściana
        if (cell.wallBottom && newY + ballR > bottom) {
            correctedY = bottom - ballR - eps
        } else if (cellY < maze.height - 1 && maze.grid[cellY + 1][cellX].wallTop && newY + ballR > bottom) {
            correctedY = bottom - ballR - eps
        }

        // Ostatecznie ogranicz do ekranu
        correctedX = correctedX.coerceIn(minX, maxX)
        correctedY = correctedY.coerceIn(minY, maxY)

        return correctedX to correctedY
    }

    private fun checkCollisionWithWalls(proposedX: Float, proposedY: Float): Pair<Float, Float> {
        // Obliczamy rozmiar komórki (ten sam sposób jak w setMaze)
        val cellSize = minOf(mazeView.width / maze.width.toFloat(), mazeView.height / maze.height.toFloat())
        val ballRadius = cellSize / 3f
        val centerX = proposedX + ballRadius
        val centerY = proposedY + ballRadius

        // Indeks komórki (na podstawie środka kulki)
        val cellX = (centerX / cellSize).toInt().coerceIn(0, maze.width - 1)
        val cellY = (centerY / cellSize).toInt().coerceIn(0, maze.height - 1)
        val cell = maze.grid[cellY][cellX]

        // Ręczne granice komórki
        val left = cellX * cellSize
        val right = (cellX + 1) * cellSize
        val top = cellY * cellSize
        val bottom = (cellY + 1) * cellSize

        // Margines mały, żeby uniknąć drgania
        val eps = 0.5f

        var correctedCenterX = centerX
        var correctedCenterY = centerY

        // Left wall: jeśli jest i kula próbuje przeniknąć
        if (cell.wallLeft) {
            val minAllowedCenterX = left + ballRadius + eps
            if (correctedCenterX - ballRadius < minAllowedCenterX) correctedCenterX = minAllowedCenterX
        } else {
            // Jeśli nie ma lewej ściany, ale sąsiedni cell po lewej ma right wall — też blokuje
            if (cellX > 0) {
                val leftNeighbor = maze.grid[cellY][cellX - 1]
                if (leftNeighbor.wallRight) {
                    val minAllowedCenterX = left + ballRadius + eps
                    if (correctedCenterX - ballRadius < minAllowedCenterX) correctedCenterX = minAllowedCenterX
                }
            }
        }

        // Right wall
        if (cell.wallRight) {
            val maxAllowedCenterX = right - ballRadius - eps
            if (correctedCenterX + ballRadius > maxAllowedCenterX) correctedCenterX = maxAllowedCenterX
        } else {
            if (cellX < maze.width - 1) {
                val rightNeighbor = maze.grid[cellY][cellX + 1]
                if (rightNeighbor.wallLeft) {
                    val maxAllowedCenterX = right - ballRadius - eps
                    if (correctedCenterX + ballRadius > maxAllowedCenterX) correctedCenterX = maxAllowedCenterX
                }
            }
        }

        // Top wall
        if (cell.wallTop) {
            val minAllowedCenterY = top + ballRadius + eps
            if (correctedCenterY - ballRadius < minAllowedCenterY) correctedCenterY = minAllowedCenterY
        } else {
            if (cellY > 0) {
                val topNeighbor = maze.grid[cellY - 1][cellX]
                if (topNeighbor.wallBottom) {
                    val minAllowedCenterY = top + ballRadius + eps
                    if (correctedCenterY - ballRadius < minAllowedCenterY) correctedCenterY = minAllowedCenterY
                }
            }
        }

        // Bottom wall
        if (cell.wallBottom) {
            val maxAllowedCenterY = bottom - ballRadius - eps
            if (correctedCenterY + ballRadius > maxAllowedCenterY) correctedCenterY = maxAllowedCenterY
        } else {
            if (cellY < maze.height - 1) {
                val bottomNeighbor = maze.grid[cellY + 1][cellX]
                if (bottomNeighbor.wallTop) {
                    val maxAllowedCenterY = bottom - ballRadius - eps
                    if (correctedCenterY + ballRadius > maxAllowedCenterY) correctedCenterY = maxAllowedCenterY
                }
            }
        }

        // Zamieniamy center na top-left (bo Twoje updateBallPosition oczekuje top-left)
        val correctedX = (correctedCenterX - ballRadius).coerceIn(minX, maxX)
        val correctedY = (correctedCenterY - ballRadius).coerceIn(minY, maxY)

        return correctedX to correctedY
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
