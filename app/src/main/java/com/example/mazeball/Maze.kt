package com.example.mazeball

import java.util.*
import kotlin.random.Random

// Class for dividing screen into cells
data class Cell(
    var isVisited: Boolean = false,
    var wallTop: Boolean = true,
    var wallBottom: Boolean = true,
    var wallLeft: Boolean = true,
    var wallRight: Boolean = true
)

// Labyrinth class
class Maze(val width: Int, val height: Int) {
    val grid: Array<Array<Cell>> = Array(height) { Array(width) { Cell() } }

    // Start and goal coordinates
    var startX = 0
    var startY = 0
    var goalX = width - 1
    var goalY = height - 1

    init {
        generateDFS()
        chooseRandomStartOnBorder()
        findFurthestGoal()
    }

    private fun chooseRandomStartOnBorder() {
        val rand = Random(System.currentTimeMillis())
        when (rand.nextInt(4)) {
            0 -> { // lewa krawędź
                startX = 0
                startY = rand.nextInt(height)
            }
            1 -> { // prawa krawędź
                startX = width - 1
                startY = rand.nextInt(height)
            }
            2 -> { // górna krawędź
                startX = rand.nextInt(width)
                startY = 0
            }
            3 -> { // dolna krawędź
                startX = rand.nextInt(width)
                startY = height - 1
            }
        }
    }

    private fun generateDFS() {
        val stack: Stack<Pair<Int, Int>> = Stack()
        val random = Random(System.currentTimeMillis())

        var currentX = 0
        var currentY = 0
        grid[currentY][currentX].isVisited = true
        stack.push(currentX to currentY)

        while (stack.isNotEmpty()) {
            val (x, y) = stack.peek()
            val neighbors = mutableListOf<Pair<Int, Int>>()

            if (x > 0 && !grid[y][x - 1].isVisited) neighbors.add(x - 1 to y)
            if (x < width - 1 && !grid[y][x + 1].isVisited) neighbors.add(x + 1 to y)
            if (y > 0 && !grid[y - 1][x].isVisited) neighbors.add(x to y - 1)
            if (y < height - 1 && !grid[y + 1][x].isVisited) neighbors.add(x to y + 1)

            if (neighbors.isNotEmpty()) {
                val (nextX, nextY) = neighbors.random(random)
                removeWall(x, y, nextX, nextY)
                currentX = nextX
                currentY = nextY
                grid[currentY][currentX].isVisited = true
                stack.push(currentX to currentY)
            } else {
                stack.pop()
            }
        }

        // Reset visited state for BFS
        for (row in grid) for (cell in row) cell.isVisited = false
    }

    // Use BFS to find the furthest reachable cell from (startX, startY)
    private fun findFurthestGoal() {
        val queue: Queue<Triple<Int, Int, Int>> = LinkedList()
        val visited = Array(height) { BooleanArray(width) }
        queue.add(Triple(startX, startY, 0))
        visited[startY][startX] = true

        var maxDist = 0
        var farthest = startX to startY

        while (queue.isNotEmpty()) {
            val (x, y, dist) = queue.remove()
            if (dist > maxDist) {
                maxDist = dist
                farthest = x to y
            }

            val cell = grid[y][x]
            // Move in open directions only
            if (!cell.wallTop && !visited[y - 1][x]) {
                queue.add(Triple(x, y - 1, dist + 1))
                visited[y - 1][x] = true
            }
            if (!cell.wallBottom && !visited[y + 1][x]) {
                queue.add(Triple(x, y + 1, dist + 1))
                visited[y + 1][x] = true
            }
            if (!cell.wallLeft && !visited[y][x - 1]) {
                queue.add(Triple(x - 1, y, dist + 1))
                visited[y][x - 1] = true
            }
            if (!cell.wallRight && !visited[y][x + 1]) {
                queue.add(Triple(x + 1, y, dist + 1))
                visited[y][x + 1] = true
            }
        }

        goalX = farthest.first
        goalY = farthest.second
    }

    // Usuwa ścianę między dwoma komórkami
    private fun removeWall(x1: Int, y1: Int, x2: Int, y2: Int) {
        if (x1 == x2) {
            if (y1 > y2) {
                grid[y1][x1].wallTop = false
                grid[y2][x2].wallBottom = false
            } else {
                grid[y1][x1].wallBottom = false
                grid[y2][x2].wallTop = false
            }
        } else {
            if (x1 > x2) {
                grid[y1][x1].wallLeft = false
                grid[y2][x2].wallRight = false
            } else {
                grid[y1][x1].wallRight = false
                grid[y2][x2].wallLeft = false
            }
        }
    }
}
