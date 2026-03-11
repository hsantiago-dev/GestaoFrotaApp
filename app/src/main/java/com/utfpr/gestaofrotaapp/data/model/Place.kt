package com.utfpr.gestaofrotaapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Localização geográfica (lat/long) usada no modelo [Car].
 * Compatível com o campo `place` da API REST.
 */
data class Place(
    @SerializedName("lat") val lat: Double,
    @SerializedName("long") val long: Double
)
