package com.ginkcoding.photocleaner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ginkcoding.photocleaner.data.model.Photo
import com.ginkcoding.photocleaner.utils.HapticFeedback
import java.text.SimpleDateFormat
import java.util.*

/**
 * 照片卡片组件（优化版）
 * 支持滑动删除、点击选择、动画反馈
 */
@Composable
fun PhotoCard(
    photo: Photo,
    modifier: Modifier = Modifier,
    showInfo: Boolean = true,
    isSelected: Boolean = false,
    onToggleSelect: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var isDeleting by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val haptic = remember { HapticFeedback(LocalContext.current) }
    
    // 缩放动画
    val scale = animateFloatAsState(
        targetValue = if (isSelected) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    // 删除动画
    val deleteTransition = updateTransition(targetState = isDeleting, label = "delete")
    val deleteScale by deleteTransition.animateFloat(
        transitionSpec = { tween(300) },
        label = "scale"
    ) { if (it) 0f else 1f }
    
    val deleteAlpha by deleteTransition.animateFloat(
        transitionSpec = { tween(300) },
        label = "alpha"
    ) { if (it) 0f else 1f }
    
    Box(
        modifier = modifier
            .scale(if (isDeleting) deleteScale else scale.value)
            .alpha(deleteAlpha)
            .padding(8.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { 
                        haptic.tick()
                        onToggleSelect()
                    },
                    onLongPress = {
                        haptic.longPress()
                        showDeleteConfirm = true
                    }
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) 
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else 
                    MaterialTheme.colorScheme.surfaceVariant
            )
    ) {
        // 照片
        AsyncImage(
            model = photo.uri,
            contentDescription = photo.displayName,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // 选择状态覆盖层
        AnimatedVisibility(
            visible = isSelected,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x400066FF))
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp),
                    tint = Color.White
                )
            }
        }
        
        // 照片信息
        if (showInfo) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        RoundedCornerShape(topEnd = 16.dp)
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = photo.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatFileSize(photo.size),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = formatDate(photo.dateTaken),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // 删除确认对话框
        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("删除照片") },
                text = { Text("确定要删除这张照片吗？此操作不可恢复。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            haptic.delete()
                            showDeleteConfirm = false
                            isDeleting = true
                            // 等待删除动画完成后回调
                            androidx.compose.runtime.LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(300)
                                onDelete()
                            }
                        }
                    ) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("取消")
                    }
                },
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
    }
}

private fun formatDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}
