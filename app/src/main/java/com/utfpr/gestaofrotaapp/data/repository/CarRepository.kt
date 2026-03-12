package com.utfpr.gestaofrotaapp.data.repository

import android.net.Uri
import com.utfpr.gestaofrotaapp.data.firebase.StorageDataSource
import com.utfpr.gestaofrotaapp.data.model.Car
import com.utfpr.gestaofrotaapp.data.model.Place
import com.utfpr.gestaofrotaapp.data.remote.CarApiService
import com.utfpr.gestaofrotaapp.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.util.UUID

class CarRepository(
    private val api: CarApiService = RetrofitClient.createService(CarApiService::class.java),
    private val storageDataSource: StorageDataSource = StorageDataSource()
) {

    suspend fun getCars(): Result<List<Car>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getCars()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                throw ApiException(response.code(), response.message())
            }
        }.mapFailureToApiException()
    }

    suspend fun getCarById(id: String): Result<Car> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getCarById(id)
            if (response.isSuccessful) {
                response.body()?.value
                    ?: throw ApiException(response.code(), "Resposta inválida")
            } else {
                throw ApiException(response.code(), response.message())
            }
        }.mapFailureToApiException()
    }

    suspend fun createCar(car: Car): Result<Car> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.createCar(car)
            if (response.isSuccessful) {
                response.body() ?: throw ApiException(response.code(), "Resposta inválida")
            } else {
                val body = response.errorBody()?.string()
                throw ApiException(response.code(), body ?: response.message())
            }
        }.mapFailureToApiException()
    }

    suspend fun createCarWithImage(
        imageUri: Uri,
        year: String,
        name: String,
        licence: String,
        place: Place,
        id: String = UUID.randomUUID().toString()
    ): Result<Car> = withContext(Dispatchers.IO) {
        runCatching {
            val imageUrl = storageDataSource.uploadImageAndGetUrl(imageUri).getOrThrow()
            val car = Car(id = id, imageUrl = imageUrl, year = year, name = name, licence = licence, place = place)
            createCar(car).getOrThrow()
        }.mapFailureToApiException()
    }

    suspend fun updateCar(id: String, car: Car): Result<Car> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.updateCar(id, car)
            if (response.isSuccessful) {
                response.body() ?: throw ApiException(response.code(), "Resposta inválida")
            } else {
                throw ApiException(response.code(), response.message())
            }
        }.mapFailureToApiException()
    }

    suspend fun updateCarWithImage(
        id: String,
        imageUri: Uri,
        year: String,
        name: String,
        licence: String,
        place: Place
    ): Result<Car> = withContext(Dispatchers.IO) {
        runCatching {
            val imageUrl = storageDataSource.uploadImageAndGetUrl(imageUri).getOrThrow()
            val car = Car(id = id, imageUrl = imageUrl, year = year, name = name, licence = licence, place = place)
            updateCar(id, car).getOrThrow()
        }.mapFailureToApiException()
    }

    suspend fun deleteCar(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.deleteCar(id)
            if (response.isSuccessful) {
                Unit
            } else {
                throw ApiException(response.code(), response.message())
            }
        }.mapFailureToApiException()
    }

    private fun <T> Result<T>.mapFailureToApiException(): Result<T> = fold(
        onSuccess = { Result.success(it) },
        onFailure = { Result.failure(it.toApiException()) }
    )

    private fun Throwable.toApiException(): ApiException = when (this) {
        is ApiException -> this
        is HttpException -> ApiException(code(), message())
        is IOException -> ApiException(-1, message ?: "Erro de conexão")
        else -> ApiException(-1, message ?: "Erro desconhecido")
    }
}

class ApiException(
    val code: Int,
    override val message: String
) : Exception(message)
