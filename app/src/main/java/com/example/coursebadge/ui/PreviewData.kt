package com.example.coursebadge.ui

import com.example.coursebadge.model.Course

// A few fixed courses for the @Preview functions only. The real app never uses
// this list, it gets the catalog from the network, but the Android Studio
// preview can't make network calls, so it needs something to draw.
internal val previewCourses: List<Course> = listOf(
    Course(
        id = "et483",
        courseCode = "E T 483",
        title = "Mobile App Programming",
        credits = 3,
        instructor = "Dr. Saleem Masadeh",
        prerequisite = "E T 382 (Advanced Software Systems)",
        tags = listOf("Kotlin", "Compose", "Android"),
        description = "Design and development of native Android applications using " +
                "Kotlin and Jetpack Compose.",
        schedule = "Tue / Thu  10:30 AM - 11:45 AM",
        location = "Goddard Hall 210",
        instructorTitle = "Instructor, Computer Science",
        instructorEmail = "saleem@nmsu.edu",
        instructorImageUrl = ""
    ),
    Course(
        id = "et362",
        courseCode = "E T 362",
        title = "Microcomputer Architecture",
        credits = 4,
        instructor = "Dr. Rolfe Sassenfeld",
        prerequisite = "E T 182 (Digital Logic & Circuits)",
        tags = listOf("Assembly", "Embedded", "ARM"),
        description = "Internal organization of microprocessors, memory and I/O " +
                "interfacing, and assembly language programming.",
        schedule = "Mon / Wed  1:00 PM - 2:15 PM  +  Fri lab",
        location = "Engineering Complex III 104",
        instructorTitle = "MIT Program Director, Professor",
        instructorEmail = "rolfe@nmsu.edu",
        instructorImageUrl = ""
    ),
    Course(
        id = "et101",
        courseCode = "E T 101",
        title = "Intro to Engineering Technology",
        credits = 3,
        instructor = "Dr. Mark Lawrence",
        prerequisite = null,
        tags = listOf("Survey", "Freshman", "Lab"),
        description = "Overview of the engineering technology disciplines and " +
                "hands-on lab projects for first year students.",
        schedule = "Mon / Wed / Fri  9:00 AM - 9:50 AM",
        location = "Jett Hall 115",
        instructorTitle = "Associate College Professor",
        instructorEmail = "marklaw@nmsu.edu",
        instructorImageUrl = ""
    )
)
