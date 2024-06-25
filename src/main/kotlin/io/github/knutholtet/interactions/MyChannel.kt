package io.github.knutholtet.interactions

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.entity.Message
import dev.kord.core.supplier.EntitySupplier

class MyChannel(
    override val kord: Kord,
    override val id: Snowflake,
    override val supplier: EntitySupplier
) : MessageChannelBehavior {

    override suspend fun createMessage(content: String): Message {
        return super.createMessage(content)
    }
}
