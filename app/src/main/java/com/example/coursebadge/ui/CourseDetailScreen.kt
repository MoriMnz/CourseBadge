package com.example.coursebadge.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.coursebadge.R
import com.example.coursebadge.model.Course
import com.example.coursebadge.ui.components.CodeBadge
import com.example.coursebadge.ui.theme.CourseBadgeTheme
import com.example.coursebadge.ui.theme.crimsonTopAppBarColors
import com.example.coursebadge.ui.components.EnrollButton
import com.example.coursebadge.ui.components.InstructorAvatar
import com.example.coursebadge.ui.components.PrerequisiteStatus
import com.example.coursebadge.ui.components.TagRow
import com.example.coursebadge.viewmodel.CourseUiState
import com.example.coursebadge.viewmodel.CourseViewModel

// Screen B: course detail

// receives only the courseId from the navigation argument, and then looks up
// the course in the same ViewModel the dashboard uses. because both screens
// read the same state, enrolling from here also updates the credits on the
// dashboard when you go back
@Composable
fun CourseDetailScreen(
    courseId: String,
    viewModel: CourseViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    CourseDetailContent(
        courseId = courseId,
        state = state,
        onTogglePrereq = { viewModel.togglePrerequisiteStatus(courseId) },
        onToggleEnrollment = { viewModel.toggleCourseEnrollment(courseId) },
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDetailContent(
    courseId: String,
    state: CourseUiState,
    onTogglePrereq: () -> Unit,
    onToggleEnrollment: () -> Unit,
    onBackClick: () -> Unit
) {
    // can be null if the id in the route doesn't match any course
    val course = state.findCourse(courseId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(course?.courseCode ?: "Course Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = "Back"
                        )
                    }
                },
                colors = crimsonTopAppBarColors()
            )
        }
    ) { innerPadding ->
        if (course == null) {
            Text(
                text = "Course \"$courseId\" was not found.",
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(16.dp)
            )
        } else {
            // a normal scrolling Column is fine here, this screen only has a
            // few fixed sections, LazyColumn is for long lists
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CourseOverviewSection(course = course, isEnrolled = state.isEnrolled(course))
                InstructorSection(course = course)

                DetailSection(title = "Prerequisite & Enrollment") {
                    PrerequisiteStatus(
                        prerequisite = course.prerequisite,
                        isPrereqSatisfied = state.isPrereqSatisfied(course),
                        isOverrideActive = state.overridePrereqs,
                        onTogglePrereq = onTogglePrereq
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    EnrollButton(
                        isEnrolled = state.isEnrolled(course),
                        canEnroll = state.canEnroll(course),
                        onToggleEnrollment = onToggleEnrollment
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Total enrolled: ${state.totalCredits} hrs",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CourseOverviewSection(course: Course, isEnrolled: Boolean) {
    DetailSection(title = null) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CodeBadge(code = course.courseCode, isEnrolled = isEnrolled)
            Text(
                text = "${course.credits} CREDITS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = course.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = course.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        DetailRow(label = "Schedule", value = course.schedule)
        DetailRow(label = "Location", value = course.location)

        Spacer(modifier = Modifier.height(12.dp))

        TagRow(tags = course.tags)
    }
}

@Composable
private fun InstructorSection(course: Course) {
    DetailSection(title = "Instructor") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // same composable as the card, just bigger
            InstructorAvatar(
                imageUrl = course.instructorImageUrl,
                name = course.instructor,
                size = 72.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = course.instructor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = course.instructorEmail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = course.instructorTitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// card with an optional title, used for every section so they all look the same
@Composable
private fun DetailSection(
    title: String?,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            content()
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Detail - prerequisite missing", showBackground = true, heightDp = 1000)
@Composable
fun CourseDetailPreview() {
    CourseBadgeTheme {
        CourseDetailContent(
            courseId = "et362",
            state = CourseUiState(),
            onTogglePrereq = { },
            onToggleEnrollment = { },
            onBackClick = { }
        )
    }
}
