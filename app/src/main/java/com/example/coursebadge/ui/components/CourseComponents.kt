package com.example.coursebadge.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.coursebadge.R

// the pieces in this file are all stateless, same idea as HW2, they show what
// they receive and send events back up with callbacks. the difference is that
// now the events end up in the ViewModel instead of in a parent composable

// header, same as HW2

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

// interactive course card. new for HW3: the instructor picture and
// onCardClick, clicking the card (not the buttons) opens the detail screen

@Composable
fun InteractiveCourseCard(
    courseCode: String,
    title: String,
    credits: Int,
    instructor: String,
    instructorImageUrl: String,
    prerequisite: String?,
    tags: List<String>,
    isEnrolled: Boolean,
    isPrereqSatisfied: Boolean,
    canEnroll: Boolean,
    isOverrideActive: Boolean,
    onTogglePrereq: () -> Unit,
    onToggleEnrollment: () -> Unit,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onCardClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            // enrolled cards change color so you can tell them apart
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

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                InstructorAvatar(imageUrl = instructorImageUrl, name = instructor, size = 36.dp)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Instructor: $instructor",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrerequisiteStatus(
                prerequisite = prerequisite,
                isPrereqSatisfied = isPrereqSatisfied,
                isOverrideActive = isOverrideActive,
                onTogglePrereq = onTogglePrereq
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (tags.isNotEmpty()) {
                TagRow(tags = tags)
                Spacer(modifier = Modifier.height(14.dp))
            }

            EnrollButton(
                isEnrolled = isEnrolled,
                canEnroll = canEnroll,
                onToggleEnrollment = onToggleEnrollment
            )
        }
    }
}

// Requirement 2: Coil image. AsyncImage downloads the picture in the
// background, so scrolling doesn't wait for the network. the placeholder shows
// while it loads, and the same icon is used if it fails or the url is empty

@Composable
fun InstructorAvatar(
    imageUrl: String,
    name: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val placeholder = painterResource(R.drawable.ic_person_placeholder)
    AsyncImage(
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = "Picture of $name",
        placeholder = placeholder,
        error = placeholder,
        fallback = placeholder,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            // the thin outline is so the circle still shows on the detail
            // screen cards, which are the same color as the background
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
    )
}

// these two used to be inline in the card, they were moved out because the
// detail screen shows the same prerequisite chip and enroll button

@Composable
fun PrerequisiteStatus(
    prerequisite: String?,
    isPrereqSatisfied: Boolean,
    isOverrideActive: Boolean,
    onTogglePrereq: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // only clickable if the course has a prerequisite. both lines use the
        // same text size, the null case keeps the HW1 wording and uses the
        // primary color so it stands out from "Prereq: ..."
        if (prerequisite == null) {
            Text(
                text = "Prerequisites: None",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = "Prereq: $prerequisite",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            FilterChip(
                selected = isPrereqSatisfied,
                // disabled while the override is on since clicking it
                // wouldn't change anything anyway
                enabled = !isOverrideActive,
                onClick = onTogglePrereq,
                // by default the selected chip uses the secondary colors, which
                // with dynamic color can be a totally different hue than the rest
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                // the outline is what keeps the chip visible on an enrolled
                // card, since that card is primaryContainer too
                border = FilterChipDefaults.filterChipBorder(
                    enabled = !isOverrideActive,
                    selected = isPrereqSatisfied,
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    selectedBorderWidth = 1.dp
                ),
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
    }
}

@Composable
fun EnrollButton(
    isEnrolled: Boolean,
    canEnroll: Boolean,
    onToggleEnrollment: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Enroll is disabled if prerequisites are missing
        if (isEnrolled) {
            OutlinedButton(
                onClick = onToggleEnrollment,
                modifier = Modifier.fillMaxWidth(),
                // white fill with crimson text and outline, without a fill the
                // button was the same pink as the enrolled card behind it
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
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
                    // says why the button is off instead of repeating the chip
                    text = "Can't enroll until the prerequisite is satisfied",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// smaller pieces used inside the card, one job each

@Composable
fun CodeBadge(code: String, isEnrolled: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (isEnrolled) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primaryContainer
            )
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (isEnrolled) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun TagRow(tags: List<String>) {
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
                // outline instead of the lighter outlineVariant so the tags
                // still show on the enrolled card tint
                border = AssistChipDefaults.assistChipBorder(
                    enabled = true,
                    borderColor = MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}
