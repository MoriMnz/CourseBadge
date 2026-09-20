package com.example.coursebadge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import com.example.coursebadge.ui.theme.CourseBadgeTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.coursebadge.ui.CourseListScreen
import com.example.coursebadge.ui.theme.CourseBadgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Swap MaterialTheme for your generated CourseBadgeTheme if you
            // kept the ui/theme package from the Empty Compose Activity template.
            CourseBadgeTheme() {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CourseListScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
