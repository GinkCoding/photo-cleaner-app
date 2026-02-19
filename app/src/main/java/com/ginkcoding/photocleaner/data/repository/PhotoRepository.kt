package com.ginkcoding.photocleaner.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import com.ginkcoding.photocleaner.data.model.Photo
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 照片仓库 - 负责从系统相册获取和管理照片
 */
@Singleton
class PhotoRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val contentResolver: ContentResolver = context.contentResolver

    /**
     * 随机获取指定数量的照片
     */
    fun getRandomPhotos(limit: Int = 10): Flow<List<Photo>> = flow {
        val allPhotos = loadAllPhotos()
        val shuffled = allPhotos.shuffled()
        emit(shuffled.take(limit))
    }.flowOn(Dispatchers.IO)

    /**
     * 加载所有照片
     */
    private fun loadAllPhotos(): List<Photo> {
        val photos = mutableListOf<Photo>()
        
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        )

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        try {
            contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val date = cursor.getLong(dateColumn)
                    val size = cursor.getLong(sizeColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)

                    val uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    photos.add(
                        Photo(
                            id = id,
                            uri = uri,
                            displayName = name,
                            dateTaken = date,
                            size = size,
                            width = width,
                            height = height
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            // 权限不足
            throw e
        } catch (e: Exception) {
            // 其他错误
            e.printStackTrace()
        }

        return photos
    }

    /**
     * 删除照片
     */
    suspend fun deletePhoto(photo: Photo): Boolean {
        return try {
            val rowsDeleted = contentResolver.delete(photo.uri, null, null)
            rowsDeleted > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * 批量删除照片
     */
    suspend fun deletePhotos(photos: List<Photo>): Int {
        var deletedCount = 0
        photos.forEach { photo ->
            if (deletePhoto(photo)) {
                deletedCount++
            }
        }
        return deletedCount
    }
}
