package bot

import dev.kord.rest.request.KtorRequestHandler
import dev.kord.rest.service.RestClient
import bot.Client
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.thread.TextChannelThread
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.core.supplier.RestEntitySupplier
import dev.kord.rest.service.*
import interactions.MyChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class Bot(
    private val token: String
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

    private suspend fun setupCommands(client: Client) {
        // Creating Global commands - NOTE: these takes time to load into effect on Discord
        client.makeSlashCommandGlobal("count-command", "Counts the messages in current channel")

        // Creating Server specific commands - NOTE: these are quick to effect and can be used for testing
        client.makeSlashCommandGlobal("test", "test command")

    }
}