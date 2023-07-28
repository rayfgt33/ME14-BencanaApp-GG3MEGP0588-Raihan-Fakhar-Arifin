package com.r2.disastertracker.api

import com.r2.disastertracker.data.DisasterResponses
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("reports")
    fun getReport(@Query("timeperiod") timePeriod: String) : Call<DisasterResponses>
    @GET("reports/archive")
    fun getArchive(@Query("start") start: String , @Query("end") end: String) : Call<DisasterResponses>
}