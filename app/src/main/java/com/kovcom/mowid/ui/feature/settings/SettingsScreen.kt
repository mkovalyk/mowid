package com.kovcom.mowid.ui.feature.settings

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kovcom.data.firebase.source.FirebaseDataSourceImpl
import com.kovcom.mowid.R
import com.kovcom.mowid.base.ui.EFFECTS_KEY
import com.kovcom.mowid.base.ui.EVENTS_KEY
import com.kovcom.mowid.model.UiFrequency
import com.kovcom.mowid.ui.composable.AppCenterAlignedTopAppBar
import com.kovcom.mowid.ui.composable.AppProgress
import com.kovcom.mowid.ui.feature.main.MainEvent
import com.kovcom.mowid.ui.feature.main.MainUserIntent
import com.kovcom.mowid.ui.feature.main.MainViewModel
import com.kovcom.mowid.ui.theme.MoWidTheme
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import timber.log.Timber

private const val SIGN_IN_TAG = "SIGN_IN_TAG"
private const val SIGN_OUT_TAG = "SIGN_OUT_TAG"
private const val USER_NAME_TAG = "USER_NAME_TAG"

@Composable
fun SettingsScreen(
    activityViewModel: MainViewModel,
    viewModel: SettingsViewModel,
    onBackClicked: () -> Unit,
) {
    val state: SettingsState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(EFFECTS_KEY) {
        viewModel.effect.onEach { effect ->
            when (effect) {
                is SettingsEffect.ShowToast -> Toast.makeText(
                    context, effect.message, Toast.LENGTH_SHORT
                ).show()

                is SettingsEffect.ShowToastId -> Toast.makeText(
                    context, effect.messageId, Toast.LENGTH_SHORT
                ).show()
            }
        }.collect()
    }

    LaunchedEffect(EVENTS_KEY) {
        viewModel.event.onEach { event ->
            when (event) {
                SettingsEvent.BackButtonClicked -> onBackClicked()
                is SettingsEvent.OnFrequencyChanged -> {}
            }
        }.collect()
    }

    ScreenContent(
        state = state,
        sendMainEvent = activityViewModel::processIntent,
        sendEvent = viewModel::publishEvent
    )
}

@Composable
fun ScreenContent(
    state: SettingsState,
    sendMainEvent: (MainUserIntent) -> Unit,
    sendEvent: (SettingsEvent) -> Unit,
) {

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(topBar = {
            AppCenterAlignedTopAppBar(title = stringResource(id = R.string.title_settings), navigationIcon = {
                IconButton(onClick = { sendEvent(SettingsEvent.BackButtonClicked) }) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack, contentDescription = "Back"
                    )
                }
            })
        }) { padding ->
            when {
                state.isLoading -> AppProgress()
                else -> Content(
                    padding = padding, state = state, sendEvent = sendEvent, sendMainEvent = sendMainEvent
                )


            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun Content(
    padding: PaddingValues,
    state: SettingsState,
    sendMainEvent: (MainUserIntent) -> Unit,
    sendEvent: (SettingsEvent) -> Unit,
) {

    val userInfoLabel = buildAnnotatedString {
        if (state.userModel == null) {
            append(stringResource(R.string.label_user_not_registered))
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                pushStringAnnotation(
                    tag = SIGN_IN_TAG, annotation = stringResource(id = R.string.label_sign_in)
                )
                append(stringResource(id = R.string.label_sign_in))
            }
        } else {
            append(stringResource(R.string.label_user_signed_in_as))
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                pushStringAnnotation(
                    tag = USER_NAME_TAG, annotation = state.userModel.fullName
                )
                append(state.userModel.fullName)
            }
            append(" ")
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
                pushStringAnnotation(
                    tag = SIGN_OUT_TAG, annotation = stringResource(id = R.string.label_sign_out)
                )
                append(stringResource(id = R.string.label_sign_out))
            }

        }
    }

    var showDropDown by remember { mutableStateOf(false) }

    var selectedFrequency by remember { mutableStateOf(state.selectedFrequency) }

    Surface(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
        ) {

            ExposedDropdownMenuBox(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), expanded = showDropDown, onExpandedChange = { showDropDown = !showDropDown }) {

                OutlinedTextField(modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(), value = selectedFrequency?.value?.let { stringResource(id = it) }
                    ?: "", onValueChange = {}, readOnly = true, label = { Text(stringResource(id = R.string.label_frequency)) }, leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings), contentDescription = "TODO"
                    )
                }, trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropDown)
                })

                ExposedDropdownMenu(expanded = showDropDown, onDismissRequest = { showDropDown = false }) {
                    state.frequencies.forEach { option ->
                        DropdownMenuItem(text = { Text(stringResource(id = option.value)) }, onClick = {
                            selectedFrequency = option
                            showDropDown = false
                        })
                    }
                }
            }

            Button(onClick = {
                sendEvent(
                    SettingsEvent.OnFrequencyChanged(
                        selectedFrequency?.frequencyId
                            ?: FirebaseDataSourceImpl.DEFAULT_FREQUENCY_VALUE
                    )
                )
            }) {
                Text(
                    text = stringResource(id = R.string.label_apply), style = MaterialTheme.typography.labelLarge
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            ClickableText(text = userInfoLabel, onClick = { offset ->
                userInfoLabel.getStringAnnotations(offset, offset).firstOrNull()?.let { span ->
                    Timber.d("SettingScreen", "Clicked on ${span.tag}")
                    when (span.tag) {
                        SIGN_IN_TAG -> sendMainEvent(MainUserIntent.SignIn)
                        SIGN_OUT_TAG -> sendMainEvent(MainUserIntent.SignOut)
                        USER_NAME_TAG -> {}
                    }
                }
            })
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ScreenContentPreview() {
    MoWidTheme {
        val list = listOf(
            UiFrequency(
                frequencyId = 0,
                value = R.string.once_a_day,
            )
        )
        ScreenContent(
            state = SettingsState(
                isLoading = false,
                selectedFrequency = UiFrequency(
                    frequencyId = 0,
                    value = R.string.once_a_day,
                ),
                frequencies = list,
                userModel = null,
            ),
            sendMainEvent = {},
            sendEvent = {},
        )
    }
}
