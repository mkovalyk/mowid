package com.kovcom.mowid.model

import androidx.annotation.StringRes
import com.kovcom.domain.model.Frequencies
import com.kovcom.domain.model.FrequencyModel
import com.kovcom.domain.model.FrequencyType
import com.kovcom.mowid.R

data class UiFrequencies(
    val selectedFrequency: UiFrequency?,
    val frequencies: List<UiFrequency>,
)

data class UiFrequency(
    val frequencyId: Long,
    @StringRes val value: Int,
)

fun UiFrequencies.toDomainModel() = Frequencies(
    selectedFrequency = selectedFrequency?.toDomainModel(),
    frequencies = frequencies.toDomainModel()
)

fun Frequencies.toUIModel() = UiFrequencies(
    selectedFrequency = selectedFrequency?.toUIModel(),
    frequencies = frequencies.toUIModel()
)

fun UiFrequency.toDomainModel() = FrequencyModel(
    frequency = FrequencyType.getById(frequencyId),
)

fun FrequencyModel.toUIModel() = UiFrequency(
    frequencyId = frequency.id,
    value = when (frequency) {
            FrequencyType.ONCE_A_WEEK -> R.string.once_a_week
            FrequencyType.ONCE_IN_FIVE_DAYS -> R.string.once_in_a_five_days
            FrequencyType.ONCE_IN_TWO_DAYS -> R.string.once_in_two_days
            FrequencyType.ONCE_A_DAY -> R.string.once_a_day
            FrequencyType.TWICE_A_DAY -> R.string.twice_a_day
            FrequencyType.FOURS_A_DAY -> R.string.fours_a_day
    }
)

fun List<UiFrequency>.toDomainModel() = map { it.toDomainModel() }

fun List<FrequencyModel>.toUIModel() = map { it.toUIModel() }
