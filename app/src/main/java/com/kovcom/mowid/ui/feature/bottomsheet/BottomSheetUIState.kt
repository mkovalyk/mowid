package com.kovcom.mowid.ui.feature.bottomsheet

import com.kovcom.mowid.Label
import com.kovcom.mowid.Title

sealed class BottomSheetUIState(
    val header: String,
    val hint1: String,
    val hint2: String,
    val buttonLabel: String,
    val isSecondFieldMandatory: Boolean,
    val isWordCapitalized: Boolean = false,
    open val id: String? = null,
    open val textField1: String = "",
    open val textField2: String = "",
) {
    data object AddGroupBottomSheet : BottomSheetUIState(
        header = Title.Add.Group.value,
        hint1 = Label.Group.value,
        hint2 = Label.Description.value,
        buttonLabel = Label.Add.value,
        isSecondFieldMandatory = true,
    )

    data object AddQuoteBottomSheet : BottomSheetUIState(
        header = Title.Add.Quote.value,
        hint1 = Label.Quote.value,
        hint2 = Label.Author.value,
        buttonLabel = Label.Add.value,
        isSecondFieldMandatory = false,
        isWordCapitalized = true,
    )

    data class EditGroupBottomSheet(
        override val id: String,
        override val textField1: String,
        override val textField2: String,
    ) : BottomSheetUIState(
        header = Title.Edit.Group.value,
        hint1 = Label.Group.value,
        hint2 = Label.Description.value,
        buttonLabel = Label.Edit.value,
        id = id,
        textField1 = textField1,
        textField2 = textField2,
        isSecondFieldMandatory = true,
    )


    data class EditQuoteBottomSheet(
        override val id: String,
        override val textField1: String,
        override val textField2: String,
    ) : BottomSheetUIState(
        header = Title.Edit.Quote.value,
        hint1 = Label.Quote.value,
        hint2 = Label.Author.value,
        buttonLabel = Label.Edit.value,
        id = id,
        textField1 = textField1,
        textField2 = textField2,
        isSecondFieldMandatory = false,
    )
}
