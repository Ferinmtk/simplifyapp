package com.simplifybiz.mobile.data

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    @TypeConverter
    fun fromIntList(value: List<Int>): String =
        runCatching { json.encodeToString(value) }.getOrElse { "[]" }

    @TypeConverter
    fun toIntList(value: String): List<Int> =
        runCatching { json.decodeFromString<List<Int>>(value) }.getOrElse { emptyList() }

    // Used by ObjectiveEntity.expectedOutcomes (free-text repeater).
    @TypeConverter
    fun fromStringList(value: List<String>): String =
        runCatching { json.encodeToString(value) }.getOrElse { "[]" }

    @TypeConverter
    fun toStringList(value: String): List<String> =
        runCatching { json.decodeFromString<List<String>>(value) }.getOrElse { emptyList() }

    @TypeConverter
    fun fromTargetMarketList(value: List<TargetMarketItem>): String =
        runCatching { json.encodeToString(value) }.getOrElse { "[]" }

    @TypeConverter
    fun toTargetMarketList(value: String): List<TargetMarketItem> =
        runCatching { json.decodeFromString<List<TargetMarketItem>>(value) }.getOrElse { emptyList() }

    // LeadershipSystemItem is shared across Leadership / Sales / Operations /
    // Risk / R&D. This converter pair handles all five modules' systems_used
    // columns — don't add a per-module pair.
    @TypeConverter
    fun fromLeadershipSystemItemList(value: List<LeadershipSystemItem>): String =
        runCatching { json.encodeToString(value) }.getOrElse { "[]" }

    @TypeConverter
    fun toLeadershipSystemItemList(value: String): List<LeadershipSystemItem> =
        runCatching { json.decodeFromString<List<LeadershipSystemItem>>(value) }.getOrElse { emptyList() }

    @TypeConverter
    fun fromMarketingChannelList(value: List<MarketingChannelItem>): String =
        runCatching { json.encodeToString(value) }.getOrElse { "[]" }

    @TypeConverter
    fun toMarketingChannelList(value: String): List<MarketingChannelItem> =
        runCatching { json.decodeFromString<List<MarketingChannelItem>>(value) }.getOrElse { emptyList() }

    @TypeConverter
    fun fromMarketingSystemList(value: List<MarketingSystemItem>): String =
        runCatching { json.encodeToString(value) }.getOrElse { "[]" }

    @TypeConverter
    fun toMarketingSystemList(value: String): List<MarketingSystemItem> =
        runCatching { json.decodeFromString<List<MarketingSystemItem>>(value) }.getOrElse { emptyList() }

    @TypeConverter
    fun fromActionStepList(value: List<ActionStepItem>): String =
        runCatching { json.encodeToString(value) }.getOrElse { "[]" }

    @TypeConverter
    fun toActionStepList(value: String): List<ActionStepItem> =
        runCatching { json.decodeFromString<List<ActionStepItem>>(value) }.getOrElse { emptyList() }

    // Removed: fromSystemItemList / toSystemItemList. The SystemItem data
    // class was only used by Risk and R&D; both now use LeadershipSystemItem,
    // handled by the shared converter above.

    // ---- People Systems ----

    @TypeConverter
    fun fromPeopleSystemList(value: List<PeopleSystemItem>): String =
        runCatching { json.encodeToString(value) }.getOrElse { "[]" }

    @TypeConverter
    fun toPeopleSystemList(value: String): List<PeopleSystemItem> =
        runCatching { json.decodeFromString<List<PeopleSystemItem>>(value) }
            .getOrElse { emptyList() }

    // ---- Money Systems ----

    @TypeConverter
    fun fromMoneySystemList(value: List<MoneySystemItem>): String =
        runCatching { json.encodeToString(value) }.getOrElse { "[]" }

    @TypeConverter
    fun toMoneySystemList(value: String): List<MoneySystemItem> =
        runCatching { json.decodeFromString<List<MoneySystemItem>>(value) }
            .getOrElse { emptyList() }

    // ---- Links ----

    @TypeConverter
    fun fromLinkItemList(value: List<LinkItem>): String =
        runCatching { json.encodeToString(value) }.getOrElse { "[]" }

    @TypeConverter
    fun toLinkItemList(value: String): List<LinkItem> =
        runCatching { json.decodeFromString<List<LinkItem>>(value) }
            .getOrElse { emptyList() }


}