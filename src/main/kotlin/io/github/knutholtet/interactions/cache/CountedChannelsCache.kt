package io.github.knutholtet.interactions.cache

import dev.kord.common.entity.Snowflake

object CountedChannelsCache {
    // To avoid ambiguity only allow one variable in this object
    val countedChannelMessages = mutableMapOf<Snowflake, Int>()
    val countedChannelLastIdOnCall = mutableMapOf<Snowflake, Snowflake>()
    val countedChannelName = mutableMapOf<Snowflake, String>()


}