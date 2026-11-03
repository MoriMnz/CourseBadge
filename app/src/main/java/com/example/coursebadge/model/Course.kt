package com.example.coursebadge.model

// domain model, what the whole app (ViewModel + UI) works with.
// id was added in HW2, the app keeps track of enrolled courses and
// prerequisites with sets of ids instead of changing the Course objects,
// so Course stays immutable and the state is easy to save.
// HW3 added the detail fields and instructorImageUrl (loaded by Coil).
// For HW4 the courses are not hard coded here anymore, they are downloaded as
// JSON (see network/CourseDto.kt) and then converted into this class with toDomain().
data class Course(
    val id: String,
    val courseCode: String,
    val title: String,
    val credits: Int,
    val instructor: String,
    val prerequisite: String?,
    val tags: List<String>,
    val description: String,
    val schedule: String,
    val location: String,
    val instructorTitle: String,
    val instructorEmail: String,
    val instructorImageUrl: String
)

// used by the search box, checks both title and course code so "483" and
// "mobile" find the same course. empty search shows everything.
// spaces are ignored for the code, that way "ET483", "ET 483" and
// "E T 483" all work, since the codes are written with a space in the middle
fun Course.matchesQuery(query: String): Boolean {
    if (query.isBlank()) return true
    val trimmed = query.trim()
    val compactQuery = trimmed.replace(" ", "")
    val compactCode = courseCode.replace(" ", "")
    return title.contains(trimmed, ignoreCase = true) ||
            compactCode.contains(compactQuery, ignoreCase = true)
}
