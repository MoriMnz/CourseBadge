package com.example.coursebadge.model

/**
 * Requirement 1 — Kotlin Data Architecture.
 *
 * Immutable model of a single course offering. `prerequisite` is declared
 * nullable (String?) so the UI layer is forced to handle the "no prerequisite"
 * case explicitly instead of relying on a magic string like "" or "None".
 */
data class Course(
    val courseCode: String,
    val title: String,
    val credits: Int,
    val instructor: String,
    val prerequisite: String?,
    val tags: List<String>
)

/**
 * Singleton data source. `object` guarantees a single instance for the whole
 * process, so every composable reads the same backing list.
 */
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
            // Null on purpose: exercises the null-safety guard in the UI.
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

    /** Higher-order function usage: sumOf with a lambda. */
    val totalCredits: Int
        get() = courses.sumOf { it.credits }

    /** Lambda + filter: courses a student can take with no prior coursework. */
    fun openEnrollmentCourses(): List<Course> =
        courses.filter { it.prerequisite == null }

    /** Lambda + map/joinToString: flattens tags for logging or search. */
    fun allTags(): String =
        courses.flatMap { it.tags }.distinct().sorted().joinToString(separator = ", ")
}
