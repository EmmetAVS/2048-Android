package com.example.a2048

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import java.util.Timer
import kotlin.collections.mutableListOf
import kotlin.concurrent.schedule
import kotlin.math.log2

class BoardItem(var row: Int, var col: Int, var value: Int = 0) {

    var newRow = row
    var newCol = col
    var moving = false
    var id = BoardItem.id ++

    fun moveTo(newRow: Int, newCol: Int) {
        this.newCol = newCol
        this.newRow = newRow
        this.moving = true
    }

    fun resetPosition() {
        this.row = this.newRow
        this.col = this.newCol
        this.moving = false
    }

    companion object {

        var id = 0
        fun genBoard(rows: Int, cols: Int): MutableList<MutableList<BoardItem>> {
            val board: MutableList<MutableList<BoardItem>> = mutableListOf()

            for (row in 0..(rows - 1)) {
                val currentRow: MutableList<BoardItem> = mutableListOf()

                for (col in 0..(cols - 1)) {
                    currentRow.add(BoardItem(row, col))
                }

                board.add(currentRow)
            }

            return board
        }
    }
}

class Model {

    public enum class MoveDirection {
        Left,
        Right,
        Up,
        Down
    }

    var board: MutableList<MutableList<BoardItem>> = BoardItem.genBoard(4, 4)
    var score: Int = 0
    var gameover: Boolean = false
    var version by mutableIntStateOf(0)
    public var newTiles: MutableList<Pair<Int, Int>> = mutableListOf()
    var animating: Boolean = false

    init {
        Log.d("", board.toString())
        placeNewTile()
        placeNewTile()
        newTiles = mutableListOf()
    }

    fun getColor(value: Int): Color {
        val hue = (30 * log2(value.toDouble())) % 360
        return Color.hsv(hue.toFloat(), 0.4f, 0.9f)
    }

    fun handleMove(direction: MoveDirection) {
        if (gameover || animating)
            return

        val madeChanges = when (direction) {
            MoveDirection.Left -> moveLeft()
            MoveDirection.Right -> moveRight()
            MoveDirection.Up -> moveUp()
            MoveDirection.Down -> moveDown()
        }

        if (madeChanges) {
            animating = true
            Timer().schedule(Constants.Numerical.ANIMATION_TIME_DELAY) {
                for (r in 0..3) {
                    for (c in 0..3) {
                        board[r][c].newRow = r;
                        board[r][c].newCol = c;
                        board[r][c].resetPosition()
                    }
                }

                animating = false
                placeNewTile()
                gameover = checkGameOver()
                version++
            }
            version++
        }
    }

    private fun moveLeft(): Boolean {
        var madeChanges = false

        for (r in 0..3) {
            val row = mutableListOf<BoardItem>()

            for (c in 0..3) {
                if (board[r][c].value != 0) {
                    row.add(board[r][c])
                }
            }

            val merged = mutableListOf<BoardItem>()
            var i = 0
            while (i < row.size) {
                if (i + 1 < row.size && row[i].value == row[i + 1].value) {
                    //Merge
                    val mergedTile = BoardItem(r, merged.size, row[i].value * 2)
                    merged.add(mergedTile)
                    score += mergedTile.value

                    row[i].moveTo(r, merged.size - 1)
                    row[i + 1].moveTo(r, merged.size - 1)

                    i += 2
                } else {
                    val newTile = BoardItem(r, merged.size, row[i].value)
                    merged.add(newTile)

                    if (row[i].col != merged.size - 1) {
                        row[i].moveTo(r, merged.size - 1)
                    }

                    i++
                }
            }

            for (c in 0..3) {
                val newValue = if (c < merged.size) merged[c].value else 0
                if (board[r][c].value != newValue) {
                    madeChanges = true
                }
                board[r][c].value = newValue
            }
        }

        return madeChanges
    }

    private fun moveRight(): Boolean {
        var madeChanges = false

        for (r in 0..3) {
            val row = mutableListOf<BoardItem>()

            for (c in 3 downTo 0) {
                if (board[r][c].value != 0) {
                    row.add(board[r][c])
                }
            }

            val merged = mutableListOf<BoardItem>()
            var i = 0
            while (i < row.size) {
                if (i + 1 < row.size && row[i].value == row[i + 1].value) {
                    val mergedTile = BoardItem(r, 3 - merged.size, row[i].value * 2)
                    merged.add(mergedTile)
                    score += mergedTile.value

                    row[i].moveTo(r, 3 - merged.size + 1)
                    row[i + 1].moveTo(r, 3 - merged.size + 1)

                    i += 2
                } else {
                    val newTile = BoardItem(r, 3 - merged.size, row[i].value)
                    merged.add(newTile)

                    if (row[i].col != 3 - merged.size + 1) {
                        row[i].moveTo(r, 3 - merged.size + 1)
                    }

                    i++
                }
            }

            for (c in 0..3) {
                val newValue = if (3 - c < merged.size) merged[3 - c].value else 0
                if (board[r][c].value != newValue) {
                    madeChanges = true
                }
                board[r][c].value = newValue
            }
        }

        return madeChanges
    }

    private fun moveUp(): Boolean {
        var madeChanges = false

        for (c in 0..3) {
            val col = mutableListOf<BoardItem>()

            for (r in 0..3) {
                if (board[r][c].value != 0) {
                    col.add(board[r][c])
                }
            }

            val merged = mutableListOf<BoardItem>()
            var i = 0
            while (i < col.size) {
                if (i + 1 < col.size && col[i].value == col[i + 1].value) {

                    val mergedTile = BoardItem(merged.size, c, col[i].value * 2)
                    merged.add(mergedTile)
                    score += mergedTile.value

                    col[i].moveTo(merged.size - 1, c)
                    col[i + 1].moveTo(merged.size - 1, c)

                    i += 2
                } else {
                    val newTile = BoardItem(merged.size, c, col[i].value)
                    merged.add(newTile)

                    if (col[i].row != merged.size - 1) {
                        col[i].moveTo(merged.size - 1, c)
                    }

                    i++
                }
            }

            for (r in 0..3) {
                val newValue = if (r < merged.size) merged[r].value else 0
                if (board[r][c].value != newValue) {
                    madeChanges = true
                }
                board[r][c].value = newValue
            }
        }

        return madeChanges
    }

    private fun moveDown(): Boolean {
        var madeChanges = false

        for (c in 0..3) {
            val col = mutableListOf<BoardItem>()

            for (r in 3 downTo 0) {
                if (board[r][c].value != 0) {
                    col.add(board[r][c])
                }
            }

            val merged = mutableListOf<BoardItem>()
            var i = 0
            while (i < col.size) {
                if (i + 1 < col.size && col[i].value == col[i + 1].value) {
                    val mergedTile = BoardItem(3 - merged.size, c, col[i].value * 2)
                    merged.add(mergedTile)
                    score += mergedTile.value

                    col[i].moveTo(3 - merged.size + 1, c)
                    col[i + 1].moveTo(3 - merged.size + 1, c)

                    i += 2
                } else {
                    val newTile = BoardItem(3 - merged.size, c, col[i].value)
                    merged.add(newTile)

                    if (col[i].row != 3 - merged.size + 1) {
                        col[i].moveTo(3 - merged.size + 1, c)
                    }

                    i++
                }
            }

            for (r in 0..3) {
                val newValue = if (3 - r < merged.size) merged[3 - r].value else 0
                if (board[r][c].value != newValue) {
                    madeChanges = true
                }
                board[r][c].value = newValue
            }
        }

        return madeChanges
    }

    private fun checkGameOver(): Boolean {
        if (getEmptySlots().isNotEmpty()) {
            return false
        }

        // Check horizontal merges
        for (r in 0..3) {
            for (c in 0..2) {
                if (board[r][c].value == board[r][c + 1].value) {
                    return false
                }
            }
        }

        // Check vertical merges
        for (r in 0..2) {
            for (c in 0..3) {
                if (board[r][c].value == board[r + 1][c].value) {
                    return false
                }
            }
        }

        return true
    }

    private fun getEmptySlots(): MutableList<Pair<Int, Int>> {
        val emptySlots: MutableList<Pair<Int, Int>> = mutableListOf()

        for (r in 0..3) {
            for (c in 0..3) {
                if (board[r][c].value == 0) {
                    emptySlots.add(Pair(r, c))
                }
            }
        }

        return emptySlots
    }

    private fun placeNewTile(): Boolean {
        val emptySlots = getEmptySlots()

        if (emptySlots.isEmpty()) {
            return false
        }

        val index = emptySlots.random()
        val type = if ((0..9).random() == 0) 4 else 2

        board[index.first][index.second].value = type
        board[index.first][index.second].moving = false
        board[index.first][index.second].newRow = index.first
        board[index.first][index.second].newCol = index.second

        newTiles.add(Pair(index.first, index.second))

        return true
    }
}