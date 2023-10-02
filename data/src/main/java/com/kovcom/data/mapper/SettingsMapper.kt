package com.kovcom.data.mapper

import com.kovcom.data.firebase.source.impl.FirebaseDataSourceImpl.Companion.DEFAULT_FREQUENCY_VALUE
import com.kovcom.data.model.FrequencyDataModel
import com.kovcom.domain.model.FrequenciesModel
import com.kovcom.domain.model.Frequency
import com.kovcom.domain.model.FrequencyModel

fun List<FrequencyDataModel>.toDomain(userSetting: Long) = FrequenciesModel(
    selectedFrequency = this.firstOrNull { it.frequencyId == userSetting }?.toDomain(),
    frequencies = this.map { it.toDomain() }
)

fun FrequencyDataModel.toDomain() = FrequencyModel(
    frequency = Frequency.getById(frequencyId ?: DEFAULT_FREQUENCY_VALUE)
)
