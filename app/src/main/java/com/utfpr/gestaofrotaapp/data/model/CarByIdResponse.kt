package com.utfpr.gestaofrotaapp.data.model

import com.google.gson.annotations.SerializedName

/**
 * Resposta do endpoint GET /car/:id.
 * A API retorna { "id": "...", "value": { ...car } }, não o carro direto.
 */
data class CarByIdResponse(
    @SerializedName("id") val id: String,
    @SerializedName("value") val value: Car
)
