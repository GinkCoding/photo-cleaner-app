package com.ginkcoding.photocleaner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ginkcoding.photocleaner.data.model.GestureType

/**
 * 手势指示器
 */
@Composable
fun GestureIndicator(
    gestureType: GestureType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        // 左滑指示 (跳过)
        AnimatedVisibility(
            visible = gestureType == GestureType.SWIPE_LEFT,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            SkipIndicator()
        }

        // 右滑指示 (删除)
        AnimatedVisibility(
            visible = gestureType == GestureType.SWIPE_RIGHT,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            DeleteIndicator()
        }
    }
}

@Composable
private fun SkipIndicator() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                Color.Gray.copy(alpha = 0.8f),
                CircleShape
            )
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Skip",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "跳过",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
private fun DeleteIndicator() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                Color.Red.copy(alpha = 0.8f),
                CircleShape
            )
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Delete",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "删除",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
