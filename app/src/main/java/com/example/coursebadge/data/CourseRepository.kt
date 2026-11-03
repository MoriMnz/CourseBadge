package com.example.coursebadge.data

import com.example.coursebadge.model.Course
import com.example.coursebadge.network.ApiClient
import com.example.coursebadge.network.CourseApiService
import com.example.coursebadge.network.toDomain
import kotlinx.coroutines.delay

// Data layer. In HW3 the courses were a hard coded list in CourseData, now they
// come from the network and this class is the only one that knows that. The
// ViewModel asks for a List<Course> and it doesn't care if it came from
// Retrofit, a database or a fake in a unit test.
//
// The api is a constructor parameter so the unit tests can pass a fake
// CourseApiService instead of making real network calls.
class CourseRepository(
    private val api: CourseApiService = ApiClient.instance,
    private val simulatedLatencyMs: Long = DEMO_LATENCY_MS
) {

    // It is a suspend function, so it waits for the network without blocking a
    // thread. The ViewModel calls it inside withContext(Dispatchers.IO), that way
    // the request and the JSON mapping both run off the main thread.
    // Errors are not caught here on purpose, they go up to the ViewModel, which
    // is the one that decides what message to show.
    suspend fun fetchCourses(): List<Course> {
        if (simulatedLatencyMs > 0) {
            // delay() suspends the coroutine, it doesn't block the thread like Thread.sleep()
            delay(simulatedLatencyMs)
        }
        // DTO -> domain. mapNotNull drops any course that came back without an
        // id, code or title, instead of failing the whole catalog.
        return api.fetchCourses().mapNotNull { it.toDomain() }
    }

    companion object {
        // GitHub answers in a few hundred ms, which is too fast to even see the
        // loading spinner. This small wait is only for the demo screenshots, it
        // should be set to 0 for a real release.
        const val DEMO_LATENCY_MS = 1_200L
    }
}
