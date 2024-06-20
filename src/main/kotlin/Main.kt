import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.core.supplier.EntitySupplier
import dev.kord.core.supplier.RestEntitySupplier
import dev.kord.rest.request.KtorRequestHandler
import dev.kord.rest.service.RestClient



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

    val chan = MyChannel(kord, Snowflake(1250138523957330026), sup)
    chan.createMessage("hade")

    val ser = Snowflake(1013799473983520859)

    val server = kord.getGuild(ser)
    // println(server)

    val c = Client()
    c.makeCommand()
}




