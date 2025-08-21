package com.example.a2048

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlin.math.abs
import com.airbnb.lottie.compose.*

@Composable
fun GameView(model: Model) {

    val board = model.board
    val score = model.score
    val gameover = model.gameover
    var totalX = 0f
    var totalY = 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Constants.Theme.BACKGROUND)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        totalX += dragAmount.x
                        totalY += dragAmount.y
                    },
                    onDragEnd = {
                        if (abs(totalX) > abs(totalY)) {
                            if (totalX > 0) {
                                model.handleMove(Model.MoveDirection.Right)
                            } else {
                                model.handleMove(Model.MoveDirection.Left)
                            }
                        } else {
                            if (totalY > 0) {
                                Log.d("", "Swiped Down")
                                model.handleMove(Model.MoveDirection.Down)
                            } else {
                                Log.d("", "Swiped Up")
                                model.handleMove(Model.MoveDirection.Up)
                            }
                        }

                        totalX = 0f
                        totalY = 0f
                    },
                    onDragCancel = {
                        totalX = 0f
                        totalY = 0f
                    }
                )
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Row(
            modifier = Modifier
                .width(3.8 * Constants.Style.BOX_SIZE)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "2048",
                color = Constants.Theme.TEXT_COLOR,
                fontSize = Constants.Style.FONT_SIZE,
                textAlign = TextAlign.Center
            )

            Column {
                Text(
                    text = "Score: $score",
                    color = Constants.Theme.TEXT_COLOR,
                    fontSize = Constants.Style.FONT_SIZE / 2,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (gameover) "Gameover!" else "",
                    color = Constants.Theme.TEXT_COLOR,
                    fontSize = Constants.Style.FONT_SIZE / 2,
                    textAlign = TextAlign.Center
                )
            }
        }

        for (r in 0..3) {

            Row {

                for (c in 0..3) {

                    val text: String = if (board[r][c] == 0) "" else "${board[r][c]}"
                    val Color = if (board[r][c] == 0) Constants.Theme.BOX_COLOR else model.getColor(board[r][c])

                    Box(
                        modifier = Modifier
                            .size(Constants.Style.BOX_SIZE)
                            .background(Color)
                            .border(Constants.Style.BORDER_WIDTH, Constants.Theme.BORDER_COLOR),
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = text,
                            color = Constants.Theme.TEXT_COLOR,
                            fontSize = Constants.Style.FONT_SIZE / 2,
                            textAlign = TextAlign.Center
                        )

                    }
                }

            }

        }

    }

}