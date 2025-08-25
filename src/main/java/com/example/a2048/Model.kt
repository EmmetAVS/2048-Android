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

    var newRow = row;
    var newCol = col

    var moving = false

    fun moveTo(newRow: Int, newCol: Int) {

        this.newCol = newCol;
        this.newRow = newRow;
        this.moving = true

    }

    companion object {
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

        var madeChanges: Boolean

        if (direction == MoveDirection.Left || direction == MoveDirection.Right) {
            madeChanges = moveHorizontal(direction)
        } else {
            madeChanges = moveVertical(direction)
        }

        gameover = checkGameOver()
        animating = true
        Timer().schedule(Constants.Numerical.ANIMATION_TIME_DELAY) {
            animating = false
            handleMovesAndMerges()

            if (madeChanges) {

                placeNewTile()

            }

            version ++
        }

        Log.d("", "gameover: $gameover")
        version ++

    }

    private fun checkGameOver(): Boolean {
        if (getEmptySlots().isNotEmpty()) {
            return false
        }

        for (r in 0..3) {
            for (c in 0..2) {
                if (board[r][c].value == board[r][c + 1].value) {
                    return false
                }
            }
        }

        for (r in 0..2) {
            for (c in 0..3) {
                if (board[r][c].value == board[r + 1][c].value) {
                    return false
                }
            }
        }

        return true
    }

    private fun moveHorizontal(direction: MoveDirection): Boolean {
        val indexChange = if (direction == MoveDirection.Left) -1 else +1
        var everMadeChanges = false

        val newBoard = BoardItem.genBoard(4, 4)

        for (r in 0..3) {
            for (c in 0..3) {
                newBoard[r][c] = board[r][c]
            }
        }

        for (r in 0..3) {
            val iterator = if (indexChange > 0) (0..3).reversed() else (0..3)
            val merged = BooleanArray(4) { false }

            var madeSlideChanges: Boolean
            do {
                madeSlideChanges = false
                var lastSeenFreeCol = iterator.first
                var foundFreeCol = false

                for (index in iterator) {
                    if (newBoard[r][index].value == 0 && !foundFreeCol) {
                        foundFreeCol = true
                        lastSeenFreeCol = index
                    }

                    if (newBoard[r][index].value != 0 && foundFreeCol) {
                        newBoard[r][lastSeenFreeCol] = newBoard[r][index]
                        everMadeChanges = true
                        madeSlideChanges = true
                        foundFreeCol = false
                    }
                }
            } while (madeSlideChanges)

            for (index in iterator) {
                val adjacentIndex = index - indexChange
                if (index in 0..3 && adjacentIndex in 0..3 &&
                    newBoard[r][index].value == newBoard[r][adjacentIndex].value &&
                    newBoard[r][index].value != 0) {

                    if (merged[index] || merged[adjacentIndex])
                        continue

                    newBoard[r][index].moveTo(r, adjacentIndex)
                    newBoard[r][adjacentIndex].moveTo(r, adjacentIndex)

                    newBoard[r][index] = BoardItem(r, index, 0)

                    everMadeChanges = true
                    merged[adjacentIndex] = true
                    score += newBoard[r][adjacentIndex].value
                }
            }

            do {
                madeSlideChanges = false
                var lastSeenFreeCol = iterator.first
                var foundFreeCol = false

                for (index in iterator) {
                    if (newBoard[r][index].value == 0 && !foundFreeCol) {
                        foundFreeCol = true
                        lastSeenFreeCol = index
                    }

                    if (newBoard[r][index].value != 0 && foundFreeCol) {
                        newBoard[r][lastSeenFreeCol] = newBoard[r][index]
                        everMadeChanges = true
                        madeSlideChanges = true
                        foundFreeCol = false
                    }
                }
            } while (madeSlideChanges)
        }

        for (r in 0..3) {
            for (c in 0..3) {
                if (newBoard[r][c].value != 0 &&
                    (newBoard[r][c].row != newBoard[r][c].newRow ||
                            newBoard[r][c].col != newBoard[r][c].newCol)) {
                    newBoard[r][c].moveTo(r, c)
                }
            }
        }

        return everMadeChanges
    }

    private fun moveVertical(direction: MoveDirection): Boolean {
        val indexChange = if (direction == MoveDirection.Up) -1 else +1
        var everMadeChanges = false

        val newBoard = BoardItem.genBoard(4, 4)

        for (r in 0..3) {
            for (c in 0..3) {
                newBoard[r][c] = board[r][c]
            }
        }

        for (c in 0..3) {
            val iterator = if (indexChange > 0) (0..3).reversed() else (0..3)
            val merged = BooleanArray(4) { false }

            var madeSlideChanges: Boolean
            do {
                madeSlideChanges = false
                var lastSeenFreeRow = iterator.first
                var foundFreeRow = false

                for (index in iterator) {
                    if (newBoard[index][c].value == 0 && !foundFreeRow) {
                        foundFreeRow = true
                        lastSeenFreeRow = index
                    }

                    if (newBoard[index][c].value != 0 && foundFreeRow) {
                        newBoard[lastSeenFreeRow][c] = newBoard[index][c]
                        everMadeChanges = true
                        madeSlideChanges = true
                        foundFreeRow = false
                    }
                }
            } while (madeSlideChanges)

            for (index in iterator) {
                val adjacentIndex = index - indexChange
                if (index in 0..3 && adjacentIndex in 0..3 &&
                    newBoard[index][c].value == newBoard[adjacentIndex][c].value &&
                    newBoard[index][c].value != 0) {

                    if (merged[index] || merged[adjacentIndex])
                        continue

                    newBoard[index][c].moveTo(adjacentIndex, c)
                    newBoard[adjacentIndex][c].moveTo(adjacentIndex, c)

                    newBoard[index][c] = BoardItem(index, c, 0)

                    everMadeChanges = true
                    merged[adjacentIndex] = true
                    score += newBoard[adjacentIndex][c].value
                }
            }

            do {
                madeSlideChanges = false
                var lastSeenFreeRow = iterator.first
                var foundFreeRow = false

                for (index in iterator) {
                    if (newBoard[index][c].value == 0 && !foundFreeRow) {
                        foundFreeRow = true
                        lastSeenFreeRow = index
                    }

                    if (newBoard[index][c].value != 0 && foundFreeRow) {
                        newBoard[lastSeenFreeRow][c] = newBoard[index][c]
                        everMadeChanges = true
                        madeSlideChanges = true
                        foundFreeRow = false
                    }
                }
            } while (madeSlideChanges)
        }

        for (r in 0..3) {
            for (c in 0..3) {
                if (newBoard[r][c].value != 0 &&
                    (newBoard[r][c].row != newBoard[r][c].newRow ||
                            newBoard[r][c].col != newBoard[r][c].newCol)) {
                    newBoard[r][c].moveTo(r, c)
                }
            }
        }

        return everMadeChanges
    }

    private fun getEmptySlots(): MutableList<Pair<Int, Int>>{
        val emptySlots: MutableList<Pair<Int, Int>> = mutableListOf();

        for (r in 0..3) {
            for (c in 0..3) {
                if (board[r][c].value == 0) {
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

        board[index.first][index.second].value = type
        board[index.first][index.second].moving = false
        board[index.first][index.second].newRow = index.first
        board[index.first][index.second].newCol = index.second

        newTiles.add(Pair(index.first, index.second))

        return true;

    }

    private fun handleMovesAndMerges() {

        val newBoard = BoardItem.genBoard(4, 4)

        for (r in 0..3) {
            for (c in 0..3) {

                if (board[r][c].value == 0) continue

                val targetRow = board[r][c].newRow
                val targetCol = board[r][c].newCol

                if (newBoard[targetRow][targetCol].value != 0) {
                    newBoard[targetRow][targetCol].value *= 2
                    continue
                }

                newBoard[targetRow][targetCol].value = board[r][c].value
                newBoard[targetRow][targetCol].moving = false
                newBoard[targetRow][targetCol].newRow = targetRow
                newBoard[targetRow][targetCol].newCol = targetCol
                newBoard[targetRow][targetCol].row = targetRow
                newBoard[targetRow][targetCol].col = targetCol

            }
        }

        board = newBoard

    }

}