package com.ginkcoding.photocleaner.ui.screens

import androidx.compose.foundation.gestures.detectHorizontalDrag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.ginkcoding.photocleaner.data.model.Photo
import com.ginkcoding.photocleaner.ui.components.GestureIndicator
import com.ginkcoding.photocleaner.ui.components.PhotoCard
import com.ginkcoding.photocleaner.ui.components.PhotoProgressBar
import com.ginkcoding.photocleaner.viewModel.PhotoUiState

private const val SWIPE_THRESHOLD = 100f

@Composable
fun PhotoViewerScreen(
    uiState: PhotoUiState,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onBack: () -> Unit,
    onUndo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var dragStart by remember { mutableStateOf(0f) }
    var currentOffset by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDrag(
                    onDragStart = { offset ->
                        dragStart = offset
                        currentOffset = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        currentOffset += dragAmount
                        change.consume()
                    },
                    onDragEnd = {
                        if (currentOffset > SWIPE_THRESHOLD) {
                            onSwipeRight()
                        } else if (currentOffset < -SWIPE_THRESHOLD) {
                            onSwipeLeft()
                        }
                        currentOffset = 0f
                    }
                )
            }
    ) {
        // 照片卡片
        uiState.currentPhoto?.let { photo ->
            PhotoCard(
                photo = photo,
                modifier = Modifier
                    .fillMaxSize()
                    .offset {
                        androidx.compose.ui.unit.IntOffset(
                            x = currentOffset.toInt(),
                            y = 0
                        )
                    }
            )
        }

        // 手势指示器
        GestureIndicator(
            gestureType = uiState.gestureType,
            modifier = Modifier.fillMaxSize()
        )

        // 顶部栏
        TopAppBar(
            title = {
                Text(
                    text = "清理照片",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            },
            actions = {
                // 撤销按钮
                if (uiState.deletedCount > 0) {
                    IconButton(onClick = onUndo) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "撤销",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.align(Alignment.TopCenter)
        )

        // 底部进度条
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            PhotoProgressBar(
                current = uiState.currentIndex + 1,
                total = uiState.photos.size
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AssistChip(
                    onClick = { },
                    label = { Text("左滑跳过") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
                
                AssistChip(
                    onClick = { },
                    label = { Text("右滑删除") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                )
            }
        }
    }
}
