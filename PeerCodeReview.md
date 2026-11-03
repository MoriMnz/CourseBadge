Mauricio Munoz & Horacio Gonzalez

# Assignment 4: Peer Code Review Checklist

**Project:** CourseBadge (NMSU Course Catalog & Tracker) | **Review type:** structured self-audit | **Scope:** the HW4 networking changes (`network/`, `data/`, `viewmodel/` and `ui/DashboardScreen.kt`)

| # | Check | Result | Evidence |
|---|-------|--------|----------|
| 1 | All network calls run off the Main thread (`Dispatchers.IO`) | Pass | In `CourseViewModel.kt:117` the call to `repository.fetchCourses()` is wrapped in `withContext(ioDispatcher)`, which is `Dispatchers.IO` by default (line 83). Additionally, the unit test "network call runs on the IO dispatcher, not main" checks it. |
| 2 | Coroutines are lifecycle-scoped (no leaks) | Pass | It uses `viewModelScope.launch` (line 110), so the request gets cancelled when the ViewModel is cleared, and the `CancellationException` is rethrown (line 131). |
| 3 | Errors handled with try/catch/finally | Pass | `CourseViewModel.kt:113–143`. The `finally` block always turns `isLoading` off, and `toUserMessage()` turns timeout, offline, HTTP and JSON errors into messages that a student can actually understand. |
| 4 | No business logic inside composables | Pass | The enrollment rules, the filtering, the credit totals and even which loading/error UI to show (`showFullScreenLoading`, `showLoadingOverlay`, `showFullScreenError`, `showErrorSnackbar`) are all calculated in `CourseUiState`, the composables just read booleans and call lambdas. |
| 5 | Proper item keys in `LazyColumn` | Pass | `DashboardScreen.kt:264` uses `items(..., key = { it.id })`, and the header and empty state items have fixed keys (lines 244, 256). The course ids are also unique, which is checked by the JSON unit test. |
| 6 | No unintentional recompositions | Pass | According to the Compose compiler report, every composable in the dashboard (`DashboardContent`, `CourseList`, `InteractiveCourseCard`, `EnrollmentHeader`, etc.) is restartable and skippable. The cards receive plain values and lambdas instead of the whole state, and since each `copy()` of the state keeps the same `Course` objects, typing in the search or toggling an enrollment only recomposes the cards whose values changed. The state is collected once per screen (`DashboardScreen.kt:81`), and the callbacks are stable method references. |
| 7 | DTOs separated from the domain model | Pass | `CourseDto`/`InstructorDto` use `@SerializedName` for the snake_case keys, and `toDomain()` checks the required fields and gives safe defaults to the rest. The UI never sees a DTO. |
| 8 | Retrofit client is a singleton | Pass | `object ApiClient` with `by lazy` (which is thread safe), and the OkHttp connect/read/call timeouts are set explicitly. |
| 9 | Loading / error / retry UI | Pass | There is a centered `CircularProgressIndicator` whenever `isLoading` is true, full screen on the first load and on top of the list during a refresh (unit tested). There is also a full screen error with a **Retry** button, a Snackbar with a Retry action when a refresh fails, pull to refresh and a refresh icon. |
| 10 | Previous features preserved / state hoisting | Pass | Search, the prerequisite override, the credit tally, the summary badge and the detail navigation all still work, and the enrollments are kept after a refresh (unit tested). The child composables are stateless, the only local UI state is the dialog flag and the `SnackbarHostState`. |

**Findings and Follow-ups**

During the review we found and fixed a few things. The first one was the spinner position, a pull to refresh used to show its spinner at the top of the list instead of the center, now the pull indicator only follows the finger and `showLoadingOverlay` draws the centered spinner for every load. The second one was duplicate requests: tapping Retry twice could start two fetches at the same time, so now `loadCourses()` returns early while `loadJob` is still active (unit tested). And the third one was stale enrollments, if a course is removed from the server, its id is now also removed from the enrolled and satisfied sets, so the badge and the credits stay correct.

There are also some things that are still open. The `CourseRepository.DEMO_LATENCY_MS` adds 1.2 seconds so the spinner can be seen in the screenshots, and it should be set to 0 for a real release. Additionally, it would be better to use `collectAsStateWithLifecycle()` so the collection stops while the app is in the background, and since the enrollment is only kept in memory, it gets lost if Android kills the process, so saving it with DataStore or Room would be the next step.

**Summary**

This assignment added a clean data path to the app, starting from the Retrofit service, then `CourseRepository` (where the DTO to domain mapping happens), then `CourseViewModel` (with the coroutine on `Dispatchers.IO`), then the `StateFlow<CourseUiState>`, and finally Compose. Every network case (success, offline, timeout, HTTP error, retry, refresh and refresh failure) is covered by 14 JVM unit tests that run against a fake API service. Nothing blocking runs on the main thread, the composables don't have business logic and they skip when their data didn't change, and the list keeps stable keys, so the three required audit items pass.
