package com.utfpr.gestaofrotaapp.data.model

import com.google.gson.annotations.SerializedName

data class Place(
    @SerializedName("lat") val lat: Double,
    @SerializedName("long") val long: Double
)
