import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*


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
    suspend fun makeSlashCommandGlobal(name: String, description: String) {
        // id : 1250137286260297822

        // denne er satt til ultimat lesergruppe design id så den lager kun kommandoer for den serveren
        // se https://discord.com/developers/docs/interactions/application-commands#slash-commands

        val url = "https://discord.com/api/v10/applications/1250137286260297822/commands"

        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(
                ChatCommand(
                    name,
                    1,
                    description
                )
            )
        }
        println("Response status: ${response.status.value}")
        println("Response: ${response.bodyAsText()}")

    }

    suspend fun makeSlashCommandGuild(name: String, description: String, guildId: String) {
        val url = "https://discord.com/api/v10/applications/1250137286260297822/guilds/$guildId/commands"

        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(
                ChatCommand(
                    name,
                    1,
                    description
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