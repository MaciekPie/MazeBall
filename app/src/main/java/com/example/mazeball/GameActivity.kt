package com.example.mazeball

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import kotlin.math.max
import kotlin.math.min
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var mazeView: MazeView
    private lateinit var pauseOverlay: LinearLayout
    private var mazeInitialized = false

    private val viewModel: GameViewModel by viewModels()

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
            viewModel.setPaused(true) // setPaused(true)
        }

        // Resume
        resumeButton.setOnClickListener {
            viewModel.setPaused(false) // setPaused(true)
        }

        // Menu
        menuButton.setOnClickListener {
            finish()
        }

        // Settings
        settingsButton.setOnClickListener {
            // TODO
        }

        // Observers
        viewModel.maze.observe(this) { m ->
            m?.let { mazeView.setMaze(it) }
        }

        viewModel.ballX.observe(this) { x ->
            val y = viewModel.ballY.value ?: return@observe
            mazeView.updateBallPosition(x, y)
        }

        viewModel.ballY.observe(this) { y ->
            val x = viewModel.ballX.value ?: return@observe
            mazeView.updateBallPosition(x, y)
        }

        viewModel.isPaused.observe(this) { paused ->
            if (paused) {
                pauseOverlay.visibility = View.VISIBLE
                sensorManager.unregisterListener(this)
            } else {
                pauseOverlay.visibility = View.GONE
                // Register sensor (only if activity is resumed)
                if (!isFinishing) {
                    sensorManager.registerListener(
                        this,
                        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                        SensorManager.SENSOR_DELAY_GAME
                    )
                }
            }
        }

        viewModel.levelWon.observe(this) { won ->
            if (won == true) {
                // Download the maze from this level
                val maze = viewModel.maze.value
                if (maze != null) {
                    val storage = LevelStorage(this)

                    // Check LEVEL_ID if there is existing
                    val existingId = intent.getIntExtra("LEVEL_ID", -1)
                    val levelId = if (existingId != -1) {
                        existingId
                    } else {
                        generateNextLevelId(storage)
                    }

                    val level = buildLevelFromMaze(levelId, maze)
                    storage.addLevel(level)   // TU zapis do SharedPreferences
                }
                Toast.makeText(this, "You won!", Toast.LENGTH_SHORT).show()
                // small delay to let user see toast (postDelayed)
                mazeView.postDelayed({
                    val intent = Intent(this, LevelSelectActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }, 1000)

                viewModel.resetLevelWonFlag()
            }
        }

        // Layout ready -> initialize maze in ViewModel (and restore a saved level if passed)
        mazeView.viewTreeObserver.addOnGlobalLayoutListener {
            if (!mazeInitialized && mazeView.width > 0 && mazeView.height > 0) {
                mazeInitialized = true

                val intentLevelId = intent.getIntExtra("LEVEL_ID", -1)
                if (intentLevelId != -1) {
                    // load level from storage and pass into viewModel
                    val storage = LevelStorage(this)
                    val level = storage.getLevelById(intentLevelId)
                    if (level != null) {
                        viewModel.initMazeFromLevel(level, mazeView.width, mazeView.height)
                    } else {
                        // fallback to generated maze
                        viewModel.initMaze(10, maxOf(1, (mazeView.height / (mazeView.width / 10f)).toInt()), mazeView.width, mazeView.height)
                    }
                } else {
                    val cols = 10
                    val rows = maxOf(1, (mazeView.height / (mazeView.width / cols.toFloat())).toInt())
                    viewModel.initMaze(cols, rows, mazeView.width, mazeView.height)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register sensor if not paused
        if (viewModel.isPaused.value != true) {
            sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_GAME
            )
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val accX = event.values[0]
            val accY = event.values[1]
            // Delegate movement handling to ViewModel
            viewModel.onAccelerometer(accX, accY)
        }
    }

    private fun buildLevelFromMaze(id: Int, maze: Maze): Level {
        val gridState = mutableListOf<MutableList<CellState>>()

        for (y in 0 until maze.height) {
            val row = mutableListOf<CellState>()
            for (x in 0 until maze.width) {
                val cell = maze.grid[y][x]
                row.add(
                    CellState(
                        wallTop = cell.wallTop,
                        wallBottom = cell.wallBottom,
                        wallLeft = cell.wallLeft,
                        wallRight = cell.wallRight
                    )
                )
            }
            gridState.add(row)
        }

        return Level(
            id = id,
            width = maze.width,
            height = maze.height,
            startX = maze.startX,
            startY = maze.startY,
            goalX = maze.goalX,
            goalY = maze.goalY,
            grid = gridState
        )
    }

    private fun generateNextLevelId(storage: LevelStorage): Int {
        val levels = storage.getLevels()
        val maxId = levels.maxOfOrNull { it.id } ?: 0
        return maxId + 1
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
