package io.github.knutholtet

import dev.kord.core.Kord
import dev.kord.gateway.Intent
import io.github.knutholtet.bot.Bot
import io.github.knutholtet.interactions.ChatInputCommandInteraction
import io.github.knutholtet.music.MusicCommands

val botToken: String by lazy {
    requireNotNull(System.getenv("DISCORD_TOKEN")?.takeIf { it.isNotBlank() }) {
        "Set DISCORD_TOKEN to your Discord bot token."
    }
}

suspend fun main() {
    val kord = Kord(botToken)
    Bot(botToken, kord).initialize()
    val interactions = ChatInputCommandInteraction(kord)
    interactions.testCommand("embeded", "tester embeded")
    interactions.countCommand("count-command", "Kommer nå ... ")
    interactions.countAllChannels("countall", "count command")
    MusicCommands(kord).register()
    kord.login {
        intents += Intent.GuildVoiceStates
    }
}
