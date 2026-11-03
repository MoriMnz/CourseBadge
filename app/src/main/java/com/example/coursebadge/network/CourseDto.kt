package com.example.coursebadge.network

import com.example.coursebadge.model.Course
import com.google.gson.annotations.SerializedName

// Requirement 1: DTOs (Data Transfer Objects)

// These classes match the JSON exactly like the server sends it (snake_case keys
// and the instructor as a nested object). The UI never uses them, the app works
// with the domain Course instead, so if the server renames a field or changes
// the shape, only this file and toDomain() need to change.
//
// Every field is nullable on purpose. Gson creates the objects with reflection
// and basically ignores Kotlin's null safety, so a missing key would end up as
// null inside a "non-null" String and crash later somewhere random. Making them
// nullable forces us to deal with it here, in one place.
data class CourseDto(
    @SerializedName("id") val id: String?,
    @SerializedName("course_code") val courseCode: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("credit_hours") val creditHours: Int?,
    @SerializedName("prerequisite") val prerequisite: String?,
    @SerializedName("tags") val tags: List<String>?,
    @SerializedName("description") val description: String?,
    @SerializedName("schedule") val schedule: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("instructor") val instructor: InstructorDto?
)

data class InstructorDto(
    @SerializedName("name") val name: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("photo_url") val photoUrl: String?
)

// DTO -> domain mapping. The id, code and title are required, without them the
// app can't show or track the course, so that course is skipped (it returns
// null) instead of crashing the whole list. The rest of the fields get safe
// defaults. Additionally, a blank prerequisite is turned into null, that way
// "no prerequisite" always looks the same for the enrollment rules in the ViewModel.
fun CourseDto.toDomain(): Course? {
    val safeId = id?.takeIf { it.isNotBlank() } ?: return null
    val safeCode = courseCode?.takeIf { it.isNotBlank() } ?: return null
    val safeTitle = title?.takeIf { it.isNotBlank() } ?: return null

    return Course(
        id = safeId,
        courseCode = safeCode,
        title = safeTitle,
        credits = creditHours?.coerceAtLeast(0) ?: 0,
        instructor = instructor?.name ?: "Staff",
        prerequisite = prerequisite?.takeIf { it.isNotBlank() },
        tags = tags.orEmpty().filter { it.isNotBlank() },
        description = description.orEmpty(),
        schedule = schedule ?: "TBA",
        location = location ?: "TBA",
        instructorTitle = instructor?.title.orEmpty(),
        instructorEmail = instructor?.email.orEmpty(),
        // Empty url, Coil fails right away and shows the placeholder
        instructorImageUrl = instructor?.photoUrl.orEmpty()
    )
}
