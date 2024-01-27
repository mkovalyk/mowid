package com.kovcom.mowid.model

import com.kovcom.domain.model.Frequencies
import com.kovcom.domain.model.FrequencyModel
import com.kovcom.domain.model.FrequencyType
import com.kovcom.mowid.Fours
import com.kovcom.mowid.Once
import com.kovcom.mowid.Twice

data class UiFrequencies(
    val selectedFrequency: UiFrequency?,
    val frequencies: List<UiFrequency>,
)

data class UiFrequency(
    val frequencyId: Long,
    val key: String,
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
    key = when (frequency) {
            FrequencyType.OnceAWeek -> Once.A.Week.value
            FrequencyType.OnceInFiveDays -> Once.In.A.Five.Days.value
            FrequencyType.OnceInTwoDays -> Once.In.Two.Days.value
            FrequencyType.OnceADay -> Once.A.Day.value
            FrequencyType.TwiceADay -> Twice.A.Day.value
            FrequencyType.FoursADay -> Fours.A.Day.value
    }
)

fun List<UiFrequency>.toDomainModel() = map { it.toDomainModel() }

fun List<FrequencyModel>.toUIModel() = map { it.toUIModel() }
