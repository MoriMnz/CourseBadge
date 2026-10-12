Horacio Gonzalez

New Mexico State University

E T 483: Mobile App Programming

Prof. Masadeh

October 11th, 2026

State Hoisting Summary

For this assignment, we built the Course Tracker app on top of the CourseBadgeCard layout from Homework 1, and the main idea behind it was state hoisting. State hoisting basically means moving the state out of the composables that display it and up into a parent composable, so the child components become stateless and only show whatever they receive. In our app, that parent is CourseTrackerScreen, which is the only composable that has state.

Inside CourseTrackerScreen we defined four state variables using rememberSaveable together with mutableStateOf and the by delegate: searchQuery, a String with what the user types in the search field; overridePrereqs, a Boolean for the "Bypass Prerequisite Checks" switch; enrolledIds, a Set of Strings with the ids of the enrolled courses; and finally satisfiedPrereqIds, another Set of Strings with the courses where the prerequisite was marked as satisfied. We used rememberSaveable instead of remember because remember loses everything when the activity is recreated, for example when the phone is rotated, and rememberSaveable keeps the search text, the switch and the enrolled courses after an orientation change. This is also why we added an id to the Course data class, saving a set of ids is a lot simpler than saving the Course objects.

Everything else is calculated from those four values instead of being stored separately. The total credits in the header is the sum of the credits of the courses whose id is in enrolledIds, and the list shown is the courses that match searchQuery by title or course code (using matchesQuery in CourseData). When one of the states changes, Compose recomposes the screen and these values are calculated again, so the header and the list can't get out of sync.

Now, for the child components, EnrollmentHeader and InteractiveCourseCard don't have any state of their own. EnrollmentHeader receives totalCredits, searchQuery and overridePrereqs, and when the user types or flips the switch it calls onSearchQueryChange or onOverrideToggle, the parent is the one that actually updates the state. InteractiveCourseCard works the same way, it receives isEnrolled, isPrereqSatisfied and canEnroll, and sends events back up with onTogglePrereq and onToggleEnrollment. The canEnroll value is decided in the parent, a course can be enrolled only if it has no prerequisite, the prerequisite is satisfied, or the override is on; if not, the Enroll button is disabled and the card shows "Prerequisites missing".

Since the rules live in the parent, it was also easy to keep the state consistent. If the user turns the override off, any course that was only enrolled because of the override gets unenrolled, and if the user marks a prerequisite as missing again, that course is unenrolled too. Additionally, when updating the sets we always create a new set (adding or removing the id) instead of changing the existing one, because Compose only notices the change when the value itself is replaced.

This is what is known as Unidirectional Data Flow (UDF), state goes down from the parent to the children and events go up through callbacks. And finally, keeping the cards stateless made them easy to test in the previews, since we can just pass different values to see the enrolled and blocked versions of the card without running the whole app.
