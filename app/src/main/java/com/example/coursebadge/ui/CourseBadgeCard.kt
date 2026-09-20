package com.example.coursebadge.ui

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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.coursebadge.model.Course
import com.example.coursebadge.model.CourseData

/* ------------------------------------------------------------------ */
/*  Requirement 2 — Screen-level container                            */
/* ------------------------------------------------------------------ */

/**
 * Root screen. A single vertically scrolling Column holds the header and every
 * course card, with an explicit Spacer between cards as required.
 */
@Composable
fun CourseListScreen(
    modifier: Modifier = Modifier,
    courses: List<Course> = CourseData.courses
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        ScreenHeader(courseCount = courses.size, totalCredits = CourseData.totalCredits)

        Spacer(modifier = Modifier.height(20.dp))

        // Higher-order function: forEachIndexed drives both rendering and spacing.
        courses.forEachIndexed { index, course ->
            CourseBadgeCard(course = course)
            if (index != courses.lastIndex) {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ScreenHeader(courseCount: Int, totalCredits: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "Fall 2026 Catalog",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Material 3 - Jetpack Compose",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Text(
                text = "$courseCount courses",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

/* ------------------------------------------------------------------ */
/*  The custom composable required by the assignment                  */
/* ------------------------------------------------------------------ */

@Composable
fun CourseBadgeCard(
    course: Course,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp)),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // --- Top row: course code on the left, credits on the right ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CodeBadge(code = course.courseCode)
                Text(
                    text = "${course.credits} CREDITS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Title: bold, large typography ---
            Text(
                text = course.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // --- Labeled instructor line ---
            InstructorRow(instructor = course.instructor)

            Spacer(modifier = Modifier.height(12.dp))

            // --- Null-safety UI guard ---
            PrerequisiteBanner(prerequisite = course.prerequisite)

            Spacer(modifier = Modifier.height(14.dp))

            // --- Tags ---
            TagRow(tags = course.tags)
        }
    }
}

/* ------------------------------------------------------------------ */
/*  Sub-views: each one does a single job                             */
/* ------------------------------------------------------------------ */

@Composable
private fun CodeBadge(code: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun InstructorRow(instructor: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Instructor:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = instructor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Null-safety guard. The Elvis operator supplies the fallback text, and the
 * same null check drives the color pair so "None" reads as visually distinct
 * from a real prerequisite.
 */
@Composable
private fun PrerequisiteBanner(prerequisite: String?) {
    val hasPrereq = prerequisite != null

    // Elvis operator: fall back to the "none" string when null.
    val label = prerequisite?.let { "Prereq: $it" } ?: "Prerequisites: None"

    val containerColor =
        if (hasPrereq) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.tertiaryContainer

    val contentColor =
        if (hasPrereq) MaterialTheme.colorScheme.onSecondaryContainer
        else MaterialTheme.colorScheme.onTertiaryContainer

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}

@Composable
private fun TagRow(tags: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Higher-order function: forEach emits one chip per tag.
        tags.forEach { tag ->
            AssistChip(
                onClick = { /* no navigation required for this assignment */ },
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

/* ------------------------------------------------------------------ */
/*  Requirement 3 — Preview                                           */
/* ------------------------------------------------------------------ */

@Preview(showBackground = true, heightDp = 1400)
@Composable
fun CourseListPreview() {
    MaterialTheme {
        CourseListScreen(courses = CourseData.courses)
    }
}

@Preview(showBackground = true, name = "Single card - null prerequisite")
@Composable
fun SingleCardPreview() {
    MaterialTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            CourseBadgeCard(course = CourseData.courses.first { it.prerequisite == null })
        }
    }
}