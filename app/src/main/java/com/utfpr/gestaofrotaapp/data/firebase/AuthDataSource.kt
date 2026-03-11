package com.utfpr.gestaofrotaapp.data.firebase

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * Fonte de dados para Firebase Authentication.
 * Login por telefone (SMS) e logout.
 */
class AuthDataSource(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val currentUser get() = auth.currentUser

    /**
     * Envia o código SMS para o número informado.
     * [activity] é necessário para o fluxo de reCAPTCHA quando aplicável.
     * Retorna [Result] com [VerificationResult] (verificationId) em sucesso.
     */
    fun sendPhoneVerificationCode(
        activity: Activity,
        phoneNumber: String,
        callback: (Result<VerificationResult>) -> Unit
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    callback(Result.success(VerificationResult(credential = credential)))
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    callback(Result.failure(e))
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    callback(Result.success(VerificationResult(verificationId = verificationId)))
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Conclui o login com o código SMS informado.
     * Usar [verificationId] retornado em [sendPhoneVerificationCode] ou credencial de [onVerificationCompleted].
     */
    suspend fun signInWithPhoneCredential(
        verificationId: String,
        code: String
    ): Result<Unit> = runCatching {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        auth.signInWithCredential(credential).await()
        Unit
    }

    /**
     * Login direto quando Firebase retorna credencial em [onVerificationCompleted] (ex.: teste em emulador).
     */
    suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<Unit> = runCatching {
        auth.signInWithCredential(credential).await()
        Unit
    }

    /**
     * Encerra a sessão do usuário atual.
     */
    fun logout() {
        auth.signOut()
    }

    /**
     * Fluxo do usuário atual (para observar mudanças de auth na UI).
     */
    fun authStateFlow(): Flow<com.google.firebase.auth.FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
}

/**
 * Resultado do envio do código: ou [verificationId] para o usuário digitar o código, ou [credential] quando já autenticado (ex.: teste).
 */
data class VerificationResult(
    val verificationId: String? = null,
    val credential: PhoneAuthCredential? = null
) {
    val needsCode: Boolean get() = !verificationId.isNullOrBlank()
    val hasCredential: Boolean get() = credential != null
}
