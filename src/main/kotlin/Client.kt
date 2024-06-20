import Headers
import dev.kord.rest.request.KtorRequestHandler
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import java.net.http.HttpResponse
import java.time.Clock
import kotlinx.datetime.*
import io.ktor.client.plugins.websocket.*


class Client {
    private val client = HttpClient(CIO) {
        // install(WebSockets)

        install(ContentNegotiation) {
            gson()
        }

        defaultRequest {
            header(HttpHeaders.Authorization, "Bot $botToken")
        }
    }


    // r = requests.post(url, headers=headers, json=json)
    suspend fun makeCommand() {
        // id : 1250137286260297822

        val url = "https://discord.com/api/v10/applications/1250137286260297822/commands"

        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(
                ChatCommand(
                    "hade-command",
                    1,
                    "kommando for å si hade"
                )
            )
        }
        println("Response status: ${response.status.value}")
        println("Response: ${response.bodyAsText()}")

    }


}

data class ChatCommand(
    val name: String,
    val type: Int,
    val description: String,
)

/* Optional extension for now */
data class Options(
   val noe: String
)

data class Headers(
    val token: String
)