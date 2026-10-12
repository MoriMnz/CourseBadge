package com.example.coursebadge.model

// Requirement 1: course model. id is new for HW2, the screen keeps track of
// enrolled courses and prerequisites with sets of ids instead of changing the
// Course objects, so Course stays immutable and the state is easy to save.
data class Course(
    val id: String,
    val courseCode: String,
    val title: String,
    val credits: Int,
    val instructor: String,
    val prerequisite: String?,
    val tags: List<String>
)

object CourseData {

    val courses: List<Course> = listOf(
        Course(
            id = "et483",
            courseCode = "E T 483",
            title = "Mobile App Programming",
            credits = 3,
            instructor = "Prof. Masadeh",
            prerequisite = "E T 382 (Advanced Software Systems)",
            tags = listOf("Kotlin", "Compose", "Android")
        ),
        Course(
            id = "et362",
            courseCode = "E T 362",
            title = "Microcomputer Architecture",
            credits = 4,
            instructor = "Dr. Ramirez",
            prerequisite = "E T 182 (Digital Logic & Circuits)",
            tags = listOf("Assembly", "Embedded", "ARM")
        ),
        Course(
            id = "et101",
            courseCode = "E T 101",
            title = "Intro to Engineering Technology",
            credits = 3,
            instructor = "Dr. Chen",
            // left null on purpose, this one can always be enrolled
            prerequisite = null,
            tags = listOf("Survey", "Freshman", "Lab")
        ),
        Course(
            id = "et583",
            courseCode = "E T 583",
            title = "Graduate Mobile Systems",
            credits = 3,
            instructor = "Prof. Masadeh",
            prerequisite = "Graduate standing",
            tags = listOf("Research", "Compose", "Seminar")
        ),
        Course(
            id = "et555",
            courseCode = "E T 555",
            title = "Virtualization",
            credits = 3,
            instructor = "Dr. Okonkwo",
            prerequisite = "E T 350 (Networking Fundamentals)",
            tags = listOf("VMware", "Hypervisor", "Labs")
        )
    )

    // used by the search box, checks both title and course code so "483" and
    // "mobile" find the same course. empty search shows everything
    fun matchesQuery(course: Course, query: String): Boolean {
        if (query.isBlank()) return true
        val trimmed = query.trim()
        return course.title.contains(trimmed, ignoreCase = true) ||
                course.courseCode.contains(trimmed, ignoreCase = true)
    }
}
