package com.example.coursebadge.viewmodel

import androidx.lifecycle.ViewModel
import com.example.coursebadge.model.Course
import com.example.coursebadge.model.CourseData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Requirement 1: UI state + ViewModel

// these are the same four values that were rememberSaveable variables in the
// HW2 screen, now they live together in one immutable object. every change
// makes a new copy, so the screen always gets a full snapshot of the state
data class CourseUiState(
    val courses: List<Course> = CourseData.courses,
    val searchQuery: String = "",
    val overridePrereqs: Boolean = false,
    val enrolledCourseIds: Set<String> = emptySet(),
    val satisfiedPrereqIds: Set<String> = emptySet()
) {
    // derived values, calculated from the fields above every time a new copy
    // is made, so they can't get out of sync with the state
    val visibleCourses: List<Course> =
        courses.filter { CourseData.matchesQuery(it, searchQuery) }

    val enrolledCourses: List<Course> =
        courses.filter { it.id in enrolledCourseIds }

    val totalCredits: Int = enrolledCourses.sumOf { it.credits }

    fun findCourse(courseId: String): Course? = courses.find { it.id == courseId }

    fun isEnrolled(course: Course): Boolean = course.id in enrolledCourseIds

    // a prerequisite counts as satisfied if the override is on or the student
    // marked it for that course. courses with no prerequisite are always fine
    fun isPrereqSatisfied(course: Course): Boolean =
        course.prerequisite == null || overridePrereqs || course.id in satisfiedPrereqIds

    fun canEnroll(course: Course): Boolean = isPrereqSatisfied(course)
}

// the ViewModel is now the single source of truth. the composables don't
// change anything themselves, they only read uiState and call these functions,
// and since a ViewModel survives configuration changes, rotating the phone
// doesn't reset anything
class CourseViewModel : ViewModel() {

    // private and mutable inside, public and read only outside, that way only
    // the ViewModel can actually change the state
    private val _uiState = MutableStateFlow(CourseUiState())
    val uiState: StateFlow<CourseUiState> = _uiState.asStateFlow()

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
