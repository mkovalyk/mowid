package com.kovcom.domain.model

data class Frequencies(
    val selectedFrequency: FrequencyModel?,
    val frequencies: List<FrequencyModel>,
)

data class FrequencyModel(
    val frequency: FrequencyType,
)

enum class FrequencyType(val id: Long) {
    OnceAWeek(168),
    OnceInFiveDays(120),
    OnceInTwoDays(48),
    OnceADay(24),
    TwiceADay(12),
    FoursADay(6);

    companion object {

        fun getById(id: Long): FrequencyType = values().first { it.id == id }
    }
}
