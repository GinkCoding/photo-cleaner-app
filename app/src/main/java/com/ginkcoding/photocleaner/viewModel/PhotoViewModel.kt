package com.ginkcoding.photocleaner.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ginkcoding.photocleaner.data.model.CleanupResult
import com.ginkcoding.photocleaner.data.model.GestureType
import com.ginkcoding.photocleaner.data.model.Photo
import com.ginkcoding.photocleaner.data.repository.PhotoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI 状态
 */
data class PhotoUiState(
    val photos: List<Photo> = emptyList(),
    val currentIndex: Int = 0,
    val currentPhoto: Photo? = null,
    val gestureType: GestureType = GestureType.NONE,
    val isLoading: Boolean = false,
    val isError: Boolean = false,
    val errorMessage: String = "",
    val isComplete: Boolean = false,
    val cleanupResult: CleanupResult? = null,
    val deletedCount: Int = 0,
    val keptCount: Int = 0
)

/**
 * 照片 ViewModel
 */
@HiltViewModel
class PhotoViewModel @Inject constructor(
    private val photoRepository: PhotoRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PhotoUiState())
    val uiState: StateFlow<PhotoUiState> = _uiState.asStateFlow()

    private val _deletedPhotos = MutableStateFlow(mutableListOf<Photo>())
    val deletedPhotos: StateFlow<List<Photo>> = _deletedPhotos.asStateFlow()

    /**
     * 加载随机照片
     */
    fun loadPhotos(limit: Int = 10) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, isError = false)
            
            try {
                photoRepository.getRandomPhotos(limit).collect { photos ->
                    if (photos.isEmpty()) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isError = true,
                            errorMessage = "没有找到照片"
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            photos = photos,
                            currentIndex = 0,
                            currentPhoto = photos.firstOrNull(),
                            isLoading = false,
                            isComplete = false,
                            deletedCount = 0,
                            keptCount = 0
                        )
                        _deletedPhotos.value = mutableListOf()
                    }
                }
            } catch (e: SecurityException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = "需要相册权限才能继续"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isError = true,
                    errorMessage = e.message ?: "加载失败"
                )
            }
        }
    }

    /**
     * 处理手势 - 跳过照片
     */
    fun onSwipeLeft() {
        val currentState = _uiState.value
        val currentPhoto = currentState.currentPhoto ?: return
        
        // 标记为跳过
        currentPhoto.isSkipped = true
        
        val newIndex = currentState.currentIndex + 1
        val keptCount = currentState.keptCount + 1
        
        if (newIndex >= currentState.photos.size) {
            // 完成
            _uiState.value = currentState.copy(
                isComplete = true,
                keptCount = keptCount,
                cleanupResult = CleanupResult(
                    totalProcessed = currentState.photos.size,
                    deletedCount = currentState.deletedCount,
                    keptCount = keptCount
                )
            )
        } else {
            _uiState.value = currentState.copy(
                currentIndex = newIndex,
                currentPhoto = currentState.photos[newIndex],
                keptCount = keptCount,
                gestureType = GestureType.SWIPE_LEFT
            )
            
            // 重置手势
            resetGesture()
        }
    }

    /**
     * 处理手势 - 删除照片
     */
    fun onSwipeRight() {
        val currentState = _uiState.value
        val currentPhoto = currentState.currentPhoto ?: return
        
        viewModelScope.launch {
            // 标记为删除
            currentPhoto.isDeleted = true
            _deletedPhotos.value.add(currentPhoto)
            
            // 从系统删除
            photoRepository.deletePhoto(currentPhoto)
            
            val newIndex = currentState.currentIndex + 1
            val deletedCount = currentState.deletedCount + 1
            
            if (newIndex >= currentState.photos.size) {
                // 完成
                _uiState.value = currentState.copy(
                    isComplete = true,
                    deletedCount = deletedCount,
                    cleanupResult = CleanupResult(
                        totalProcessed = currentState.photos.size,
                        deletedCount = deletedCount,
                        keptCount = currentState.keptCount
                    )
                )
            } else {
                _uiState.value = currentState.copy(
                    currentIndex = newIndex,
                    currentPhoto = currentState.photos[newIndex],
                    deletedCount = deletedCount,
                    gestureType = GestureType.SWIPE_RIGHT
                )
                
                // 重置手势
                resetGesture()
            }
        }
    }

    /**
     * 撤销最后一次删除
     */
    fun undoLastDelete() {
        val deletedList = _deletedPhotos.value.toMutableList()
        if (deletedList.isNotEmpty()) {
            val lastDeleted = deletedList.removeAt(deletedList.size - 1)
            _deletedPhotos.value = deletedList
            
            // 注意：这里无法恢复已删除的照片，只能更新 UI 计数
            val currentState = _uiState.value
            _uiState.value = currentState.copy(
                deletedCount = currentState.deletedCount - 1
            )
        }
    }

    /**
     * 重置手势
     */
    private fun resetGesture() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(300)
            _uiState.value = _uiState.value.copy(
                gestureType = GestureType.NONE
            )
        }
    }

    /**
     * 重新开始
     */
    fun restart() {
        loadPhotos()
    }

    /**
     * 清除错误
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(
            isError = false,
            errorMessage = ""
        )
    }
}
