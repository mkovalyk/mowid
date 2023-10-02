package com.kovcom.data.mapper

import com.kovcom.data.model.QuoteDataModel
import com.kovcom.domain.model.QuoteModel

fun QuoteDataModel.mapToDomain(selectedQuotes: Set<String>) = QuoteModel(
    id = id.orEmpty(),
    author = author.orEmpty(),
    created = created.orEmpty(),
    quote = quote.orEmpty(),
    canBeDeleted = canBeDeleted ?: false,
    isSelected = selectedQuotes.contains(id)
)
