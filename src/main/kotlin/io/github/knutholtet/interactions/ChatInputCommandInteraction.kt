package io.github.knutholtet.interactions

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.entity.channel.thread.TextChannelThread
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import dev.kord.core.supplier.RestEntitySupplier
import dev.kord.rest.builder.message.embed
import io.github.knutholtet.interactions.cache.CountedChannelsCache
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.runBlocking

class ChatInputCommandInteraction(
    private val kord: Kord
){


    private val restEntitySupplier = RestEntitySupplier(kord)
    private val countedChannelsCache = CountedChannelsCache

    /* COUNTING COMMANDS */
    suspend fun countCommand(commandName: String, interactionContent: String) {
        /* TODO replace deprecated ChatInputCommandInteractionCreateEvent
        *    se : https://dokka.kord.dev/core/dev.kord.core.event.interaction/-chat-input-command-interaction-create-event/index.html
        * */
        println("countCommand commandName = $commandName")

        kord.on<ChatInputCommandInteractionCreateEvent> {
            if(interaction.command.rootName == commandName) {
                interaction.deferEphemeralResponse().respond {
                    content = interactionContent
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
    }

    suspend fun countAllChannels() {
        /* TODO: Add caching */
    }

    /* GREETINGS COMMAND */
    suspend fun helloCommand(commandName: String, interactionContent: String) {

        kord.on<ChatInputCommandInteractionCreateEvent> {
            if(interaction.command.rootName == commandName) {
                interaction.deferEphemeralResponse().respond {
                    content = interactionContent
                }
            }
        }
    }

    /* QUOTE COMMANDS */
    suspend fun quoteLastPerson() {}

    /* ANIME COMMAND */
    fun getRandomAnimeGif() {}

    /* MEME COMMAND */
    fun getRandomMemeGif() {}

    /* TEST COMMAND */

    suspend fun testCommand(commandName: String, interactionContent: String) {
        kord.on<ChatInputCommandInteractionCreateEvent> {
            if(interaction.command.rootName == commandName) {
                interaction.deferEphemeralResponse().respond {
                    content = interactionContent
                }
                /*
                val chanId = interaction.getChannel().id
                val channelForEmbed = MyChannel(kord, chanId, restEntitySupplier)
                val embedBuilder = EmbedBuilder()
                embedBuilder.image = "https://media1.giphy.com/media/v1.Y2lkPTc5MGI3NjExeXgybGQyNjFkMzMybjBmY2FwazBsaDc0MjZ5ZDQyYzNqOGlmeXA3diZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/l3fQf1OEAq0iri9RC/giphy.webp"
                channelForEmbed.createEmbed { embedBuilder.field("hei") }
                 */
                val message = kord.rest.channel.createMessage(channelId = interaction.getChannel().id) {
                    embed {
                        title = "Test"
                        description = "funk for faen"
                        url = "https://media1.giphy.com/media/v1.Y2lkPTc5MGI3NjExeXgybGQyNjFkMzMybjBmY2FwazBsaDc0MjZ5ZDQyYzNqOGlmeXA3diZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/l3fQf1OEAq0iri9RC/giphy.webp"
                        color = Color(0x1ABC9C)
                        image = "https://media1.giphy.com/media/v1.Y2lkPTc5MGI3NjExeXgybGQyNjFkMzMybjBmY2FwazBsaDc0MjZ5ZDQyYzNqOGlmeXA3diZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/l3fQf1OEAq0iri9RC/giphy.webp"

                        author {
                            name = "ligma"
                            // iconUrl = "https://i.pinimg.com/564x/d6/0b/60/d60b60df9147a88c660bc1452385c3a7.jpg"

                        }

                        field {
                            name = "feild en"
                            value = "verdi"
                            inline = true
                        }

                        footer {
                            text = "ligma"
                            icon = "https://i.pinimg.com/564x/d6/0b/60/d60b60df9147a88c660bc1452385c3a7.jpg"
                        }
                    }
                }

                val messageId = message.id


                delay(5000)
                kord.rest.channel.editMessage(interaction.getChannel().id, messageId) {
                    embed {
                        description = "ENDING MAUFAKA"
                    }
                }

            }
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