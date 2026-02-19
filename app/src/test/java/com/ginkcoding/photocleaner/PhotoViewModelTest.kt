package com.ginkcoding.photocleaner

import com.ginkcoding.photocleaner.data.model.CleanupResult
import com.ginkcoding.photocleaner.data.model.GestureType
import com.ginkcoding.photocleaner.data.model.Photo
import com.ginkcoding.photocleaner.data.repository.PhotoRepository
import com.ginkcoding.photocleaner.viewModel.PhotoUiState
import com.ginkcoding.photocleaner.viewModel.PhotoViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.net.URI

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoViewModelTest {

    @Mock
    private lateinit var photoRepository: PhotoRepository

    private lateinit var viewModel: PhotoViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `test initial state`() = runTest {
        viewModel = PhotoViewModel(photoRepository)
        
        val state = viewModel.uiState.value
        
        assertTrue(state.photos.isEmpty())
        assertEquals(0, state.currentIndex)
        assertNull(state.currentPhoto)
        assertFalse(state.isLoading)
        assertFalse(state.isComplete)
    }

    @Test
    fun `test load photos success`() = runTest {
        val mockPhotos = createMockPhotos(10)
        `when`(photoRepository.getRandomPhotos(10)).thenReturn(flowOf(mockPhotos))
        
        viewModel = PhotoViewModel(photoRepository)
        viewModel.loadPhotos(10)
        
        val state = viewModel.uiState.value
        
        assertEquals(10, state.photos.size)
        assertEquals(0, state.currentIndex)
        assertNotNull(state.currentPhoto)
        assertFalse(state.isLoading)
        assertFalse(state.isError)
    }

    @Test
    fun `test load photos empty`() = runTest {
        `when`(photoRepository.getRandomPhotos(10)).thenReturn(flowOf(emptyList()))
        
        viewModel = PhotoViewModel(photoRepository)
        viewModel.loadPhotos(10)
        
        val state = viewModel.uiState.value
        
        assertTrue(state.photos.isEmpty())
        assertTrue(state.isError)
        assertEquals("没有找到照片", state.errorMessage)
    }

    @Test
    fun `test swipe left skips photo`() = runTest {
        val mockPhotos = createMockPhotos(10)
        `when`(photoRepository.getRandomPhotos(10)).thenReturn(flowOf(mockPhotos))
        
        viewModel = PhotoViewModel(photoRepository)
        viewModel.loadPhotos(10)
        viewModel.onSwipeLeft()
        
        val state = viewModel.uiState.value
        
        assertEquals(1, state.currentIndex)
        assertEquals(1, state.keptCount)
        assertEquals(0, state.deletedCount)
        assertTrue(mockPhotos[0].isSkipped)
    }

    @Test
    fun `test swipe right deletes photo`() = runTest {
        val mockPhotos = createMockPhotos(10)
        `when`(photoRepository.getRandomPhotos(10)).thenReturn(flowOf(mockPhotos))
        `when`(photoRepository.deletePhoto(mockPhotos[0])).thenReturn(true)
        
        viewModel = PhotoViewModel(photoRepository)
        viewModel.loadPhotos(10)
        viewModel.onSwipeRight()
        
        val state = viewModel.uiState.value
        
        assertEquals(1, state.currentIndex)
        assertEquals(0, state.keptCount)
        assertEquals(1, state.deletedCount)
        assertTrue(mockPhotos[0].isDeleted)
    }

    @Test
    fun `test complete after processing all photos`() = runTest {
        val mockPhotos = createMockPhotos(1)
        `when`(photoRepository.getRandomPhotos(1)).thenReturn(flowOf(mockPhotos))
        `when`(photoRepository.deletePhoto(mockPhotos[0])).thenReturn(true)
        
        viewModel = PhotoViewModel(photoRepository)
        viewModel.loadPhotos(1)
        viewModel.onSwipeRight()
        
        val state = viewModel.uiState.value
        
        assertTrue(state.isComplete)
        assertNotNull(state.cleanupResult)
        assertEquals(1, state.cleanupResult?.totalProcessed)
        assertEquals(1, state.cleanupResult?.deletedCount)
    }

    @Test
    fun `test undo last delete`() = runTest {
        val mockPhotos = createMockPhotos(10)
        `when`(photoRepository.getRandomPhotos(10)).thenReturn(flowOf(mockPhotos))
        `when`(photoRepository.deletePhoto(mockPhotos[0])).thenReturn(true)
        
        viewModel = PhotoViewModel(photoRepository)
        viewModel.loadPhotos(10)
        viewModel.onSwipeRight()
        
        var state = viewModel.uiState.value
        assertEquals(1, state.deletedCount)
        
        viewModel.undoLastDelete()
        
        state = viewModel.uiState.value
        assertEquals(0, state.deletedCount)
    }

    @Test
    fun `test restart loads new photos`() = runTest {
        val mockPhotos1 = createMockPhotos(10)
        val mockPhotos2 = createMockPhotos(10)
        `when`(photoRepository.getRandomPhotos(10))
            .thenReturn(flowOf(mockPhotos1))
            .thenReturn(flowOf(mockPhotos2))
        
        viewModel = PhotoViewModel(photoRepository)
        viewModel.loadPhotos(10)
        viewModel.onSwipeLeft()
        
        var state = viewModel.uiState.value
        assertEquals(1, state.currentIndex)
        
        viewModel.restart()
        
        state = viewModel.uiState.value
        assertEquals(0, state.currentIndex)
        assertFalse(state.isComplete)
    }

    private fun createMockPhotos(count: Int): List<Photo> {
        return List(count) { index ->
            Photo(
                id = index.toLong(),
                uri = android.net.Uri.parse("content://media/external/images/media/$index"),
                displayName = "photo_$index.jpg",
                dateTaken = System.currentTimeMillis(),
                size = 1024 * (index + 1),
                width = 1920,
                height = 1080
            )
        }
    }
}
