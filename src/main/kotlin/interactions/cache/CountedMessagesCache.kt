package interactions.cache

import dev.kord.common.entity.Snowflake
import kotlinx.coroutines.flow.MutableStateFlow

object CountedMessagesCache {

    var countedMessages = 0
    val messageIdOnFirstCall = mutableListOf<Snowflake>()
}
