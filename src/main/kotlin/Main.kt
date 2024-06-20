import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.RestEntitySupplier
import dev.kord.rest.request.KtorRequestHandler
import dev.kord.rest.service.RestClient
import dev.kord.rest.service.*



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
    // c.makeCommand()

    // val guildChannels = getGuildChannels(guild.id)
    val ktorRequestHandler = KtorRequestHandler(botToken)

    val guildService = GuildService(ktorRequestHandler)

    /*
    * 1250138523957330026 - #generelt
    * 1253461533695152129 - #gen2
    *  */

    println(guildService.getGuildChannels(guild.id))


}





