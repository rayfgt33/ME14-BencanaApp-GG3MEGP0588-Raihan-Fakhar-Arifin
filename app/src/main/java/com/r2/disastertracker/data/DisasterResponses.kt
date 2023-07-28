package com.r2.disastertracker.data

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class DisasterResponses(
    @SerializedName("statusCode")
    val statusCode: Int=0,
    @SerializedName("result")
    val result: ResultDisaster = ResultDisaster()
): Serializable

data class ResultDisaster(
    @SerializedName("type")
    val type: String = "",
    @SerializedName("objects")
    val objects : Objects = Objects()
): Serializable

data class Objects(
    @SerializedName("output")
    val output: Output = Output()
): Serializable

data class Output(
    @SerializedName("type")
    val type: String = "",
    @SerializedName("geometries")
    val geometries : ArrayList<Geometry>  = ArrayList()
): Serializable

data class Geometry(
    @SerializedName("type")
    val type: String="",
    @SerializedName("properties")
    val properties: Properties = Properties() ,
    @SerializedName("coordinates")
    val coordinates : List<Any>
): Serializable

data class Properties(
    @SerializedName("pkey")
    val pkey: String = "",
    @SerializedName("created_at")
    val created: String = "",
    @SerializedName("source")
    val source: String = "",
    @SerializedName("status")
    val status: String = "",
    @SerializedName("url")
    val url: String = "",
    @SerializedName("image_url")
    val imgUrl: String = "",
    @SerializedName("disaster_type")
    val disasterType: String = "",
    @SerializedName("tags")
    val tags : Tags = Tags()
): Serializable

data class Tags(
    @SerializedName("instance_region_code")
    val region : String? = ""
): Serializable
