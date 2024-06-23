import bot.Bot
import bot.Client
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.thread.TextChannelThread
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.core.supplier.RestEntitySupplier
import dev.kord.rest.request.KtorRequestHandler
import dev.kord.rest.service.*
import interactions.ChatInputCommandInteraction
import interactions.MyChannel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch



suspend fun main(args: Array<String>) {
    // val token = args.firstOrNull() ?: error("token required")
    val token = botToken
    val kord = Kord(token)
    val bot = Bot(token, kord)

    bot.initialize()

    val interactions = ChatInputCommandInteraction(kord)

    // interactions.helloCommand("hei-command", "hei")
    // interactions.countCommand("count-command", "Kommer nå ... ")
    // interactions.testCommand("test-command", "test command lolsie")
    interactions.testCommand("test3", "this is test3")
    interactions.countCommand("count-command", "Kommer nå ... ")


    CoroutineScope(Dispatchers.Default).launch {
        kord.login()
    }



    /*TODO:
    *  ultimate leser gruppe id for now, get this id from a get call
    *  preffered setup with websocket when command is called
    *  */
    val ultimatServer = 1013799473983520859
    val canuteServer = 1250138523332382885

    val serverId = Snowflake(canuteServer)

    val guild = kord.getGuild(serverId)




    // val guildChannels = getGuildChannels(guild.id)
    val ktorRequestHandler = KtorRequestHandler(botToken)

    val guildService = GuildService(ktorRequestHandler)

    /*
    * 1250138523957330026 - #generelt
    * 1253461533695152129 - #gen2
    *  */

    println(guildService.getGuildChannels(guild.id))
}






