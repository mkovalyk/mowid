package com.kovcom.mowid.model

import android.os.Parcelable
import com.kovcom.domain.model.Quote
import kotlinx.parcelize.Parcelize

@Parcelize
data class UiQuote(
    val id: String,
    val author: String,
    val created: String,
    val quote: String,
    val canBeDeleted: Boolean,
    val isSelected: Boolean,
) : Parcelable

fun UiQuote.toDomainModel() = Quote(
    id = id,
    author = author,
    created = created,
    quote = quote,
    isSelected = isSelected,
    canBeDeleted = canBeDeleted
)

fun Quote.toUIModel() = UiQuote(
    id = id,
    author = author,
    created = created,
    quote = quote,
    isSelected = isSelected,
    canBeDeleted = canBeDeleted
)

fun List<UiQuote>.toDomainModel() = map { it.toDomainModel() }

fun List<Quote>.toUIModel() = map { it.toUIModel() }
