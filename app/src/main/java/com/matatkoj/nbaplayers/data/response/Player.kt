package com.matatkoj.nbaplayers.data.response

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

@Immutable
data class Player(
    val id: Int,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val position: String,
    val team: Team,
    @SerializedName("height_feet") val heightFeet: Int?,
    @SerializedName("height_inches") val heightInches: Int?,
    @SerializedName("weight_pounds") val weightPounds: Int?
)