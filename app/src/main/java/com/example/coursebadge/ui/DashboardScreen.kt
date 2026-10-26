package com.example.coursebadge.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.coursebadge.R
import com.example.coursebadge.ui.components.EnrollmentHeader
import com.example.coursebadge.ui.theme.CourseBadgeTheme
import com.example.coursebadge.ui.theme.crimsonTopAppBarColors
import com.example.coursebadge.ui.components.InteractiveCourseCard
import com.example.coursebadge.viewmodel.CourseUiState
import com.example.coursebadge.viewmodel.CourseViewModel

// Screen A: dashboard / catalog

// this one is the only part of the screen that knows the ViewModel exists. it
// collects the StateFlow and turns it into plain values and callbacks for
// DashboardContent, which is the same thing CourseTrackerScreen did in HW2,
// except the state isn't stored here anymore, it is only read
@Composable
fun DashboardScreen(
    viewModel: CourseViewModel,
    onCourseClick: (String) -> Unit
) {
    // collectAsState subscribes to the StateFlow, every time the ViewModel
    // emits a new CourseUiState this recomposes with the new value
    val state by viewModel.uiState.collectAsState()

    DashboardContent(
        state = state,
        onSearchQueryChange = viewModel::onSearchQueryChanged,
        onOverrideToggle = viewModel::togglePrerequisiteBypass,
        onTogglePrereq = viewModel::togglePrerequisiteStatus,
        onToggleEnrollment = viewModel::toggleCourseEnrollment,
        onCourseClick = onCourseClick
    )
}

// stateless version of the screen, separated so the previews can pass a fake
// CourseUiState without needing a real ViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: CourseUiState,
    onSearchQueryChange: (String) -> Unit,
    onOverrideToggle: (Boolean) -> Unit,
    onTogglePrereq: (String) -> Unit,
    onToggleEnrollment: (String) -> Unit,
    onCourseClick: (String) -> Unit
) {
    // the dialog being open or closed is only about how the screen looks, not
    // about enrollment, so this is the one piece of state that stays local
    var showSummary by rememberSaveable { mutableStateOf(false) }

    // Requirement 3: Scaffold + TopAppBar with an action icon
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NMSU Course Catalog & Tracker") },
                colors = crimsonTopAppBarColors(),
                actions = {
                    // badge with the number of enrolled courses, clicking it
                    // opens a quick summary of the schedule
                    IconButton(onClick = { showSummary = true }) {
                        BadgedBox(
                            badge = {
                                if (state.enrolledCourseIds.isNotEmpty()) {
                                    // white badge with crimson number, the default
                                    // red one didn't show up on the crimson bar
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text("${state.enrolledCourseIds.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_school),
                                contentDescription = "Enrollment summary"
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        // Requirement 2: LazyColumn instead of Column + verticalScroll. the
        // old version built every card even if it was off screen, this one
        // only composes the cards that are actually visible
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item(key = "header") {
                EnrollmentHeader(
                    totalCredits = state.totalCredits,
                    enrolledCount = state.enrolledCourseIds.size,
                    searchQuery = state.searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    overridePrereqs = state.overridePrereqs,
                    onOverrideToggle = onOverrideToggle
                )
            }

            if (state.visibleCourses.isEmpty()) {
                item(key = "empty") {
                    EmptyResults(query = state.searchQuery)
                }
            }

            // key = id, so when the search filters the list Compose knows
            // which card is which and reuses them instead of rebuilding all
            items(items = state.visibleCourses, key = { it.id }) { course ->
                InteractiveCourseCard(
                    courseCode = course.courseCode,
                    title = course.title,
                    credits = course.credits,
                    instructor = course.instructor,
                    instructorImageUrl = course.instructorImageUrl,
                    prerequisite = course.prerequisite,
                    tags = course.tags,
                    isEnrolled = state.isEnrolled(course),
                    isPrereqSatisfied = state.isPrereqSatisfied(course),
                    canEnroll = state.canEnroll(course),
                    isOverrideActive = state.overridePrereqs,
                    onTogglePrereq = { onTogglePrereq(course.id) },
                    onToggleEnrollment = { onToggleEnrollment(course.id) },
                    onCardClick = { onCourseClick(course.id) }
                )
            }
        }
    }

    if (showSummary) {
        EnrollmentSummaryDialog(
            state = state,
            onDismiss = { showSummary = false }
        )
    }
}

@Composable
private fun EnrollmentSummaryDialog(
    state: CourseUiState,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        },
        title = { Text("My Schedule") },
        text = {
            if (state.enrolledCourses.isEmpty()) {
                Text("You are not enrolled in any courses yet.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.enrolledCourses.forEach { course ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${course.courseCode}  ${course.title}")
                            Text("${course.credits}")
                        }
                    }
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total credits", fontWeight = FontWeight.Bold)
                        Text("${state.totalCredits}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    )
}

@Composable
private fun EmptyResults(query: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Text(
            text = "No courses match \"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(20.dp)
        )
    }
}

// previews, they use DashboardContent with a fixed state, one with a couple of
// courses enrolled so the enrolled colors and the badge show up

private val previewState = CourseUiState(
    enrolledCourseIds = setOf("et101", "et483"),
    satisfiedPrereqIds = setOf("et483")
)

@Preview(name = "Dashboard - Light", showBackground = true, heightDp = 1200)
@Composable
fun DashboardPreview() {
    CourseBadgeTheme {
        DashboardContent(
            state = previewState,
            onSearchQueryChange = { },
            onOverrideToggle = { },
            onTogglePrereq = { },
            onToggleEnrollment = { },
            onCourseClick = { }
        )
    }
}

@Preview(
    name = "Dashboard - Dark",
    showBackground = true,
    heightDp = 1200,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun DashboardDarkPreview() {
    CourseBadgeTheme {
        DashboardContent(
            state = previewState,
            onSearchQueryChange = { },
            onOverrideToggle = { },
            onTogglePrereq = { },
            onToggleEnrollment = { },
            onCourseClick = { }
        )
    }
}
