package com.ginkcoding.photocleaner.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ginkcoding.photocleaner.viewModel.PhotoViewModel

/**
 * 应用导航主机
 */
@Composable
fun PhotoCleanerNavHost(
    modifier: Modifier = Modifier
) {
    val viewModel: PhotoViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 根据状态显示不同屏幕
    when {
        uiState.isError -> {
            // 错误状态 - 显示权限请求
            PermissionScreen(
                onPermissionGranted = {
                    viewModel.clearError()
                    viewModel.loadPhotos()
                },
                modifier = modifier
            )
        }
        uiState.isComplete -> {
            // 完成状态 - 显示结果
            uiState.cleanupResult?.let { result ->
                ResultScreen(
                    result = result,
                    onRestart = { viewModel.restart() },
                    onExit = { viewModel.restart() },
                    modifier = modifier
                )
            }
        }
        uiState.isLoading -> {
            // 加载中
            LoadingScreen(
                modifier = modifier
            )
        }
        uiState.currentPhoto != null -> {
            // 正常浏览照片
            PhotoViewerScreen(
                uiState = uiState,
                onSwipeLeft = { viewModel.onSwipeLeft() },
                onSwipeRight = { viewModel.onSwipeRight() },
                onBack = { /* 返回逻辑 */ },
                onUndo = { viewModel.undoLastDelete() },
                modifier = modifier
            )
        }
        else -> {
            // 初始状态 - 加载照片
            LaunchedEffect(Unit) {
                viewModel.loadPhotos()
            }
            
            LoadingScreen(
                modifier = modifier
            )
        }
    }
}

@Composable
private fun LoadingScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        CircularProgressIndicator(
            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
        )
    }
}
