package com.example.mazeball


data class Level(
    val id: Int,
    val width: Int,
    val height: Int,
    val startX: Int,
    val startY: Int,
    val goalX: Int,
    val goalY: Int,
    val grid: List<List<CellState>>
)

data class CellState(
    val wallTop: Boolean,
    val wallBottom: Boolean,
    val wallLeft: Boolean,
    val wallRight: Boolean
)
