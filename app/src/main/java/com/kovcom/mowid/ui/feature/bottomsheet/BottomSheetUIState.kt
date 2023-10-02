package com.kovcom.mowid.ui.feature.bottomsheet

import androidx.annotation.StringRes
import com.kovcom.mowid.R

sealed class BottomSheetUIState(
    @StringRes
    val header: Int,
    @StringRes
    val hint1: Int,
    @StringRes
    val hint2: Int,
    @StringRes
    val buttonLabel: Int,
    val isSecondFieldMandatory: Boolean,
    open val id: String? = null,
    open val textField1: String = "",
    open val textField2: String = "",
) {
    object AddGroupBottomSheet : BottomSheetUIState(
        header = R.string.title_add_group,
        hint1 = R.string.label_group,
        hint2 = R.string.label_description,
        buttonLabel = R.string.label_add,
        isSecondFieldMandatory = true,
    )

    object AddQuoteBottomSheet : BottomSheetUIState(
        header = R.string.title_add_quote,
        hint1 = R.string.label_quote,
        hint2 = R.string.label_author,
        buttonLabel = R.string.label_add,
        isSecondFieldMandatory = false,
    )

    data class EditGroupBottomSheet(
        override val id: String,
        override val textField1: String,
        override val textField2: String,
    ) : BottomSheetUIState(
        header = R.string.title_edit_group,
        hint1 = R.string.label_group,
        hint2 = R.string.label_description,
        buttonLabel = R.string.label_edit,
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
        header = R.string.title_edit_quote,
        hint1 = R.string.label_quote,
        hint2 = R.string.label_author,
        buttonLabel = R.string.label_edit,
        id = id,
        textField1 = textField1,
        textField2 = textField2,
        isSecondFieldMandatory = false,
    )
}
