package interactions

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createEmbed
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.thread.TextChannelThread
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.core.supplier.RestEntitySupplier
import dev.kord.rest.builder.message.EmbedBuilder
import interactions.cache.CountedChannelsCache
import kotlinx.coroutines.flow.count

class ChatInputCommandInteraction(
    private val kord: Kord
){


    private val restEntitySupplier = RestEntitySupplier(kord)
    private val countedChannelsCache = CountedChannelsCache

    /* COUNTING COMMANDS */
    fun countCommand(commandName: String, interactionContent: String) {

        kord.on<ChatInputCommandInteractionCreateEvent> {
            if(interaction.command.rootName == commandName) {
                interaction.deferEphemeralResponse().respond {
                    content = interactionContent
                }
            }

            val chanId = interaction.getChannel().id
            val channelForText = MyChannel(kord, chanId, restEntitySupplier)

            if(chanId !in countedChannelsCache.countedChannelMessages) {

                /* Caching... */
                val antMsg = countMessages(interaction.getChannel().id, kord)
                countedChannelsCache.countedChannelMessages[chanId] = antMsg // adds messages

                val channel = kord.getChannel(chanId)
                val lastMsg = channel?.data?.lastMessageId!!.value!!
                countedChannelsCache.countedChannelLastIdOnCall[chanId] = lastMsg


                channelForText.createMessage("Antall meldinger i kanalen: ${countedChannelsCache.countedChannelMessages[chanId]}")

            } else {

                val lastMsg = countedChannelsCache.countedChannelLastIdOnCall[chanId]!!
                val antMsg = countMessagesAfter(chanId, kord, lastMsg)

                val channel = kord.getChannel(chanId)
                val newLastMessage = channel?.data?.lastMessageId!!.value!!
                countedChannelsCache.countedChannelLastIdOnCall[chanId] = newLastMessage

                countedChannelsCache.countedChannelMessages[chanId] = countedChannelsCache.countedChannelMessages[chanId]!! + antMsg
                channelForText.createMessage("Antall meldinger i kanalen: ${countedChannelsCache.countedChannelMessages[chanId]}")
            }

        }
    }

    fun countAllChannels() {
        /* TODO: Add caching */
    }

    /* GREETINGS COMMAND */
    fun helloCommand(commandName: String, interactionContent: String) {

        kord.on<ChatInputCommandInteractionCreateEvent> {
            if(interaction.command.rootName == commandName) {
                interaction.deferEphemeralResponse().respond {
                    content = interactionContent
                }
            }
        }
    }

    /* QUOTE COMMANDS */
    fun quoteLastPerson() {}

    /* ANIME COMMAND */
    fun getRandomAnimeGif() {}

    /* MEME COMMAND */
    fun getRandomMemeGif() {}

    /* TEST COMMAND */
    fun testCommand(commandName: String, interactionContent: String) {
        kord.on<ChatInputCommandInteractionCreateEvent> {
            if(interaction.command.rootName == commandName) {
                interaction.deferEphemeralResponse().respond {
                    content = interactionContent
                }
            }
            /*
            val chanId = interaction.getChannel().id
            val channelForEmbed = MyChannel(kord, chanId, restEntitySupplier)


            val embedBuilder = EmbedBuilder()
            embedBuilder.image = "https://media1.giphy.com/media/v1.Y2lkPTc5MGI3NjExeXgybGQyNjFkMzMybjBmY2FwazBsaDc0MjZ5ZDQyYzNqOGlmeXA3diZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/l3fQf1OEAq0iri9RC/giphy.webp"
            channelForEmbed.createEmbed { embedBuilder.field("hei") }


            println("Test command run-ed")
            // channelForEmbed.createEmbed()
             */
            println("TEST COMMANDO FUNKER ENDELIG!")

        }
    }


    private suspend fun countMessages(channelId: Snowflake, kord: Kord): Int {
        val channel = kord.getChannel(channelId)
        val textChannelThread = TextChannelThread(channel!!.data, channel.kord, channel.supplier)
        val lastMsg = channel.data.lastMessageId!!.value!!

        /* TODO: Replace with logs */
        // println("lastMessageId er .. ${channel.data.lastMessageId!!.value!!}")
        // println("lastMessageId er .. ${channel.data.lastMessageId!!.value!!.value}")
        // println("lastMsg typing er .. ${lastMsg.javaClass.name}")

        val messageCount = textChannelThread.getMessagesBefore(lastMsg)
        val count = messageCount.count() + 1
        return count
    }

    private suspend fun countMessagesAfter(channelId: Snowflake, kord: Kord, lastMsg: Snowflake): Int {
        val channel = kord.getChannel(channelId)
        val textChannelThread = TextChannelThread(channel!!.data, channel.kord, channel.supplier)


        val messageCount = textChannelThread.getMessagesAfter(lastMsg)
        val count = messageCount.count()


        /* TODO: Create logs */
        // println(messageCount)
        // println(count)

        return count
    }
}