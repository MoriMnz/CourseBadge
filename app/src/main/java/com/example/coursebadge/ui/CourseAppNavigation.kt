package com.example.coursebadge.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.coursebadge.viewmodel.CourseViewModel

// Requirement 3: navigation

// route names in one place so a typo in a string doesn't break navigation
object CourseRoutes {
    const val DASHBOARD = "dashboard"
    const val COURSE_ID_ARG = "courseId"
    const val DETAIL = "detail/{$COURSE_ID_ARG}"

    fun detail(courseId: String) = "detail/$courseId"
}

@Composable
fun CourseAppNavigation() {
    val navController = rememberNavController()

    // the ViewModel is created here, outside the NavHost, so it belongs to the
    // activity and both screens get the same instance. if each screen called
    // viewModel() inside its own composable() they would get separate ones,
    // and enrolling from the detail screen wouldn't show up on the dashboard
    val viewModel: CourseViewModel = viewModel()

    NavHost(navController = navController, startDestination = CourseRoutes.DASHBOARD) {

        composable(CourseRoutes.DASHBOARD) {
            DashboardScreen(
                viewModel = viewModel,
                onCourseClick = { courseId ->
                    // launchSingleTop so a double tap doesn't open the same
                    // detail screen twice
                    navController.navigate(CourseRoutes.detail(courseId)) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // only the id travels in the route, not the whole Course object, the
        // detail screen looks up the rest in the ViewModel
        composable(
            route = CourseRoutes.DETAIL,
            arguments = listOf(navArgument(CourseRoutes.COURSE_ID_ARG) { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString(CourseRoutes.COURSE_ID_ARG) ?: ""
            CourseDetailScreen(
                courseId = courseId,
                viewModel = viewModel,
                onBackClick = {
                    // the check is so tapping back twice really fast doesn't
                    // pop the dashboard too and leave a blank screen
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}
