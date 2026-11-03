package com.example.coursebadge.viewmodel

import com.example.coursebadge.data.CourseRepository
import com.example.coursebadge.network.CourseApiService
import com.example.coursebadge.network.CourseDto
import com.example.coursebadge.network.InstructorDto
import kotlin.coroutines.ContinuationInterceptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@OptIn(ExperimentalCoroutinesApi::class)
class CourseViewModelTest {

    // One scheduler and two dispatchers, "main" is used instead of
    // Dispatchers.Main and "io" instead of Dispatchers.IO, that way the test can
    // check which one the network call actually ran on.
    private val scheduler = TestCoroutineScheduler()
    private val mainDispatcher = StandardTestDispatcher(scheduler, name = "main")
    private val ioDispatcher = StandardTestDispatcher(scheduler, name = "io")

    private lateinit var api: FakeCourseApi

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        api = FakeCourseApi()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel() = CourseViewModel(
        repository = CourseRepository(api = api, simulatedLatencyMs = 0),
        ioDispatcher = ioDispatcher
    )

    @Test
    fun `a centered spinner shows whenever isLoading is true`() = runTest(scheduler) {
        api.responder = {
            delay(1_000)
            listOf(courseDto("et101"))
        }
        val vm = createViewModel()

        // First load, nothing is on the screen yet so it shows the full screen spinner
        runCurrent()
        assertTrue(vm.uiState.value.showFullScreenLoading)
        assertFalse(vm.uiState.value.showLoadingOverlay)
        advanceUntilIdle()
        assertFalse(vm.uiState.value.showFullScreenLoading)
        assertFalse(vm.uiState.value.showLoadingOverlay)

        // Refresh, the courses stay on the screen and the spinner goes on top of them
        vm.loadCourses()
        runCurrent()
        val refreshing = vm.uiState.value
        assertTrue(refreshing.isLoading)
        assertTrue(refreshing.showLoadingOverlay)
        assertFalse(refreshing.showFullScreenLoading)
        assertEquals(1, refreshing.courses.size)

        advanceUntilIdle()
        assertFalse(vm.uiState.value.showLoadingOverlay)
    }

    @Test
    fun `shows loading while the request runs then the courses`() = runTest(scheduler) {
        api.responder = {
            delay(1_000)
            listOf(courseDto("et101"), courseDto("et483"))
        }
        val vm = createViewModel()

        runCurrent()
        assertTrue(vm.uiState.value.isLoading)
        assertTrue(vm.uiState.value.showFullScreenLoading)

        advanceUntilIdle()
        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(listOf("et101", "et483"), state.courses.map { it.id })
    }

    @Test
    fun `network call runs on the IO dispatcher, not main`() = runTest(scheduler) {
        createViewModel()
        advanceUntilIdle()

        assertSame(ioDispatcher, api.lastDispatcher)
    }

    @Test
    fun `offline error shows a friendly message and stops loading`() = runTest(scheduler) {
        api.responder = { throw UnknownHostException("raw.githubusercontent.com") }
        val vm = createViewModel()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertFalse("finally must turn the spinner off", state.isLoading)
        assertTrue(state.errorMessage!!.startsWith("No internet connection"))
        assertTrue(state.showFullScreenError)
        assertFalse(state.showErrorSnackbar)
    }

    @Test
    fun `timeout and http errors get their own messages`() = runTest(scheduler) {
        api.responder = { throw SocketTimeoutException("timeout") }
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.errorMessage!!.contains("too long"))

        api.responder = {
            throw HttpException(Response.error<Any>(404, "".toResponseBody(null)))
        }
        vm.loadCourses()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.errorMessage!!.contains("HTTP 404"))
    }

    @Test
    fun `retry after an error clears it and loads the courses`() = runTest(scheduler) {
        api.responder = { throw UnknownHostException() }
        val vm = createViewModel()
        advanceUntilIdle()
        assertTrue(vm.uiState.value.showFullScreenError)

        api.responder = { listOf(courseDto("et101")) }
        vm.loadCourses()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertNull(state.errorMessage)
        assertEquals(1, state.courses.size)
        assertEquals(2, api.calls)
    }

    @Test
    fun `retry while a request is running does not start a second one`() = runTest(scheduler) {
        api.responder = {
            delay(1_000)
            listOf(courseDto("et101"))
        }
        val vm = createViewModel()
        runCurrent()

        vm.loadCourses()
        vm.loadCourses()
        advanceUntilIdle()

        assertEquals(1, api.calls)
    }

    @Test
    fun `failed refresh keeps the old courses and uses the snackbar`() = runTest(scheduler) {
        api.responder = { listOf(courseDto("et101"), courseDto("et483")) }
        val vm = createViewModel()
        advanceUntilIdle()

        api.responder = { throw UnknownHostException() }
        vm.loadCourses()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(2, state.courses.size)
        assertTrue(state.showErrorSnackbar)
        assertFalse(state.showFullScreenError)

        vm.onErrorMessageShown()
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `enrollment survives a refresh and drops removed courses`() = runTest(scheduler) {
        api.responder = {
            listOf(courseDto("et101"), courseDto("et310"), courseDto("cs272", credits = 4))
        }
        val vm = createViewModel()
        advanceUntilIdle()
        vm.toggleCourseEnrollment("et101")
        vm.toggleCourseEnrollment("cs272")
        assertEquals(7, vm.uiState.value.totalCredits)

        // The server stops offering cs272
        api.responder = { listOf(courseDto("et101"), courseDto("et310")) }
        vm.loadCourses()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertEquals(setOf("et101"), state.enrolledCourseIds)
        assertEquals(3, state.totalCredits)
    }

    @Test
    fun `search still filters the downloaded courses`() = runTest(scheduler) {
        api.responder = {
            listOf(courseDto("et483", code = "E T 483"), courseDto("et101", code = "E T 101"))
        }
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onSearchQueryChanged("ET483")

        assertEquals(listOf("et483"), vm.uiState.value.visibleCourses.map { it.id })
    }

    // Fake Retrofit service with no network. It records how many times it was
    // called and which dispatcher the call ran on.
    private class FakeCourseApi : CourseApiService {
        var responder: suspend () -> List<CourseDto> = { listOf(courseDto("et101")) }
        var calls = 0
        var lastDispatcher: ContinuationInterceptor? = null

        override suspend fun fetchCourses(): List<CourseDto> {
            calls++
            lastDispatcher = currentCoroutineContext()[ContinuationInterceptor]
            return responder()
        }
    }

    private companion object {
        fun courseDto(
            id: String,
            code: String = id.uppercase(),
            credits: Int = 3,
            prerequisite: String? = null
        ) = CourseDto(
            id = id,
            courseCode = code,
            title = "Course $id",
            creditHours = credits,
            prerequisite = prerequisite,
            tags = listOf("Tag"),
            description = "",
            schedule = "",
            location = "",
            instructor = InstructorDto("Prof", "Title", "p@nmsu.edu", "")
        )
    }
}
