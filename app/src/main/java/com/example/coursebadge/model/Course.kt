package com.example.coursebadge.model

// Requirement 1: course model. prerequisite is nullable on purpose so we
// actually have to handle the "no prerequisite" case instead of just
// using an empty string as a placeholder.
data class Course(
    val courseCode: String,
    val title: String,
    val credits: Int,
    val instructor: String,
    val prerequisite: String?,
    val tags: List<String>
)

// object here since we only need one instance of this data for the whole app
object CourseData {

    val courses: List<Course> = listOf(
        Course(
            courseCode = "E T 483",
            title = "Mobile App Programming",
            credits = 3,
            instructor = "Prof. Masadeh",
            prerequisite = "E T 382 (Advanced Software Systems)",
            tags = listOf("Kotlin", "Compose", "Android")
        ),
        Course(
            courseCode = "E T 362",
            title = "Microcomputer Architecture",
            credits = 4,
            instructor = "Dr. Ramirez",
            prerequisite = "E T 182 (Digital Logic & Circuits)",
            tags = listOf("Assembly", "Embedded", "ARM")
        ),
        Course(
            courseCode = "E T 101",
            title = "Intro to Engineering Technology",
            credits = 3,
            // left null on purpose to test the null-safety guard in the UI
            prerequisite = null,
            instructor = "Dr. Chen",
            tags = listOf("Survey", "Freshman", "Lab")
        ),
        Course(
            courseCode = "E T 583",
            title = "Graduate Mobile Systems",
            credits = 3,
            instructor = "Prof. Masadeh",
            prerequisite = "Graduate standing",
            tags = listOf("Research", "Compose", "Seminar")
        )
    )

    // adds up credits across all courses
    val totalCredits: Int
        get() = courses.sumOf { it.credits }

    // courses with no prerequisite, so a first-time student could take these
    fun openEnrollmentCourses(): List<Course> =
        courses.filter { it.prerequisite == null }

    // combines all tags into one string, mainly useful for logging/search
    fun allTags(): String =
        courses.flatMap { it.tags }.distinct().sorted().joinToString(separator = ", ")
}
