import dev.kord.common.entity.DiscordShard
import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.common.ratelimit.IntervalRateLimiter
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.Message
import dev.kord.core.entity.application.GlobalApplicationCommand
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.gateway.start
import dev.kord.core.on
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.RestEntitySupplier
import dev.kord.gateway.*
import dev.kord.gateway.retry.LinearRetry
import dev.kord.rest.request.KtorRequestHandler
import dev.kord.rest.service.RestClient
import dev.kord.rest.service.*
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds


class MyChannel(
    override val kord: Kord,
    override val id: Snowflake,
    override val supplier: EntitySupplier
) : MessageChannelBehavior {

    override suspend fun createMessage(content: String): Message {
        return super.createMessage(content)
    }
}


suspend fun main(args: Array<String>) {
    // val token = args.firstOrNull() ?: error("token required")
    val token = botToken
    val rest = RestClient(KtorRequestHandler(token))
    val username = rest.user.getCurrentUser().username

    println(rest.user.getCurrentUser().id)
    println("using $username's token")

    val kord = Kord(token)


    kord.on<ChatInputCommandInteractionCreateEvent> {
       if(interaction.command.rootName == "hei-command") {
           interaction.deferEphemeralResponse().respond {
               content = "hei"
           }
       }
    }

    CoroutineScope(Dispatchers.Default).launch {
        kord.login()
    }




    val sup = RestEntitySupplier(kord)

    // Canute's server channel id
    val chan = MyChannel(kord, Snowflake(1250138523957330026), sup)
    chan.createMessage("hade")

    /*TODO:
    *  ultimate leser gruppe id for now, get this id from a get call
    *  preffered setup with websocket when command is called
    *  */
    val ultimatServer = 1013799473983520859
    val canuteServer = 1250138523332382885

    val serverId = Snowflake(canuteServer)

    val guild = kord.getGuild(serverId)

    // println(server)

    val c = Client()
    c.makeCommand()

    // val guildChannels = getGuildChannels(guild.id)
    val ktorRequestHandler = KtorRequestHandler(botToken)

    val guildService = GuildService(ktorRequestHandler)

    /*
    * 1250138523957330026 - #generelt
    * 1253461533695152129 - #gen2
    *  */

    println(guildService.getGuildChannels(guild.id))
}





