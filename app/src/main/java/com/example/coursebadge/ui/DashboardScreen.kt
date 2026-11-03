package com.example.coursebadge.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.coursebadge.R
import com.example.coursebadge.ui.components.EnrollmentHeader
import com.example.coursebadge.ui.components.InteractiveCourseCard
import com.example.coursebadge.ui.theme.CourseBadgeTheme
import com.example.coursebadge.ui.theme.crimsonTopAppBarColors
import com.example.coursebadge.viewmodel.CourseUiState
import com.example.coursebadge.viewmodel.CourseViewModel

// Screen A: dashboard / catalog

// This is the only part of the screen that knows the ViewModel exists. It
// collects the StateFlow and turns it into plain values and callbacks for
// DashboardContent. For HW4 we added two more callbacks, onRetry (used by the
// Retry button, the Snackbar action, pull to refresh and the refresh icon) and
// onErrorShown.
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
        onCourseClick = onCourseClick,
        onRetry = viewModel::loadCourses,
        onErrorShown = viewModel::onErrorMessageShown
    )
}

// Stateless version of the screen, it is separated so the previews can pass a
// fake CourseUiState without needing a real ViewModel (or a network connection).
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: CourseUiState,
    onSearchQueryChange: (String) -> Unit,
    onOverrideToggle: (Boolean) -> Unit,
    onTogglePrereq: (String) -> Unit,
    onToggleEnrollment: (String) -> Unit,
    onCourseClick: (String) -> Unit,
    onRetry: () -> Unit,
    onErrorShown: () -> Unit
) {
    // the dialog being open or closed is only about how the screen looks, not
    // about enrollment, so this is the one piece of state that stays local
    var showSummary by rememberSaveable { mutableStateOf(false) }

    // Requirement 2: Snackbar for errors. It is only used when a refresh fails
    // and the old courses are still on the screen. If nothing was loaded yet,
    // the full screen error below is shown instead.
    val snackbarHostState = remember { SnackbarHostState() }
    val currentOnRetry by rememberUpdatedState(onRetry)
    val currentOnErrorShown by rememberUpdatedState(onErrorShown)
    LaunchedEffect(state.showErrorSnackbar, state.errorMessage) {
        val message = state.errorMessage
        if (state.showErrorSnackbar && message != null) {
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = "Retry",
                duration = SnackbarDuration.Long
            )
            // Retry starts a new request (which clears the error by itself),
            // otherwise we just tell the ViewModel that the message was seen
            if (result == SnackbarResult.ActionPerformed) currentOnRetry()
            else currentOnErrorShown()
        }
    }

    // Requirement 3 (HW3): Scaffold + TopAppBar with action icons
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NMSU Course Catalog & Tracker") },
                colors = crimsonTopAppBarColors(),
                actions = {
                    // Manual refresh button, it is disabled while a request is running
                    IconButton(onClick = onRetry, enabled = !state.isLoading) {
                        Icon(
                            painter = painterResource(R.drawable.ic_refresh),
                            contentDescription = "Refresh courses"
                        )
                    }
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
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->

        // Requirement 3: pull to refresh. Dragging the list down calls onRetry.
        // isRefreshing is always false on purpose, the pull indicator only
        // follows the finger and goes back when you let go, and the loading
        // itself is shown by the centered spinner below. This way there is only
        // one spinner and it is always in the middle, no matter how the load
        // started.
        PullToRefreshBox(
            isRefreshing = false,
            onRefresh = onRetry,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // The screen only picks which UI to show, the decision itself
            // (showFullScreenLoading, showLoadingOverlay, etc.) is made in CourseUiState
            when {
                state.showFullScreenLoading -> LoadingState()

                state.showFullScreenError -> ErrorState(
                    message = state.errorMessage.orEmpty(),
                    onRetry = onRetry
                )

                else -> Box(modifier = Modifier.fillMaxSize()) {
                    CourseList(
                        state = state,
                        onSearchQueryChange = onSearchQueryChange,
                        onOverrideToggle = onOverrideToggle,
                        onTogglePrereq = onTogglePrereq,
                        onToggleEnrollment = onToggleEnrollment,
                        onCourseClick = onCourseClick
                    )
                    // Refreshing with the courses already on screen, the old list
                    // stays visible and the spinner goes on top of it, centered
                    if (state.showLoadingOverlay) {
                        LoadingOverlay()
                    }
                }
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

// The LazyColumn from HW3, it is basically the same, the only change is that
// now it is its own function
@Composable
private fun CourseList(
    state: CourseUiState,
    onSearchQueryChange: (String) -> Unit,
    onOverrideToggle: (Boolean) -> Unit,
    onTogglePrereq: (String) -> Unit,
    onToggleEnrollment: (String) -> Unit,
    onCourseClick: (String) -> Unit
) {
    // LazyColumn only composes the cards that are actually visible on the screen
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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

        // key = id, so when the search filters the list (or a refresh brings a
        // new list from the server) Compose knows which card is which, and
        // reuses them instead of rebuilding all of them.
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

// Requirement 3: centered spinner for a refresh. It sits on a small round
// surface with a shadow, so it stays visible on top of the course cards.
@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 6.dp
        ) {
            CircularProgressIndicator(modifier = Modifier.padding(14.dp))
        }
    }
}

// Requirement 3: spinner centered on the screen while the catalog is downloading
@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading course catalog…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Requirement 3: error state with a Retry button. It scrolls (even though it
// fits on the screen) so pull to refresh also works from here.
@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_cloud_off),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Couldn't load courses",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Icon(
                painter = painterResource(R.drawable.ic_refresh),
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
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
            // A blank query and no courses means the server sent an empty catalog
            text = if (query.isBlank()) "No courses are available right now."
            else "No courses match \"$query\"",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(20.dp)
        )
    }
}

// Previews, they use DashboardContent with a fixed state. There is one for
// each network state: loaded, refreshing, loading and error.

private val loadedPreviewState = CourseUiState(
    courses = previewCourses,
    enrolledCourseIds = setOf("et101", "et483"),
    satisfiedPrereqIds = setOf("et483")
)

@Composable
private fun DashboardPreviewContent(state: CourseUiState) {
    CourseBadgeTheme {
        DashboardContent(
            state = state,
            onSearchQueryChange = { },
            onOverrideToggle = { },
            onTogglePrereq = { },
            onToggleEnrollment = { },
            onCourseClick = { },
            onRetry = { },
            onErrorShown = { }
        )
    }
}

@Preview(name = "Dashboard - Light", showBackground = true, heightDp = 1200)
@Composable
fun DashboardPreview() {
    DashboardPreviewContent(loadedPreviewState)
}

@Preview(
    name = "Dashboard - Dark",
    showBackground = true,
    heightDp = 1200,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun DashboardDarkPreview() {
    DashboardPreviewContent(loadedPreviewState)
}

@Preview(name = "Dashboard - Loading", showBackground = true)
@Composable
fun DashboardLoadingPreview() {
    DashboardPreviewContent(CourseUiState(isLoading = true))
}

@Preview(name = "Dashboard - Refreshing", showBackground = true, heightDp = 900)
@Composable
fun DashboardRefreshingPreview() {
    DashboardPreviewContent(loadedPreviewState.copy(isLoading = true))
}

@Preview(name = "Dashboard - Error", showBackground = true)
@Composable
fun DashboardErrorPreview() {
    DashboardPreviewContent(
        CourseUiState(
            errorMessage = "No internet connection. Turn on Wi-Fi or mobile data and try again."
        )
    )
}
