package com.kovcom.mowid.model

import androidx.annotation.StringRes
import com.kovcom.domain.model.FrequenciesModel
import com.kovcom.domain.model.Frequency
import com.kovcom.domain.model.FrequencyModel
import com.kovcom.mowid.R

data class FrequenciesUIModel(
    val selectedFrequency: FrequencyUIModel?,
    val frequencies: List<FrequencyUIModel>
)

data class FrequencyUIModel(
    val frequencyId: Long,
    @StringRes val value: Int
)

fun FrequenciesUIModel.toDomainModel() = FrequenciesModel(
    selectedFrequency = selectedFrequency?.toDomainModel(),
    frequencies = frequencies.toDomainModel()
)

fun FrequenciesModel.toUIModel() = FrequenciesUIModel(
    selectedFrequency = selectedFrequency?.toUIModel(),
    frequencies = frequencies.toUIModel()
)

fun FrequencyUIModel.toDomainModel() = FrequencyModel(
    frequency = Frequency.getById(frequencyId),
)

fun FrequencyModel.toUIModel() = FrequencyUIModel(
    frequencyId = frequency.id,
    value = when (frequency) {
        Frequency.ONCE_A_WEEK -> R.string.once_a_week
        Frequency.ONCE_IN_FIVE_DAYS -> R.string.once_in_a_five_days
        Frequency.ONCE_IN_TWO_DAYS -> R.string.once_in_two_days
        Frequency.ONCE_A_DAY -> R.string.once_a_day
        Frequency.TWICE_A_DAY -> R.string.twice_a_day
        Frequency.FOURS_A_DAY -> R.string.fours_a_day
    }
)

fun List<FrequencyUIModel>.toDomainModel() = map { it.toDomainModel() }

fun List<FrequencyModel>.toUIModel() = map { it.toUIModel() }
