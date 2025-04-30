package app.web.simaocoutinho.networkconnection.network

import kotlin.Result // Use Kotlin's Result

interface ExecutableRequest<T> {
    val id: String // For potential tracking/cancellation
    suspend fun execute(): Result<T>
}