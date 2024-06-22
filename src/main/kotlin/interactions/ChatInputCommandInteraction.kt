package interactions

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import interactions.cache.CountedMessagesCache
import kotlinx.serialization.json.JsonNull.content

class ChatInputCommandInteraction(
    private val kord: Kord
){

    private fun chatInputCommandCreateEvent(commandName: String, interactionContent: String, function: () -> Unit) {
        kord.on<ChatInputCommandInteractionCreateEvent> {
            if(interaction.command.rootName == commandName) {
                interaction.deferEphemeralResponse().respond {
                    content = interactionContent
                }
            }

            function()
        }
    }

    /* COUNTING COMMANDS */
    fun countCommand() {
        /* TODO:
        *   - Add caching
        *   - getMessagesAfter : kord standard library
        * */
        chatInputCommandCreateEvent("count-command", "Meldingene kommer strats!") {


        }
    }

    /*
    // we need to make a command for guild countThisChannel
    kord.on<ChatInputCommandInteractionCreateEvent> {
        if(interaction.command.rootName == "count-command") {
            interaction.deferEphemeralResponse().respond {
                content = "kommer nå botten min er treg bare"
            }

            val antMsg = countMessages(interaction.getChannel().id, kord)

            val chanId = interaction.getChannel().id

            val chan = MyChannel(kord, chanId, restEntitySupplier)
            println(chan)
            println(antMsg)
            chan.createMessage(antMsg)
            //println(interaction.getChannel().id)
        }
    }
     */

    fun countAllChannels() {
        /* TODO: Add caching */
    }

    /* Greetings command */
    fun helloCommand() {
        chatInputCommandCreateEvent("hei-command", "Heisann!") {}
    }

    /* QUOTE COMMANDS */
    fun quoteLastPerson() {}

    /* ANIME COMMAND */
    fun getRandomAnimeGif() {}

    /* MEME COMMAND */
    fun getRandomMemeGif() {}
}