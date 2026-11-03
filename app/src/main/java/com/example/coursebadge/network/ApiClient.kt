package com.example.coursebadge.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Requirement 1: singleton Retrofit client

// Using `object` makes this a singleton, there is only one ApiClient in the
// whole app. This matters because Retrofit and OkHttp are expensive to build
// (thread pools, connection pool, cache), so they should be created once and
// then reused.
object ApiClient {

    // The course catalog is the api/v1/courses.json file in our GitHub repo,
    // which is served over HTTPS by GitHub's raw file server. It basically works
    // like a small read-only REST endpoint, a GET returns the JSON list of courses.
    const val BASE_URL = "https://raw.githubusercontent.com/MoriMnz/CourseBadge/master/"

    // OkHttp's default is 10 seconds per step with no limit for the whole call.
    // We add a 15 s cap for the whole request, so a stalled connection ends up as
    // a timeout error that the user can retry, instead of a spinner that never ends.
    private const val CONNECT_TIMEOUT_SECONDS = 10L
    private const val READ_TIMEOUT_SECONDS = 10L
    private const val CALL_TIMEOUT_SECONDS = 15L

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    // `by lazy` builds it the first time something uses it (not at app start),
    // and it is thread safe, so two coroutines can't create two instances.
    val instance: CourseApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            // Gson turns the JSON response into CourseDto objects
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CourseApiService::class.java)
    }
}
