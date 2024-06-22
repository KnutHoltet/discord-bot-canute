package interactions

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.on
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
        /* TODO: Add caching */
    }

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