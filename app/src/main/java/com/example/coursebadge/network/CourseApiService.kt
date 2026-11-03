package com.example.coursebadge.network

import retrofit2.http.GET

// Requirement 1: Retrofit API interface

// Retrofit generates the implementation of this interface at runtime. The path
// is relative to ApiClient.BASE_URL, so the full request is
// GET {BASE_URL}api/v1/courses.json
//
// It is a suspend function, which means Retrofit runs the call without blocking
// the thread that called it, and it returns the parsed list directly. If the
// request fails (no internet, a timeout, HTTP 404/500 or bad JSON) it throws an
// exception, and the ViewModel is the one that catches it.
interface CourseApiService {
    @GET("api/v1/courses.json")
    suspend fun fetchCourses(): List<CourseDto>
}
