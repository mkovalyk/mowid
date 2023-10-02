package com.kovcom.domain.model

data class FrequenciesModel(
    val selectedFrequency: FrequencyModel?,
    val frequencies: List<FrequencyModel>
)

data class FrequencyModel(
    val frequency: Frequency,
)

enum class Frequency(val id: Long) {
    ONCE_A_WEEK(168),
    ONCE_IN_FIVE_DAYS(120),
    ONCE_IN_TWO_DAYS(48),
    ONCE_A_DAY(24),
    TWICE_A_DAY(12),
    FOURS_A_DAY(6);

    companion object {
        fun getById(id: Long): Frequency = values().first { it.id == id }
    }
}
