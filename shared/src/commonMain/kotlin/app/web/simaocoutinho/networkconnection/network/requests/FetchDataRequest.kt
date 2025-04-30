package app.web.simaocoutinho.networkconnection.network.requests

import app.web.simaocoutinho.networkconnection.network.ExecutableRequest
import app.web.simaocoutinho.networkconnection.util.generateUUID
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlin.Result

class FetchDataRequest(
    private val httpClient: HttpClient // Pass Ktor client
    // Add any parameters needed, e.g., url path, query params
) : ExecutableRequest<List<String>> { // Example: fetching a list

    override val id: String = generateUUID()

    override suspend fun execute(): Result<List<String>> {
        return try {
            // Example Ktor GET call
            val data = httpClient.get("https://jsonplaceholder.typicode.com/posts") {
                // Configure request (headers, etc.) if needed
            }.body<List<String>>() // Ktor deserializes using configured plugin (e.g., kotlinx.serialization)
            Result.success(data)
        } catch (e: Exception) {
            println("FetchDataRequest Error: ${e.message}") // Log specific error
            Result.failure(e) // Handles Ktor exceptions
        }
    }
}