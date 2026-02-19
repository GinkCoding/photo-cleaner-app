package com.ginkcoding.photocleaner.data.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * 照片数据模型
 */
data class Photo(
    val id: Long,
    val uri: Uri,
    val displayName: String,
    val dateTaken: Long,
    val size: Long,
    val width: Int,
    val height: Int,
    var isDeleted: Boolean = false,
    var isSkipped: Boolean = false
) {
    /**
     * 格式化文件大小
     */
    fun getFormattedSize(): String {
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> String.format("%.1f MB", size / (1024.0 * 1024.0))
        }
    }
}

/**
 * 照片清理结果
 */
data class CleanupResult(
    val totalProcessed: Int,
    val deletedCount: Int,
    val keptCount: Int
)

/**
 * 手势类型
 */
enum class GestureType {
    NONE,
    SWIPE_LEFT,    // 跳过
    SWIPE_RIGHT,   // 删除
    SWIPE_UP,
    SWIPE_DOWN
}
