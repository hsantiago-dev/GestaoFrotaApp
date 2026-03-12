package com.utfpr.gestaofrotaapp.data.firebase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID

class StorageDataSource(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {

    private val carsRef = storage.reference.child("cars")

    suspend fun uploadImageAndGetUrl(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val extension = uri.lastPathSegment?.substringAfterLast('.', "jpg") ?: "jpg"
            val fileName = "${UUID.randomUUID()}.$extension"
            val ref = carsRef.child(fileName)

            ref.putFile(uri).await()
            val downloadUrl = ref.getDownloadUrl().await()
            downloadUrl.toString()
        }
    }
}
