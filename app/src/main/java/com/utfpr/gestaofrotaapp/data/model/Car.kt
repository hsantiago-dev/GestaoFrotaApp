package com.utfpr.gestaofrotaapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de carro da API REST.
 * Schema da API: id, imageUrl, year (string "2020/2021"), name, licence, place.
 * POST/PATCH exigem todos os campos; GET /car retorna array de Car; GET /car/:id retorna [CarByIdResponse].
 */
data class Car(
    @SerializedName("id") val id: String? = null,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("year") val year: String,
    @SerializedName("name") val name: String,
    @SerializedName("licence") val licence: String,
    @SerializedName("place") val place: Place
)
