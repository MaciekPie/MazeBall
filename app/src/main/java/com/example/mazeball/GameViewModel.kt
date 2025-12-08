package com.example.mazeball

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlin.math.max
import kotlin.math.min

class GameViewModel : ViewModel() {

    private val _maze = MutableLiveData<Maze?>()
    val maze: LiveData<Maze?> = _maze

    private val _ballX = MutableLiveData<Float>()
    val ballX: LiveData<Float> = _ballX

    private val _ballY = MutableLiveData<Float>()
    val ballY: LiveData<Float> = _ballY

    private val _isPaused = MutableLiveData(false)
    val isPaused: LiveData<Boolean> = _isPaused

    // Emit true once when level is won. Activity should react and then optionally reset it.
    private val _levelWon = MutableLiveData<Boolean>(false)
    val levelWon: LiveData<Boolean> = _levelWon

    // Internal geometry
    private var ballRadius = 0f
    private var minX = 0f
    private var maxX = 0f
    private var minY = 0f
    private var maxY = 0f
    private var cellSize = 0f

    // Sensitivity factor (you can expose setter if needed)
    private val SPEED_FACTOR = 3.5f

    /**
     * Initialize a freshly generated maze (cols x rows) using screen dimensions to compute geometry.
     */
    fun initMaze(cols: Int, rows: Int, viewWidth: Int, viewHeight: Int) {
        val m = Maze(cols, rows)
        setupMazeAndGeometry(m, viewWidth, viewHeight)
    }

    /**
     * Initialize maze from previously saved Level.
     */
    fun initMazeFromLevel(level: Level, viewWidth: Int, viewHeight: Int) {
        // Create a Maze of matching size, then overwrite grid & start/goal from level
        val m = Maze(level.width, level.height)
        // Overwrite grid cells according to Level.grid (CellState -> Cell)
        for (y in 0 until level.height) {
            for (x in 0 until level.width) {
                val state = level.grid[y][x]
                val cell = m.grid[y][x]
                cell.wallTop = state.wallTop
                cell.wallBottom = state.wallBottom
                cell.wallLeft = state.wallLeft
                cell.wallRight = state.wallRight
                cell.isVisited = false
            }
        }
        m.startX = level.startX
        m.startY = level.startY
        m.goalX = level.goalX
        m.goalY = level.goalY

        setupMazeAndGeometry(m, viewWidth, viewHeight)
    }

    private fun setupMazeAndGeometry(m: Maze, viewWidth: Int, viewHeight: Int) {
        _maze.value = m

        // Compute sizes similarly to previous approach:
        val cols = m.width
        cellSize = viewWidth.toFloat() / cols.toFloat()
        ballRadius = cellSize * 0.9f

        // initial ball center at start cell center
        val initialX = (m.startX + 0.5f) * cellSize
        val initialY = (m.startY + 0.5f) * cellSize

        minX = ballRadius / 2
        minY = ballRadius / 2
        maxX = (viewWidth - ballRadius / 2)
        maxY = (viewHeight - ballRadius / 2)

        _ballX.value = initialX.coerceIn(minX, maxX)
        _ballY.value = initialY.coerceIn(minY, maxY)

        _isPaused.value = false
        _levelWon.value = false
    }

    /**
     * Called by Activity when accelerometer reports change.
     * ViewModel computes new position, handles collisions and win detection.
     */
    fun onAccelerometer(accX: Float, accY: Float) {
        if (_isPaused.value == true) return
        val currentX = _ballX.value ?: return
        val currentY = _ballY.value ?: return
        val dX = -accX * SPEED_FACTOR
        val dY = accY * SPEED_FACTOR

        var newX = currentX + dX
        var newY = currentY + dY

        // clamp to screen
        newX = newX.coerceIn(minX, maxX)
        newY = newY.coerceIn(minY, maxY)

        // collision correction
        val corrected = checkCollision(newX, newY)
        newX = corrected.first
        newY = corrected.second

        _ballX.value = newX
        _ballY.value = newY

        checkWinCondition()
    }

    fun setPaused(paused: Boolean) {
        _isPaused.value = paused
    }

    /**
     * Simple collision detection ported from previous GameActivity implementation.
     */
    private fun checkCollision(newX: Float, newY: Float): Pair<Float, Float> {
        val m = _maze.value ?: return newX to newY

        val cellWidth = (cellSize) // we computed cellSize using viewWidth/cols
        val cellHeight = (cellSize) // cells are square

        val ballR = ballRadius / 2f
        var correctedX = newX
        var correctedY = newY

        val cellX = (newX / cellWidth).toInt().coerceIn(0, m.width - 1)
        val cellY = (newY / cellHeight).toInt().coerceIn(0, m.height - 1)
        val cell = m.grid[cellY][cellX]

        val left = cellX * cellWidth
        val right = (cellX + 1) * cellWidth
        val top = cellY * cellHeight
        val bottom = (cellY + 1) * cellHeight

        val eps = 0.5f

        // Left wall
        if (cell.wallLeft && newX - ballR < left) {
            correctedX = left + ballR + eps
        } else if (cellX > 0 && m.grid[cellY][cellX - 1].wallRight && newX - ballR < left) {
            correctedX = left + ballR + eps
        }

        // Right wall
        if (cell.wallRight && newX + ballR > right) {
            correctedX = right - ballR - eps
        } else if (cellX < m.width - 1 && m.grid[cellY][cellX + 1].wallLeft && newX + ballR > right) {
            correctedX = right - ballR - eps
        }

        // Top
        if (cell.wallTop && newY - ballR < top) {
            correctedY = top + ballR + eps
        } else if (cellY > 0 && m.grid[cellY - 1][cellX].wallBottom && newY - ballR < top) {
            correctedY = top + ballR + eps
        }

        // Bottom
        if (cell.wallBottom && newY + ballR > bottom) {
            correctedY = bottom - ballR - eps
        } else if (cellY < m.height - 1 && m.grid[cellY + 1][cellX].wallTop && newY + ballR > bottom) {
            correctedY = bottom - ballR - eps
        }

        correctedX = correctedX.coerceIn(minX, maxX)
        correctedY = correctedY.coerceIn(minY, maxY)

        return correctedX to correctedY
    }

    private fun checkWinCondition() {
        val m = _maze.value ?: return
        val bx = _ballX.value ?: return
        val by = _ballY.value ?: return

        val cellWidth = cellSize
        val cellHeight = cellSize

        val cellX = (bx / cellWidth).toInt().coerceIn(0, m.width - 1)
        val cellY = (by / cellHeight).toInt().coerceIn(0, m.height - 1)

        if (cellX == m.goalX && cellY == m.goalY) {
            onLevelWon()
        }
    }

    private fun onLevelWon() {
        // stop movement logically
        _isPaused.value = true
        _levelWon.value = true
    }

    /**
     * Activity can call this to reset the level-won flag after handling it.
     */
    fun resetLevelWonFlag() {
        _levelWon.value = false
    }
}
