package com.matatkoj.nbaplayers.data.response

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

@Immutable
data class Team(
    val id: Int,
    val name: String,
    @SerializedName("full_name") val fullName: String,
    val abbreviation: String,
    val city: String,
    val conference: String,
    val division: String
)