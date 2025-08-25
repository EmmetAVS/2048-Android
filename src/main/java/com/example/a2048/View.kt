package com.example.a2048

import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import kotlin.math.abs

@Composable
fun GameView(model: Model) {

    val board = model.board
    val score = model.score
    val gameover = model.gameover
    var totalX = 0f
    var totalY = 0f
    val version = model.version

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
                    var value by remember(board[r][c].id) {mutableIntStateOf(board[r][c].value)}

                    //Animation for new tiles
                    val isNew = model.newTiles.contains(Pair(r, c))

                    var animationPhase by remember(isNew) { mutableIntStateOf(if (isNew) 0 else 2) }
                    val targetScale = when (animationPhase) {
                        0 -> 0.5f
                        1 -> 1.1f
                        else -> 1f
                    }

                    val scale by animateFloatAsState(
                        targetValue = targetScale,
                        animationSpec = when (animationPhase) {
                            0 -> tween(durationMillis = 0)
                            1 -> tween(durationMillis = Constants.Numerical.ANIMATION_TIME_DELAY.toInt(), easing = FastOutSlowInEasing)
                            else -> tween(durationMillis = 0, easing = LinearOutSlowInEasing)
                        },
                        finishedListener = {
                            when (animationPhase) {
                                0 -> animationPhase = 1
                                1 -> animationPhase = 2
                                2 -> if (isNew) model.newTiles.remove(Pair(r, c))
                            }
                        }
                    )

                    //Animation for moving
                    val isMoving = board[r][c].moving
                    val offsetX = if (isMoving) (board[r][c].newCol - board[r][c].col).toFloat() else 0f
                    val offsetY = if (isMoving) (board[r][c].newRow - board[r][c].row).toFloat() else 0f

                    if (!isMoving && !model.animating) {
                        value = board[r][c].value
                    }
                    val text: String = if (value == 0) "" else "${value}"
                    val color = if (value == 0) Constants.Theme.BOX_COLOR else model.getColor(value)

                    val offset by animateOffsetAsState(
                        targetValue = if (isMoving) Offset(offsetX, offsetY) else Offset(0f, 0f),
                        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                    )

                    val offsetXVal = if (isMoving) offset.x * Constants.Style.BOX_SIZE else 0.dp
                    val offsetYVal = if (isMoving) offset.y * Constants.Style.BOX_SIZE else 0.dp

                    Box(
                        modifier = Modifier
                            .size(Constants.Style.BOX_SIZE)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .background(Constants.Theme.BOX_COLOR)
                            .border(Constants.Style.BORDER_WIDTH, Constants.Theme.BORDER_COLOR),
                        contentAlignment = Alignment.Center
                    ) {

                        Box(

                            modifier = Modifier
                                .size(Constants.Style.BOX_SIZE)
                                .offset(offsetXVal, offsetYVal)
                                .background(color)
                                .graphicsLayer {
                                    shadowElevation = if (isMoving) 10f else 0f
                                },
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

}