package com.utfpr.gestaofrotaapp.data.model

import com.google.gson.annotations.SerializedName

data class CarByIdResponse(
    @SerializedName("id") val id: String,
    @SerializedName("value") val value: Car
)
