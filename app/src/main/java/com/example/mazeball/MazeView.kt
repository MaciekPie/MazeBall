package com.example.mazeball

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class MazeView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    // --- Rysowanie: Obiekty Paint ---
    private val wallPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 6f // Grubość ściany
        style = Paint.Style.STROKE
        isAntiAlias = true // Wygładzanie
    }

    private val ballPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val startPaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }

    private val finishPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
    }

    // --- Model Danych i Wymiary ---
    private var maze: Maze? = null
    private var cellSize = 0f // Rozmiar jednej komórki w pikselach
    private var ballRadius = 0f

    // Pozycja kulki (przekazywana z GameActivity)
    var ballXCenter = 0f // Środek X
    var ballYCenter = 0f // Środek Y

    // Ustawia labirynt, wylicza rozmiary i wymusza rysowanie
    fun setMaze(newMaze: Maze) {
        maze = newMaze

        // Compute cell size after the view has been measured
        post {
            maze?.let { m ->
                cellSize = height.toFloat() / m.height
                ballRadius = cellSize / 3f

                // Set ball in start cell center
                ballXCenter = (m.startX + 0.5f) * cellSize
                ballYCenter = (m.startY + 0.5f) * cellSize

                invalidate()
            }
        }
    }

    // Ta metoda jest wywoływana z GameActivity do aktualizacji pozycji
    fun updateBallPosition(newX: Float, newY: Float) {
        ballXCenter = newX
        ballYCenter = newY

        // Safety clamp (ball never leaves screen)
        ballXCenter = ballXCenter.coerceIn(ballRadius, width - ballRadius)
        ballYCenter = ballYCenter.coerceIn(ballRadius, height - ballRadius)

        invalidate()
    }

    // --- Rysowanie ---

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        maze?.let { m ->
            // Draw maze walls
            for (y in 0 until m.height) {
                for (x in 0 until m.width) {
                    // Wyśrodkuj labirynt w poziomie
                    val offsetX = (width - (m.width * cellSize)) / 2f
                    val offsetY = 0f

                    val cell = m.grid[y][x]

                    val left = offsetX + x * cellSize
                    val top = offsetY + y * cellSize
                    val right = (x + 1) * cellSize
                    val bottom = (y + 1) * cellSize

                    // Start cell
                    if (x == m.startX && y == m.startY) {
                        canvas.drawRect(left, top, right, bottom, startPaint)
                    }

                    // Goal cell
                    if (x == m.goalX && y == m.goalY) {
                        canvas.drawRect(left, top, right, bottom, finishPaint)
                    }

                    // Walls
                    if (cell.wallTop) canvas.drawLine(left, top, right, top, wallPaint)
                    if (cell.wallBottom) canvas.drawLine(left, bottom, right, bottom, wallPaint)
                    if (cell.wallLeft) canvas.drawLine(left, top, left, bottom, wallPaint)
                    if (cell.wallRight) canvas.drawLine(right, top, right, bottom, wallPaint)
                }
            }

            // Draw the ball
            canvas.drawCircle(ballXCenter, ballYCenter, ballRadius, ballPaint)
        }
    }
}
