package com.example.coursebadge.model

// course model. id was added in HW2, the app keeps track of enrolled courses
// and prerequisites with sets of ids instead of changing the Course objects,
// so Course stays immutable and the state is easy to save.
// for HW3 the last fields are new, the detail screen needs more information
// than the card, and instructorImageUrl is the picture Coil loads
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

object CourseData {

    // the pictures are the professors' real profile photos, loaded straight
    // from the NMSU and PLEX Lab websites. if the phone is offline or a link
    // stops working, the card shows the gray placeholder instead
    private const val MASADEH_PHOTO =
        "https://plexlab.net/wp-content/uploads/2019/07/saleem-cropped.png"
    private const val SASSENFELD_PHOTO =
        "https://et.nmsu.edu/assets/_portraits/RolfeProfilePIc.jpg"
    private const val BRAKER_PHOTO = "https://et.nmsu.edu/images/Braker.jpg"
    private const val LAWRENCE_PHOTO = "https://et.nmsu.edu/images/Mark-Lawrence.jpg"
    private const val PANWAR_PHOTO =
        "https://computerscience.nmsu.edu/_assets/images/gaurav_provile.png"
    private const val LOPEZ_MORALES_PHOTO =
        "https://computerscience.nmsu.edu/facultydirectory/_images/efren-lopez-morales-250-250.jpg"
    private const val COOPER_PHOTO =
        "https://computerscience.nmsu.edu/facultydirectory/_images/shaun-2019b-sq-250x250.jpg"

    val courses: List<Course> = listOf(
        Course(
            id = "et483",
            courseCode = "E T 483",
            title = "Mobile App Programming",
            credits = 3,
            instructor = "Dr. Saleem Masadeh",
            prerequisite = "E T 382 (Advanced Software Systems)",
            tags = listOf("Kotlin", "Compose", "Android"),
            description = "Design and development of native Android applications using " +
                    "Kotlin and Jetpack Compose, covering state management, MVVM " +
                    "architecture, navigation, and working with remote data.",
            schedule = "Tue / Thu  10:30 AM - 11:45 AM",
            location = "Goddard Hall 210",
            instructorTitle = "Instructor, Computer Science",
            instructorEmail = "saleem@nmsu.edu",
            instructorImageUrl = MASADEH_PHOTO
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
                    "interfacing, and assembly language programming on ARM based " +
                    "development boards.",
            schedule = "Mon / Wed  1:00 PM - 2:15 PM  +  Fri lab",
            location = "Engineering Complex III 104",
            instructorTitle = "MIT Program Director, Professor",
            instructorEmail = "rolfe@nmsu.edu",
            instructorImageUrl = SASSENFELD_PHOTO
        ),
        Course(
            id = "et101",
            courseCode = "E T 101",
            title = "Intro to Engineering Technology",
            credits = 3,
            instructor = "Dr. Mark Lawrence",
            // left null on purpose, this one can always be enrolled
            prerequisite = null,
            tags = listOf("Survey", "Freshman", "Lab"),
            description = "Overview of the engineering technology disciplines, technical " +
                    "problem solving, teamwork, and hands-on lab projects for first " +
                    "year students.",
            schedule = "Mon / Wed / Fri  9:00 AM - 9:50 AM",
            location = "Jett Hall 115",
            instructorTitle = "Associate College Professor",
            instructorEmail = "marklaw@nmsu.edu",
            instructorImageUrl = LAWRENCE_PHOTO
        ),
        Course(
            id = "et583",
            courseCode = "E T 583",
            // graduate section of E T 483, same class with a bigger project
            title = "Mobile App Programming (Graduate)",
            credits = 3,
            instructor = "Dr. Saleem Masadeh",
            prerequisite = "Graduate standing",
            tags = listOf("Kotlin", "Compose", "Research"),
            description = "Graduate section of E T 483. Same Kotlin and Jetpack Compose " +
                    "topics, plus reading current mobile research papers and building " +
                    "a semester long app project.",
            schedule = "Wed  6:00 PM - 8:30 PM",
            location = "Goddard Hall 230",
            instructorTitle = "Instructor, Computer Science",
            instructorEmail = "saleem@nmsu.edu",
            instructorImageUrl = MASADEH_PHOTO
        ),
        Course(
            id = "et555",
            courseCode = "E T 555",
            title = "Virtualization",
            credits = 3,
            instructor = "Prof. Philip Braker",
            prerequisite = "E T 350 (Networking Fundamentals)",
            tags = listOf("VMware", "Hypervisor", "Labs"),
            description = "Compute, storage, and network virtualization, Type 1 and " +
                    "Type 2 hypervisors, and techniques like snapshots, templates, " +
                    "and live migration.",
            schedule = "Online  (asynchronous)",
            location = "Canvas",
            instructorTitle = "ICT Program Coordinator, Associate Professor",
            instructorEmail = "pbraker@nmsu.edu",
            instructorImageUrl = BRAKER_PHOTO
        ),
        Course(
            id = "et420",
            courseCode = "E T 420",
            title = "Network Security",
            credits = 3,
            instructor = "Prof. Philip Braker",
            prerequisite = "E T 350 (Networking Fundamentals)",
            tags = listOf("Firewalls", "Crypto", "Linux"),
            description = "Securing networked systems, firewalls, VPNs, intrusion " +
                    "detection, and an introduction to applied cryptography.",
            schedule = "Tue / Thu  2:30 PM - 3:45 PM",
            location = "Engineering Complex III 210",
            instructorTitle = "ICT Program Coordinator, Associate Professor",
            instructorEmail = "pbraker@nmsu.edu",
            instructorImageUrl = BRAKER_PHOTO
        ),
        Course(
            id = "et310",
            courseCode = "E T 310",
            title = "Database Systems",
            credits = 3,
            instructor = "Dr. Mark Lawrence",
            prerequisite = null,
            tags = listOf("SQL", "Modeling", "Postgres"),
            description = "Relational database design, entity relationship modeling, " +
                    "normalization, and writing SQL queries against a real database.",
            schedule = "Mon / Wed  3:00 PM - 4:15 PM",
            location = "Jett Hall 202",
            instructorTitle = "Associate College Professor",
            instructorEmail = "marklaw@nmsu.edu",
            instructorImageUrl = LAWRENCE_PHOTO
        ),
        Course(
            id = "et470",
            courseCode = "E T 470",
            title = "Cloud Computing",
            credits = 3,
            instructor = "Dr. Rolfe Sassenfeld",
            prerequisite = "E T 555 (Virtualization)",
            tags = listOf("AWS", "Containers", "IaaS"),
            description = "Cloud service models, deploying applications on public cloud " +
                    "providers, containers, and resource pooling.",
            schedule = "Tue / Thu  5:30 PM - 6:45 PM",
            location = "Goddard Hall 115",
            instructorTitle = "MIT Program Director, Professor",
            instructorEmail = "rolfe@nmsu.edu",
            instructorImageUrl = SASSENFELD_PHOTO
        ),
        Course(
            id = "cs478",
            courseCode = "C S 478",
            title = "Applied Cryptography & Blockchain",
            credits = 3,
            instructor = "Dr. Gaurav Panwar",
            prerequisite = "E T 420 (Network Security)",
            tags = listOf("Crypto", "Blockchain", "Privacy"),
            description = "Symmetric and public key cryptography, hashing, digital " +
                    "signatures, and how blockchains and smart contracts use them to " +
                    "build systems that don't need a trusted middleman.",
            schedule = "Mon / Wed  10:30 AM - 11:45 AM",
            location = "Science Hall 118",
            instructorTitle = "Assistant Professor, Computer Science",
            instructorEmail = "gpanwar@nmsu.edu",
            instructorImageUrl = PANWAR_PHOTO
        ),
        Course(
            id = "cs479",
            courseCode = "C S 479",
            title = "Cyber-Physical Systems Security",
            credits = 3,
            instructor = "Dr. Efrén López-Morales",
            prerequisite = "E T 362 (Microcomputer Architecture)",
            tags = listOf("ICS", "Satellites", "Deception"),
            description = "Security of systems that connect software with the physical " +
                    "world, like industrial control systems, PLCs, and satellites, plus " +
                    "honeypots and cyber deception.",
            schedule = "Tue / Thu  1:00 PM - 2:15 PM",
            location = "Science Hall 107",
            instructorTitle = "Assistant Professor, Computer Science",
            instructorEmail = "elopezm@nmsu.edu",
            instructorImageUrl = LOPEZ_MORALES_PHOTO
        ),
        Course(
            id = "cs272",
            courseCode = "C S 272",
            title = "Data Structures",
            credits = 4,
            instructor = "Dr. Shaun Cooper",
            prerequisite = null,
            tags = listOf("C++", "Trees", "Algorithms"),
            description = "Lists, stacks, queues, trees, hash tables, and graphs, how " +
                    "they are implemented, and how to pick the right one by looking at " +
                    "the running time.",
            schedule = "Mon / Wed / Fri  11:30 AM - 12:20 PM",
            location = "Science Hall 118",
            instructorTitle = "College Associate Professor, Computer Science",
            instructorEmail = "scooper@nmsu.edu",
            instructorImageUrl = COOPER_PHOTO
        )
    )

    // used by the search box, checks both title and course code so "483" and
    // "mobile" find the same course. empty search shows everything.
    // spaces are ignored for the code, that way "ET483", "ET 483" and
    // "E T 483" all work, since the codes are written with a space in the middle
    fun matchesQuery(course: Course, query: String): Boolean {
        if (query.isBlank()) return true
        val trimmed = query.trim()
        val compactQuery = trimmed.replace(" ", "")
        val compactCode = course.courseCode.replace(" ", "")
        return course.title.contains(trimmed, ignoreCase = true) ||
                compactCode.contains(compactQuery, ignoreCase = true)
    }
}
