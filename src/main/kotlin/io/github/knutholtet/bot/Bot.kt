package io.github.knutholtet.bot

import dev.kord.rest.request.KtorRequestHandler
import dev.kord.rest.service.RestClient
import dev.kord.core.Kord

class Bot(
    private val token: String,
    private val kord: Kord
){
    private val rest = RestClient(KtorRequestHandler(token))

    suspend fun initialize() {
        val username = rest.user.getCurrentUser().username

        println(rest.user.getCurrentUser().id)
        println("using $username's token")
        println("using ${rest.user.getCurrentUser().id.javaClass.name}'s id")

        val client = Client(rest.user.getCurrentUser().id)
        setupCommands(client)
    }

    /* I think the issue lies in this function, not sure why */
    private suspend fun setupCommands(client: Client) {
        // Creating Global commands - NOTE: these takes time to load into effect on Discord
        client.makeSlashCommandGlobal("count-command", "Counts the messages in current channel")

        // Creating Server specific commands - NOTE: these are quick to effect and can be used for testing
        client.makeSlashCommandGuild("embeded", "test command for embeded", "1013799473983520859")
        client.makeSlashCommandGuild("countall", "test command for all channels", "1013799473983520859")
    }
}