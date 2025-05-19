package com.example.safetrack

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.GeoPoint
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class FirebaseManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun signUp(nome: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: throw Exception("Usuário nulo")

            // Salva no Firestore
            val userData = hashMapOf(
                "nome" to nome,
                "email" to email,
                "createdAt" to System.currentTimeMillis()
            )

            firestore.collection("usuarios")
                .document(user.uid)
                .set(userData)
                .await()

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    fun saveUserLocationDetailed(userId: String, latitude: Double, longitude: Double, timestamp: String) {
        val locationData = hashMapOf(
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to timestamp
        )

        firestore.collection("usuarios")
            .document(userId)
            .collection("localizacoes")
            .add(locationData)
            .addOnSuccessListener {
                // Sucesso opcional
            }
            .addOnFailureListener {
                // Erro opcional
            }
    }

    suspend fun registrarIncidente(
        userId: String,
        categoria: String,
        descricao: String,
        imagem: String?,
        local: String,
        latitude: Double,
        longitude: Double,
        setor: String,
        status: String = "" // padrão vazio
    ): Result<Void?> {
        return try {
            val incidenteData = hashMapOf(
                "categoria" to categoria,
                "descricao" to descricao,
                "imagem" to (imagem ?: ""), // evita null no Firestore
                "local" to local,
                "localizacao" to GeoPoint(latitude, longitude),
                "setor" to setor,
                "status" to status,
                "timestamp" to System.currentTimeMillis()
            )

            firestore
                .collection("usuarios")
                .document(userId)
                .collection("incidentes")
                .add(incidenteData)
                .await()

            Result.success(null)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadImagem(uri: Uri): Result<String> {
        return try {
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val ref = storage.reference.child("imagens_incidentes/$fileName")
            ref.putFile(uri).await()
            val url = ref.downloadUrl.await().toString()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
