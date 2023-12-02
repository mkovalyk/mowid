package com.kovcom.mowid.ui.feature.settings

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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kovcom.data.firebase.source.FirebaseDataSourceImpl
import com.kovcom.mowid.*
import com.kovcom.mowid.R
import com.kovcom.mowid.base.ui.EVENTS_KEY
import com.kovcom.mowid.model.UiFrequency
import com.kovcom.mowid.ui.composable.AppCenterAlignedTopAppBar
import com.kovcom.mowid.ui.composable.AppProgress
import com.kovcom.mowid.ui.feature.main.MainUserIntent
import com.kovcom.mowid.ui.feature.main.MainViewModel
import com.kovcom.mowid.ui.feature.settings.SettingsContract.Intent
import com.kovcom.mowid.ui.feature.settings.SettingsContract.State
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
    val state: State by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current

    LaunchedEffect(EVENTS_KEY) {
        viewModel.event.onEach { event ->
            when (event) {
                is SettingsContract.Event.ShowToast -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                is SettingsContract.Event.ShowToastId -> Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
            }
        }.collect()
    }

    ScreenContent(
        state = state,
        sendMainEvent = activityViewModel::processIntent,
        sendEvent = viewModel::processIntent,
        onBackClicked = onBackClicked,
    )
}

@Composable
fun ScreenContent(
    state: State,
    sendMainEvent: (MainUserIntent) -> Unit,
    sendEvent: (Intent) -> Unit,
    onBackClicked: () -> Unit,
) {

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(topBar = {
            AppCenterAlignedTopAppBar(title = Title.Settings.value, navigationIcon = {
                IconButton(onClick = onBackClicked) {
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
    state: State,
    sendMainEvent: (MainUserIntent) -> Unit,
    sendEvent: (Intent) -> Unit,
) {

    val userInfoLabel = buildAnnotatedString {
        if (state.userModel == null) {
            append(Label.User.Not.Registered.value)
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                pushStringAnnotation(
                    tag = SIGN_IN_TAG, annotation = Label.Sign.In.value
                )
                append(Label.Sign.In.value)
            }
        } else {
            append(Label.User.Signed.In.As.value)
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                pushStringAnnotation(
                    tag = USER_NAME_TAG, annotation = state.userModel.fullName
                )
                append(state.userModel.fullName)
            }
            append(" ")
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
                pushStringAnnotation(
                    tag = SIGN_OUT_TAG, annotation = Label.Sign.Out.value
                )
                append(Label.Sign.Out.value)
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

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    value = selectedFrequency?.key ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(Label.Frequency.value) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_settings), contentDescription = "TODO"
                        )
                    }, trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDropDown)
                })

                ExposedDropdownMenu(expanded = showDropDown, onDismissRequest = { showDropDown = false }) {
                    state.frequencies.forEach { option ->
                        DropdownMenuItem(text = { Text(option.key) }, onClick = {
                            selectedFrequency = option
                            showDropDown = false
                        })
                    }
                }
            }

            Button(onClick = {
                sendEvent(
                    Intent.FrequencyChanged(
                        selectedFrequency?.frequencyId
                            ?: FirebaseDataSourceImpl.DEFAULT_FREQUENCY_VALUE
                    )
                )
            }) {
                Text(
                    text = Label.Apply.value, style = MaterialTheme.typography.labelLarge
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
                key = Once.A.Day.value,
            )
        )
        ScreenContent(
            state = State(
                isLoading = false,
                selectedFrequency = UiFrequency(
                    frequencyId = 0,
                    key = Once.A.Day.value,
                ),
                frequencies = list,
                userModel = null,
            ),
            sendMainEvent = {},
            sendEvent = {},
            onBackClicked = {},
        )
    }
}
