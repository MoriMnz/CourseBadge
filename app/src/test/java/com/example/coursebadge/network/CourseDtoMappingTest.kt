package com.example.coursebadge.network

import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class CourseDtoMappingTest {

    private fun dto(
        id: String? = "et483",
        courseCode: String? = "E T 483",
        title: String? = "Mobile App Programming",
        creditHours: Int? = 3,
        prerequisite: String? = "E T 382",
        tags: List<String>? = listOf("Kotlin", "Compose"),
        instructor: InstructorDto? = InstructorDto(
            name = "Dr. Saleem Masadeh",
            title = "Instructor",
            email = "saleem@nmsu.edu",
            photoUrl = "https://example.com/photo.png"
        )
    ) = CourseDto(
        id = id,
        courseCode = courseCode,
        title = title,
        creditHours = creditHours,
        prerequisite = prerequisite,
        tags = tags,
        description = "desc",
        schedule = "MWF",
        location = "Room 1",
        instructor = instructor
    )

    @Test
    fun `maps every field including the nested instructor`() {
        val course = dto().toDomain()!!

        assertEquals("et483", course.id)
        assertEquals("E T 483", course.courseCode)
        assertEquals(3, course.credits)
        assertEquals("E T 382", course.prerequisite)
        assertEquals(listOf("Kotlin", "Compose"), course.tags)
        assertEquals("Dr. Saleem Masadeh", course.instructor)
        assertEquals("saleem@nmsu.edu", course.instructorEmail)
        assertEquals("https://example.com/photo.png", course.instructorImageUrl)
    }

    @Test
    fun `course without id code or title is skipped`() {
        assertNull(dto(id = null).toDomain())
        assertNull(dto(courseCode = " ").toDomain())
        assertNull(dto(title = "").toDomain())
    }

    @Test
    fun `missing optional fields get safe defaults`() {
        val course = dto(
            creditHours = null,
            prerequisite = "  ",
            tags = null,
            instructor = null
        ).toDomain()!!

        assertEquals(0, course.credits)
        assertNull("blank prerequisite should mean no prerequisite", course.prerequisite)
        assertTrue(course.tags.isEmpty())
        assertEquals("Staff", course.instructor)
        assertEquals("", course.instructorImageUrl)
    }

    @Test
    fun `the published courses json parses with Gson and maps to domain`() {
        // This is the same file the app downloads, so if someone breaks the JSON
        // or renames a key, this test fails before the app does.
        val file = listOf(File("../api/v1/courses.json"), File("api/v1/courses.json"))
            .first { it.exists() }
        val dtos = Gson().fromJson(file.readText(), Array<CourseDto>::class.java)
        val courses = dtos.mapNotNull { it.toDomain() }

        assertEquals("every course in the JSON should map", dtos.size, courses.size)
        assertEquals(11, courses.size)
        assertEquals(courses.size, courses.map { it.id }.toSet().size)

        val intro = courses.first { it.id == "et101" }
        assertNull(intro.prerequisite)
        assertEquals("Dr. Mark Lawrence", intro.instructor)
        assertTrue(intro.instructorImageUrl.startsWith("https://"))

        val micro = courses.first { it.id == "et362" }
        assertEquals(4, micro.credits)
        assertNotNull(micro.prerequisite)
    }
}
