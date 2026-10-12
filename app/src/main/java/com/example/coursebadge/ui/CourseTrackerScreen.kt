package com.example.coursebadge.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.coursebadge.model.Course
import com.example.coursebadge.model.CourseData

/* ================================================================== */
/*  Requirement 4 — State hoisting and integration                    */
/* ================================================================== */

/**
 * The only stateful composable in the file. Every piece of mutable state lives
 * here and flows down to the children as plain values; the children report user
 * actions back up through callbacks. That keeps EnrollmentHeader and
 * InteractiveCourseCard stateless, reusable, and previewable in isolation.
 */
@Composable
fun CourseTrackerScreen(
    modifier: Modifier = Modifier,
    courses: List<Course> = CourseData.courses
) {
    // rememberSaveable survives configuration changes (screen rotation),
    // unlike plain remember, which would reset all four of these.
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var overridePrereqs by rememberSaveable { mutableStateOf(false) }
    var enrolledIds by rememberSaveable { mutableStateOf(setOf<String>()) }
    var satisfiedPrereqIds by rememberSaveable { mutableStateOf(setOf<String>()) }

    // Derived values — recomputed on each recomposition rather than stored,
    // so they can never drift out of sync with the state above.
    val totalCredits = courses
        .filter { it.id in enrolledIds }
        .sumOf { it.credits }

    val visibleCourses = courses.filter { CourseData.matchesQuery(it, searchQuery) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EnrollmentHeader(
            totalCredits = totalCredits,
            enrolledCount = enrolledIds.size,
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            overridePrereqs = overridePrereqs,
            onOverrideToggle = { enabled ->
                overridePrereqs = enabled
                if (!enabled) {
                    // Turning the override off revokes any enrollment that only
                    // the override made legal, so the enrolled set can never
                    // contradict the prerequisite rules.
                    enrolledIds = enrolledIds.filterTo(mutableSetOf()) { id ->
                        val course = courses.first { it.id == id }
                        course.prerequisite == null || id in satisfiedPrereqIds
                    }
                }
            }
        )

        if (visibleCourses.isEmpty()) {
            EmptyResults(query = searchQuery)
        }

        visibleCourses.forEach { course ->
            // A course is enrollable when it has no prerequisite at all, when
            // the global override is on, or when this specific prerequisite
            // has been marked satisfied.
            val prereqSatisfied = overridePrereqs || course.id in satisfiedPrereqIds
            val canEnroll = course.prerequisite == null || prereqSatisfied

            InteractiveCourseCard(
                courseCode = course.courseCode,
                title = course.title,
                credits = course.credits,
                instructor = course.instructor,
                prerequisite = course.prerequisite,
                tags = course.tags,
                isEnrolled = course.id in enrolledIds,
                isPrereqSatisfied = prereqSatisfied,
                canEnroll = canEnroll,
                isOverrideActive = overridePrereqs,
                onTogglePrereq = {
                    // Set +/- returns a new Set, so the state object changes
                    // identity and Compose sees the update.
                    satisfiedPrereqIds = if (course.id in satisfiedPrereqIds) {
                        // Withdrawing the prerequisite also withdraws the
                        // enrollment that depended on it.
                        enrolledIds = enrolledIds - course.id
                        satisfiedPrereqIds - course.id
                    } else {
                        satisfiedPrereqIds + course.id
                    }
                },
                onToggleEnrollment = {
                    enrolledIds = if (course.id in enrolledIds) {
                        enrolledIds - course.id
                    } else {
                        enrolledIds + course.id
                    }
                }
            )
        }
    }
}

/* ================================================================== */
/*  Requirement 2 — Stateless header summary                          */
/* ================================================================== */

@Composable
fun EnrollmentHeader(
    totalCredits: Int,
    enrolledCount: Int,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    overridePrereqs: Boolean,
    onOverrideToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Enrolled Credits: $totalCredits hrs",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = if (enrolledCount == 1) "1 course enrolled"
                else "$enrolledCount courses enrolled",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Search Courses") },
                placeholder = { Text("Title or course code") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bypass Prerequisite Checks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Switch(
                    checked = overridePrereqs,
                    onCheckedChange = onOverrideToggle
                )
            }
        }
    }
}

/* ================================================================== */
/*  Requirement 3 — Interactive course card                           */
/* ================================================================== */

@Composable
fun InteractiveCourseCard(
    courseCode: String,
    title: String,
    credits: Int,
    instructor: String,
    prerequisite: String?,
    tags: List<String>,
    isEnrolled: Boolean,
    isPrereqSatisfied: Boolean,
    canEnroll: Boolean,
    isOverrideActive: Boolean,
    onTogglePrereq: () -> Unit,
    onToggleEnrollment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            // Selection state: enrolled cards switch container color.
            containerColor = if (isEnrolled) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isEnrolled) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CodeBadge(code = courseCode, isEnrolled = isEnrolled)
                Text(
                    text = "$credits CREDITS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = if (isEnrolled) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Instructor: $instructor",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- Prerequisite row: null-safe, and interactive when present ---
            if (prerequisite == null) {
                Text(
                    text = "No prerequisites",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Prereq: $prerequisite",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                FilterChip(
                    selected = isPrereqSatisfied,
                    // Disabled while the global override is on, because an
                    // individual toggle cannot change the outcome then.
                    enabled = !isOverrideActive,
                    onClick = onTogglePrereq,
                    label = {
                        Text(
                            text = when {
                                isOverrideActive -> "Satisfied (overridden)"
                                isPrereqSatisfied -> "Prerequisite satisfied"
                                else -> "Prerequisite missing"
                            }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (tags.isNotEmpty()) {
                TagRow(tags = tags)
                Spacer(modifier = Modifier.height(14.dp))
            }

            // --- Enrollment control ---
            if (isEnrolled) {
                OutlinedButton(
                    onClick = onToggleEnrollment,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Unenroll")
                }
            } else {
                Button(
                    onClick = onToggleEnrollment,
                    enabled = canEnroll,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Enroll")
                }

                if (!canEnroll) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Prerequisites missing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/* ================================================================== */
/*  Small shared pieces                                               */
/* ================================================================== */

@Composable
private fun CodeBadge(code: String, isEnrolled: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (isEnrolled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondaryContainer
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (isEnrolled) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
private fun TagRow(tags: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            AssistChip(
                onClick = { },
                label = {
                    Text(
                        text = tag.uppercase(),
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
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

/* ================================================================== */
/*  Previews                                                          */
/* ================================================================== */

@Preview(name = "Light Mode", showBackground = true, heightDp = 1800)
@Composable
fun TrackerPreview() {
    MaterialTheme {
        CourseTrackerScreen()
    }
}

@Preview(
    name = "Dark Mode",
    showBackground = true,
    heightDp = 1800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun TrackerDarkPreview() {
    MaterialTheme {
        CourseTrackerScreen()
    }
}

@Preview(name = "Card - enrolled", showBackground = true)
@Composable
fun EnrolledCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            InteractiveCourseCard(
                courseCode = "E T 483",
                title = "Mobile App Programming",
                credits = 3,
                instructor = "Prof. Masadeh",
                prerequisite = "E T 382 (Advanced Software Systems)",
                tags = listOf("Kotlin", "Compose"),
                isEnrolled = true,
                isPrereqSatisfied = true,
                canEnroll = true,
                isOverrideActive = false,
                onTogglePrereq = { },
                onToggleEnrollment = { }
            )
        }
    }
}

@Preview(name = "Card - blocked", showBackground = true)
@Composable
fun BlockedCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            InteractiveCourseCard(
                courseCode = "E T 362",
                title = "Microcomputer Architecture",
                credits = 4,
                instructor = "Dr. Ramirez",
                prerequisite = "E T 182 (Digital Logic & Circuits)",
                tags = listOf("Assembly", "ARM"),
                isEnrolled = false,
                isPrereqSatisfied = false,
                canEnroll = false,
                isOverrideActive = false,
                onTogglePrereq = { },
                onToggleEnrollment = { }
            )
        }
    }
}
