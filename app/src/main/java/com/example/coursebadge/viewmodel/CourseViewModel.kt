package com.example.coursebadge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursebadge.data.CourseRepository
import com.example.coursebadge.model.Course
import com.example.coursebadge.model.matchesQuery
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.UnknownHostException

// Requirement 2: UI state

// Same idea as HW3, one immutable object with everything the screens need.
// For HW4 we added the network state: courses starts empty (since they come
// from the server now); isLoading is true while a request is running; and
// finally errorMessage, which holds a readable message when the last request
// failed.
data class CourseUiState(
    val courses: List<Course> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val enrolledCourseIds: Set<String> = emptySet(),
    // Kept from HW2/HW3 so the prerequisites and the override still work
    val overridePrereqs: Boolean = false,
    val satisfiedPrereqIds: Set<String> = emptySet()
) {
    // Derived values, these are recalculated every time a new copy is made, so
    // they can't get out of sync with the fields above

    val visibleCourses: List<Course> = courses.filter { it.matchesQuery(searchQuery) }

    val enrolledCourses: List<Course> = courses.filter { it.id in enrolledCourseIds }

    val totalCredits: Int = enrolledCourses.sumOf { it.credits }

    // Which loading/error UI to show. This is decided here and not in the
    // composables, so the screen only has to read a Boolean. If nothing was
    // downloaded yet, it shows a full screen spinner or a full screen error; if
    // the courses are already on screen, it keeps showing them, with a centered
    // spinner on top while reloading and a Snackbar if the reload fails. Either
    // way, whenever isLoading is true there is a spinner in the center.
    val hasCourses: Boolean get() = courses.isNotEmpty()
    val showFullScreenLoading: Boolean get() = isLoading && !hasCourses
    val showLoadingOverlay: Boolean get() = isLoading && hasCourses
    val showFullScreenError: Boolean get() = !isLoading && errorMessage != null && !hasCourses
    val showErrorSnackbar: Boolean get() = errorMessage != null && hasCourses

    fun findCourse(courseId: String): Course? = courses.find { it.id == courseId }

    fun isEnrolled(course: Course): Boolean = course.id in enrolledCourseIds

    // a prerequisite counts as satisfied if the override is on or the student
    // marked it for that course. courses with no prerequisite are always fine
    fun isPrereqSatisfied(course: Course): Boolean =
        course.prerequisite == null || overridePrereqs || course.id in satisfiedPrereqIds

    fun canEnroll(course: Course): Boolean = isPrereqSatisfied(course)
}

// The ViewModel is still the single source of truth. What is new in HW4 is that
// it starts the network request, moves it to a background thread, and turns
// the result (or the exception) into state that the UI can show.
//
// The constructor parameters have default values, so viewModel() can still
// create it with no arguments, and the unit tests can pass a fake repository
// and a test dispatcher.
class CourseViewModel(
    private val repository: CourseRepository = CourseRepository(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseUiState())
    val uiState: StateFlow<CourseUiState> = _uiState.asStateFlow()

    // The request that is running right now (if there is one)
    private var loadJob: Job? = null

    init {
        // The catalog is downloaded once, when the ViewModel is created. When the
        // phone rotates the same ViewModel is reused, so it doesn't download it again.
        loadCourses()
    }

    // Requirement 2: coroutine + Dispatchers.IO + try/catch/finally.
    // This is used for the first load, the Retry button, the Snackbar action,
    // the refresh icon and pull to refresh.
    fun loadCourses() {
        // If a request is already running (like a double tap on Retry, or a pull
        // to refresh while it's loading) we don't start a second one
        if (loadJob?.isActive == true) return

        // viewModelScope is tied to the ViewModel's lifecycle. If the user
        // leaves and the ViewModel is cleared, the request gets cancelled
        // automatically, so there are no leaks and no updates to a screen that
        // is already gone.
        loadJob = viewModelScope.launch {
            // This part runs on the main thread, and the UI shows the spinner
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // The network call and the JSON -> Course mapping run on the IO
                // thread pool, that way the main thread stays free to draw the
                // spinner. When it finishes, the coroutine goes back to main.
                val courses = withContext(ioDispatcher) {
                    repository.fetchCourses()
                }
                _uiState.update { state ->
                    val ids = courses.map { it.id }.toSet()
                    state.copy(
                        courses = courses,
                        // If a course was removed from the server, we drop it
                        // from the enrolled/satisfied sets too, so the badge
                        // count doesn't include courses that don't exist anymore
                        enrolledCourseIds = state.enrolledCourseIds intersect ids,
                        satisfiedPrereqIds = state.satisfiedPrereqIds intersect ids
                    )
                }
            } catch (e: CancellationException) {
                // Cancellation is not an error, it has to be rethrown so the
                // coroutine actually stops.
                throw e
            } catch (e: Exception) {
                // No internet, a timeout, an HTTP error or bad JSON. The old list
                // (if there was one) stays on screen and only the message is set.
                _uiState.update { it.copy(errorMessage = e.toUserMessage()) }
            } finally {
                // This runs on success and also on failure, so the spinner can
                // never get stuck on the screen.
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // The Snackbar calls this after it is dismissed, so the same message is
    // not shown again on the next recomposition/rotation.
    fun onErrorMessageShown() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun togglePrerequisiteBypass(bypass: Boolean) {
        _uiState.update { state ->
            if (bypass) {
                state.copy(overridePrereqs = true)
            } else {
                // when the override is turned off, the courses that were only
                // allowed because of the override get unenrolled
                val stillAllowed = state.enrolledCourseIds.filterTo(mutableSetOf()) { id ->
                    val course = state.findCourse(id)
                    course?.prerequisite == null || id in state.satisfiedPrereqIds
                }
                state.copy(overridePrereqs = false, enrolledCourseIds = stillAllowed)
            }
        }
    }

    fun togglePrerequisiteStatus(courseId: String) {
        _uiState.update { state ->
            if (courseId in state.satisfiedPrereqIds) {
                // prerequisite is missing again, so unenroll too, unless the
                // override is still covering it
                val enrolled = if (state.overridePrereqs) state.enrolledCourseIds
                else state.enrolledCourseIds - courseId
                state.copy(
                    satisfiedPrereqIds = state.satisfiedPrereqIds - courseId,
                    enrolledCourseIds = enrolled
                )
            } else {
                state.copy(satisfiedPrereqIds = state.satisfiedPrereqIds + courseId)
            }
        }
    }

    fun toggleCourseEnrollment(courseId: String) {
        _uiState.update { state ->
            val course = state.findCourse(courseId) ?: return@update state
            when {
                state.isEnrolled(course) ->
                    state.copy(enrolledCourseIds = state.enrolledCourseIds - courseId)
                // the button is already disabled in the UI, but the rule is
                // checked here too, so the guard doesn't depend on the screen
                state.canEnroll(course) ->
                    state.copy(enrolledCourseIds = state.enrolledCourseIds + courseId)
                else -> state
            }
        }
    }
}

// Turns the exception into something a student can actually understand. The
// order of the checks matters here, timeouts and "no internet" are both
// IOExceptions, so the specific ones have to be checked before the general one.
internal fun Throwable.toUserMessage(): String = when (this) {
    // SocketTimeoutException and OkHttp's call timeout both extend this one
    is InterruptedIOException ->
        "The course server took too long to respond. Check your connection and try again."
    // This is what you get in airplane mode or with no Wi-Fi, the server name can't be found
    is UnknownHostException ->
        "No internet connection. Turn on Wi-Fi or mobile data and try again."
    is IOException ->
        "Couldn't reach the course server. Check your connection and try again."
    is HttpException ->
        "The course server returned an error (HTTP ${code()}). Please try again later."
    is JsonParseException ->
        "The course catalog came back in an unexpected format."
    else ->
        "Something went wrong while loading courses. Please try again."
}
