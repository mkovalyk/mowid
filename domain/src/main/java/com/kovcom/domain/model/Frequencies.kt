package com.kovcom.domain.model

data class Frequencies(
    val selectedFrequency: FrequencyModel?,
    val frequencies: List<FrequencyModel>,
)

data class FrequencyModel(
    val frequency: FrequencyType,
)

enum class FrequencyType(val id: Long) {
    ONCE_A_WEEK(168),
    ONCE_IN_FIVE_DAYS(120),
    ONCE_IN_TWO_DAYS(48),
    ONCE_A_DAY(24),
    TWICE_A_DAY(12),
    FOURS_A_DAY(6);

    companion object {

        fun getById(id: Long): FrequencyType = values().first { it.id == id }
    }
}
