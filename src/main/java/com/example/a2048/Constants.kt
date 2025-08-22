package com.example.a2048

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class Constants {

    object Theme {
        val BACKGROUND = Color(251,248,239);
        val TEXT_COLOR = Color.Gray;
        val BORDER_COLOR = Color(190,178,166)
        val BOX_COLOR = Color(205,193,179)
    }

    object Style {

        val FONT_SIZE = 48.sp;
        val BORDER_WIDTH = 6.dp;
        val BOX_SIZE = 80.dp;

    }

    object Numerical {

        val ANIMATION_TIME_DELAY = 500L

    }

}