package com.utfpr.gestaofrotaapp.data.remote

import com.utfpr.gestaofrotaapp.data.model.Car
import com.utfpr.gestaofrotaapp.data.model.CarByIdResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Interface Retrofit para a API REST de carros.
 * Base URL deve terminar com / (ex: http://10.0.2.2:3000/).
 */
interface CarApiService {

    @GET("car")
    suspend fun getCars(): Response<List<Car>>

    @GET("car/{id}")
    suspend fun getCarById(@Path("id") id: String): Response<CarByIdResponse>

    @POST("car")
    suspend fun createCar(@Body car: Car): Response<Car>

    @PATCH("car/{id}")
    suspend fun updateCar(@Path("id") id: String, @Body car: Car): Response<Car>

    @DELETE("car/{id}")
    suspend fun deleteCar(@Path("id") id: String): Response<DeleteMessageResponse>
}

/**
 * Resposta do DELETE /car/:id — { "message": "..." }.
 */
data class DeleteMessageResponse(
    val message: String? = null
)
