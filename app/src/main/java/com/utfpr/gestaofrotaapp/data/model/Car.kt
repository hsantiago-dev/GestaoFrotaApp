package com.utfpr.gestaofrotaapp.data.model

import com.google.gson.annotations.SerializedName

data class Car(
    @SerializedName("id") val id: String? = null,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("year") val year: String,
    @SerializedName("name") val name: String,
    @SerializedName("licence") val licence: String,
    @SerializedName("place") val place: Place
)
