package com.ginkcoding.photocleaner.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDrag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ginkcoding.photocleaner.ui.components.GestureIndicator
import com.ginkcoding.photocleaner.ui.components.PhotoCard
import com.ginkcoding.photocleaner.ui.components.PhotoProgressBar
import com.ginkcoding.photocleaner.ui.components.ParticleEffect
import com.ginkcoding.photocleaner.utils.HapticFeedback
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
    var isDeleting by remember { mutableStateOf(false) }
    var showParticles by remember { mutableStateOf(false) }
    
    val haptic = remember { HapticFeedback(androidx.compose.ui.platform.LocalContext.current) }
    
    // 旋转动画（根据拖动偏移量）
    val rotation by animateFloatAsState(
        targetValue = (currentOffset / 10).coerceIn(-15f, 15f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "rotation"
    )
    
    // 缩放动画
    val scale by animateFloatAsState(
        targetValue = if (currentOffset != 0f) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0E1A),
                        Color(0xFF121826),
                        Color(0xFF151A28)
                    )
                )
            )
            .pointerInput(Unit) {
                detectHorizontalDrag(
                    onDragStart = { offset ->
                        dragStart = offset
                        currentOffset = 0f
                        haptic.tick()
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        currentOffset += dragAmount
                        change.consume()
                    },
                    onDragEnd = {
                        haptic.tick()
                        if (currentOffset > SWIPE_THRESHOLD) {
                            // 右滑删除
                            isDeleting = true
                            showParticles = true
                            haptic.delete()
                            onSwipeRight()
                            // 重置状态
                            kotlinx.coroutines.delay(600)
                            isDeleting = false
                            showParticles = false
                        } else if (currentOffset < -SWIPE_THRESHOLD) {
                            // 左滑跳过
                            haptic.success()
                            onSwipeLeft()
                        }
                        currentOffset = 0f
                    }
                )
            }
    ) {
        // 照片卡片
        AnimatedContent(
            targetState = uiState.currentPhoto,
            transitionSpec = {
                if (isDeleting) {
                    fadeOut(animationSpec = tween(300)) togetherWith
                    fadeIn(animationSpec = tween(300))
                } else {
                    slideInHorizontally(initialOffsetX = { it }) togetherWith
                    slideOutHorizontally(targetOffsetX = { -it })
                }
            },
            label = "photoTransition"
        ) { photo ->
            photo?.let {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = rotation
                            scaleX = scale
                            scaleY = scale
                        }
                        .offset {
                            androidx.compose.ui.unit.IntOffset(
                                x = currentOffset.toInt(),
                                y = 0
                            )
                        }
                ) {
                    PhotoCard(
                        photo = photo,
                        modifier = Modifier.fillMaxSize(),
                        showInfo = true
                    )
                    
                    // 删除指示器（右滑时显示）
                    AnimatedVisibility(
                        visible = currentOffset > 50,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(32.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFEF4444).copy(alpha = (currentOffset / SWIPE_THRESHOLD).coerceIn(0f, 1f)),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    
                    // 跳过指示器（左滑时显示）
                    AnimatedVisibility(
                        visible = currentOffset < -50,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(32.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF6B7280).copy(alpha = (-currentOffset / SWIPE_THRESHOLD).coerceIn(0f, 1f)),
                            modifier = Modifier.size(60.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 粒子特效（删除时显示）
        AnimatedVisibility(
            visible = showParticles,
            modifier = Modifier.align(Alignment.Center)
        ) {
            ParticleEffect(
                particleCount = 50,
                effectDuration = 600
            )
        }
        
        // 手势指示器
        GestureIndicator(
            gestureType = uiState.gestureType,
            modifier = Modifier.fillMaxSize()
        )
        
        // 顶部栏
        Surface(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            color = Color.Transparent
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "清理照片",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // 撤销按钮
                    AnimatedVisibility(visible = uiState.deletedCount > 0) {
                        IconButton(onClick = onUndo) {
                            Icon(
                                imageVector = Icons.Default.Undo,
                                contentDescription = "撤销",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
        
        // 底部进度条和控制
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 统计信息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatChip(
                    label = "已删除",
                    value = uiState.deletedCount.toString(),
                    color = Color(0xFFEF4444)
                )
                
                StatChip(
                    label = "已保留",
                    value = uiState.keptCount.toString(),
                    color = Color(0xFF10B981)
                )
                
                StatChip(
                    label = "剩余",
                    value = (uiState.photos.size - uiState.currentIndex - 1).toString(),
                    color = Color(0xFF0066FF)
                )
            }
            
            // 进度条
            PhotoProgressBar(
                current = uiState.currentIndex + 1,
                total = uiState.photos.size
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 手势提示
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                GestureChip(
                    icon = Icons.Default.Close,
                    label = "右滑删除",
                    color = Color(0xFFEF4444)
                )
                
                GestureChip(
                    icon = Icons.Default.Check,
                    label = "左滑保留",
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}

@Composable
private fun StatChip(
    label: String,
    value: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E2538).copy(alpha = 0.8f)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = label,
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun GestureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E2538).copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                color = Color(0xFFE8ECF1),
                fontSize = 14.sp
            )
        }
    }
}
