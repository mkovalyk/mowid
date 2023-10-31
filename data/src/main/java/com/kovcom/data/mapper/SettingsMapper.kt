package com.kovcom.data.mapper

import com.kovcom.data.firebase.source.FirebaseDataSourceImpl.Companion.DEFAULT_FREQUENCY_VALUE
import com.kovcom.domain.model.Frequencies
import com.kovcom.domain.model.FrequencyType

fun List<com.kovcom.data.model.FrequencyModel>.toDomain(userSetting: Long) = Frequencies(
    selectedFrequency = this.firstOrNull { it.frequencyId == userSetting }?.toDomain(),
    frequencies = this.map { it.toDomain() }
)

fun com.kovcom.data.model.FrequencyModel.toDomain() = com.kovcom.domain.model.FrequencyModel(
    frequency = FrequencyType.getById(frequencyId ?: DEFAULT_FREQUENCY_VALUE)
)
