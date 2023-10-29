package com.kovcom.data.mapper

import com.kovcom.data.model.QuoteModel
import com.kovcom.domain.model.Quote

fun QuoteModel.mapToDomain(selectedQuotes: Set<String>) = Quote(
    id = id.orEmpty(),
    author = author.orEmpty(),
    created = created.orEmpty(),
    quote = quote.orEmpty(),
    canBeDeleted = canBeDeleted ?: false,
    isSelected = selectedQuotes.contains(id)
)
