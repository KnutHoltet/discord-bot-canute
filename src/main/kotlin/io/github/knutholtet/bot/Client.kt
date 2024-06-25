package io.github.knutholtet.bot

import io.github.knutholtet.botToken
import dev.kord.common.entity.Snowflake
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.github.knutholtet.model.ChatCommand


class Client(
    private val botApplicationId: Snowflake
) {
    private val api = "https://discord.com/api/v10/applications/"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }

        defaultRequest {
            header(HttpHeaders.Authorization, "Bot $botToken")
        }
    }


    /* Se https://discord.com/developers/docs/interactions/application-commands#slash-commands */
    suspend fun makeSlashCommandGlobal(name: String, description: String) {


        // val url = "https://discord.com/api/v10/applications/1250137286260297822/commands"
        val url = api + "/${botApplicationId.value}/commands"

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

        println("**** MAKE SLASH COMMAND GLOBALLY ****")
        println("Response status: ${response.status.value}")
        println("Response: ${response.bodyAsText()}")
    }

    suspend fun makeSlashCommandGuild(name: String, description: String, guildId: String) {
        val url = api + "/${botApplicationId.value}/guilds/$guildId/commands"

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

        println("**** MAKE SLASH COMMAND FOR SERVER ****")
        println("Response status: ${response.status.value}")
        println("Response: ${response.bodyAsText()}")
    }
}