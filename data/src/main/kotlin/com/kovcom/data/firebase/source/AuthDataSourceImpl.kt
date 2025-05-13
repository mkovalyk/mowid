package com.kovcom.data.firebase.source

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.kovcom.data.model.Result
import com.kovcom.data.model.UserModelBase
import com.kovcom.data.preferences.LocalDataSource
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID
import kotlin.coroutines.CoroutineContext

class AuthDataSourceImpl constructor(
    private val dbInstance: FirebaseFirestore,
    private val authInstance: FirebaseAuth,
    private val localDataSource: LocalDataSource,
) : AuthDataSource, CoroutineScope {

    override val userFlow: Flow<Result<UserModelBase>> = callbackFlow {
        val subscription = dbInstance
            .collection(COLLECTION_USER)
            .document(authInstance.currentUser?.uid ?: "_")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    trySend(Result.Error(error))
                    return@addSnapshotListener
                }
                val user = value?.toObject<UserModelBase.UserModel>()
                if (user == null) {
                    trySend(Result.Success(UserModelBase.Empty))
                } else {
                    trySend(Result.Success(user))
                }
            }

        awaitClose {
            subscription.remove()
            cancel()
        }
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + SupervisorJob()

    override fun signInSuccess() {
        launch {
            saveUser()
        }
    }

    override fun signOutSuccess() {
        localDataSource.setToken(UUID.randomUUID().toString())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun saveUser(): String {
        val token = getTokenFromUser() ?: ""
        return suspendCancellableCoroutine { continuation ->
            authInstance.currentUser?.let {
                if (token.isEmpty()) {
                    dbInstance.collection(COLLECTION_USER)
                        .document(it.uid)
                        .set(
                            UserModelBase.UserModel(
                                token = UUID.randomUUID().toString(),
                                fullName = it.displayName,
                                email = it.email,
                                id = it.uid,
                            )
                        )
                        .addOnCompleteListener { continuation.resume(token, {}) }
                } else {
                    localDataSource.setToken(token)
                    continuation.resume(token, {})
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun getTokenFromUser(): String? =
        suspendCancellableCoroutine { continuation ->
            dbInstance.collection(COLLECTION_USER)
                .document(authInstance.currentUser?.uid ?: "_")
                .get()
                .addOnCompleteListener { snapShot ->
                    continuation.resume(snapShot.result.data?.get(TOKEN_FIELD) as? String, {})
                }
                .addOnFailureListener {
                    continuation.resume(null, {})
                }
        }

//    private suspend fun saveCurrentToken(): String {
//        var token: String = getTokenFromUser() ?: ""
//        if (token.isEmpty()) {
//            if (localDataSource.token.isEmpty()) {
//                localDataSource.setToken(UUID.randomUUID().toString())
//            }
//            token = localDataSource.token
//        } else {
//            localDataSource.setToken(token)
//        }
//        return token
//    }

    companion object {

        private const val COLLECTION_USER = "users"
        private const val TOKEN_FIELD = "token"

    }
}
