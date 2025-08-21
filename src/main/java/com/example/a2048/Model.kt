package com.example.a2048

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlin.math.log2
import kotlin.math.pow

class Model {

    public enum class MoveDirection {

        Left,
        Right,
        Up,
        Down

    }

    var board: Array<Array<Int>> by mutableStateOf(
        arrayOf(
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 0, 0, 0),
            arrayOf(0, 0, 0, 0),
        )
    )

    var score: Int = 0
    var gameover: Boolean = false

    public var newTiles: MutableList<Pair<Int, Int>> = mutableListOf()

    init {
        placeNewTile()
        placeNewTile()
        newTiles = mutableListOf()
    }

    public fun getColor(value: Int): Color {

        val hue = (30 * log2(value.toDouble())) % 360
        return Color.hsv(hue.toFloat(), 0.4f, 0.9f)

    }

    public fun handleMove(direction: MoveDirection) {

        if (gameover)
            return

        var madeChanges: Boolean

        if (direction == MoveDirection.Left || direction == MoveDirection.Right) {
            madeChanges = moveHorizontal(direction)
        } else {
            madeChanges = moveVertical(direction)
        }

        if (madeChanges) {

            placeNewTile()

        }

        gameover = checkGameOver()

        Log.d("", "gameover: $gameover")
        val newBoard = board.map { it.copyOf() }.toTypedArray()
        board = newBoard;

    }

    private fun checkGameOver(): Boolean {

        var oldBoard = Array<Array<Int>>(board.size) { i ->
            Array<Int>(board[i].size) { j ->
                board[i][j]
            }
        }
        val oldScore = score

        if (moveHorizontal(MoveDirection.Left)) {
            board = oldBoard
            score = oldScore
            return false
        }

        oldBoard = Array<Array<Int>>(board.size) { i ->
            Array<Int>(board[i].size) { j ->
                board[i][j]
            }
        }

        if (moveHorizontal(MoveDirection.Right)) {
            board = oldBoard
            score = oldScore
            return false
        }

        oldBoard = Array<Array<Int>>(board.size) { i ->
            Array<Int>(board[i].size) { j ->
                board[i][j]
            }
        }

        if (moveVertical(MoveDirection.Up)) {
            board = oldBoard
            score = oldScore
            return false
        }

        oldBoard = Array<Array<Int>>(board.size) { i ->
            Array<Int>(board[i].size) { j ->
                board[i][j]
            }
        }

        if (moveVertical(MoveDirection.Down)) {
            board = oldBoard
            score = oldScore
            return false
        }

        score = oldScore
        return true

    }

    private fun moveHorizontal(direction: MoveDirection): Boolean {
        val indexChange = if (direction == MoveDirection.Left) -1 else +1
        var everMadeChanges = false

        for (r in 0..3) {
            val iterator = if (indexChange > 0) (0..3).reversed() else (0..3)
            val merged = BooleanArray(4) { false }

            var madeSlideChanges: Boolean
            do {
                madeSlideChanges = false
                var lastSeenFreeCol = iterator.first
                var foundFreeCol = false

                for (index in iterator) {
                    if (board[r][index] == 0 && !foundFreeCol) {
                        foundFreeCol = true
                        lastSeenFreeCol = index
                    }

                    if (board[r][index] != 0 && foundFreeCol) {
                        board[r][lastSeenFreeCol] = board[r][index]
                        board[r][index] = 0
                        everMadeChanges = true
                        madeSlideChanges = true
                        foundFreeCol = false
                    }
                }
            } while (madeSlideChanges)

            for (index in iterator) {
                val adjacentIndex = index - indexChange
                if (index in 0..3 && adjacentIndex in 0..3 &&
                    board[r][index] == board[r][adjacentIndex] &&
                    board[r][index] != 0) {

                    if (merged[index] || merged[adjacentIndex])
                        continue

                    board[r][adjacentIndex] = board[r][index] * 2
                    board[r][index] = 0
                    everMadeChanges = true
                    merged[adjacentIndex] = true
                    score += board[r][adjacentIndex]
                }
            }

            do {
                madeSlideChanges = false
                var lastSeenFreeCol = iterator.first
                var foundFreeCol = false

                for (index in iterator) {
                    if (board[r][index] == 0 && !foundFreeCol) {
                        foundFreeCol = true
                        lastSeenFreeCol = index
                    }

                    if (board[r][index] != 0 && foundFreeCol) {
                        board[r][lastSeenFreeCol] = board[r][index]
                        board[r][index] = 0
                        everMadeChanges = true
                        madeSlideChanges = true
                        foundFreeCol = false
                    }
                }
            } while (madeSlideChanges)
        }

        return everMadeChanges
    }

    private fun moveVertical(direction: MoveDirection): Boolean {
        val indexChange = if (direction == MoveDirection.Up) -1 else +1
        var everMadeChanges = false

        for (c in 0..3) {
            val iterator = if (indexChange > 0) (0..3).reversed() else (0..3)
            val merged = BooleanArray(4) { false }

            var madeSlideChanges: Boolean
            do {
                madeSlideChanges = false
                var lastSeenFreeCol = iterator.first
                var foundFreeCol = false

                for (index in iterator) {
                    if (board[index][c] == 0 && !foundFreeCol) {
                        foundFreeCol = true
                        lastSeenFreeCol = index
                    }

                    if (board[index][c] != 0 && foundFreeCol) {
                        board[lastSeenFreeCol][c] = board[index][c]
                        board[index][c] = 0
                        everMadeChanges = true
                        madeSlideChanges = true
                        foundFreeCol = false
                    }
                }
            } while (madeSlideChanges)

            for (index in iterator) {
                val adjacentIndex = index - indexChange
                if (index in 0..3 && adjacentIndex in 0..3 &&
                    board[index][c] == board[adjacentIndex][c] &&
                    board[index][c] != 0) {

                    if (merged[index] || merged[adjacentIndex])
                        continue

                    board[adjacentIndex][c] = board[index][c] * 2
                    board[index][c] = 0
                    everMadeChanges = true
                    merged[adjacentIndex] = true
                    score += board[adjacentIndex][c]
                }
            }

            do {
                madeSlideChanges = false
                var lastSeenFreeCol = iterator.first
                var foundFreeCol = false

                for (index in iterator) {
                    if (board[index][c] == 0 && !foundFreeCol) {
                        foundFreeCol = true
                        lastSeenFreeCol = index
                    }

                    if (board[index][c] != 0 && foundFreeCol) {
                        board[lastSeenFreeCol][c] = board[index][c]
                        board[index][c] = 0
                        everMadeChanges = true
                        madeSlideChanges = true
                        foundFreeCol = false
                    }
                }
            } while (madeSlideChanges)
        }

        return everMadeChanges
    }

    private fun getEmptySlots(): MutableList<Pair<Int, Int>>{
        val emptySlots: MutableList<Pair<Int, Int>> = mutableListOf();

        for (r in 0..3) {
            for (c in 0..3) {
                if (board[r][c] == 0) {
                    emptySlots.add(Pair<Int, Int>(r, c))
                }
            }
        }

        return emptySlots
    }

    private fun placeNewTile(): Boolean {

        val emptySlots = getEmptySlots()

        if (emptySlots.isEmpty()) {
            return false;
        }

        val index = emptySlots.random()
        val type = if ((0..9).random() == 0) 4 else 2

        board[index.first][index.second] = type

        newTiles.add(Pair(index.first, index.second))

        return true;

    }

}