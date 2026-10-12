package com.example.coursebadge.model

/**
 * Requirement 1 — Data Model Updates.
 *
 * `id` is new in Homework 2. The screen tracks enrollment and prerequisite
 * status in Sets of ids rather than in the Course objects themselves, which
 * keeps Course immutable and makes the state cheap to save and restore.
 */
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
            // Null on purpose: this course is always enrollable.
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

    /**
     * Matches a course against the search box. Checked against both the title
     * and the course code so "483" and "mobile" both find the same course.
     * A blank query matches everything.
     */
    fun matchesQuery(course: Course, query: String): Boolean {
        if (query.isBlank()) return true
        val trimmed = query.trim()
        return course.title.contains(trimmed, ignoreCase = true) ||
                course.courseCode.contains(trimmed, ignoreCase = true)
    }
}
