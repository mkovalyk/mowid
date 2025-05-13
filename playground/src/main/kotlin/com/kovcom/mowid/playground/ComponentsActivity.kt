package com.kovcom.mowid.playground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kovcom.design.BottomBar
import com.kovcom.design.BottomBarIconType
import com.kovcom.design.BottomBarState
import com.kovcom.design.obj.button.*
import com.kovcom.design.theme.MoWidTheme
import timber.log.Timber

class ComponentsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoWidTheme {
                var bottomState by remember {
                    mutableStateOf(
                        BottomBarState(
                            icons = listOf(
                                BottomBarIconType.Home,
                                BottomBarIconType.Favorites,
                                BottomBarIconType.Profile
                            ),
                            selectedIcon = BottomBarIconType.Home,
                            fabIcon = FabIconType.Add
                        )
                    )
                }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        BottomBar(
                            state = bottomState,
                            onFabClick = {
                                Timber.tag("BottomBar").d("FAB clicked")
                            },
                            onIconClick = { icon ->
                                Timber.tag("BottomBar").d("Icon clicked: $icon")
                                bottomState = bottomState.copy(
                                    selectedIcon = icon
                                )
                            },
                        )
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(paddingValues)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PrimaryButtons()
                        SecondaryButtons()
                        OutlinedButtons()
                    }
                }
            }
        }
    }

    @Composable
    private fun SecondaryButtons() {
        Text("Secondary:")
        SecondaryButton(text = "Buttons", onClick = {})
        SecondaryButton(text = "Buttons", enabled = false, onClick = {})
    }

    @Composable
    private fun PrimaryButtons() {
        Text("Primary: ")
        PrimaryButton(text = "Buttons", onClick = {})
        PrimaryButton(text = "Buttons", enabled = false, onClick = {})
    }

    @Composable
    private fun OutlinedButtons() {
        Text("Outlined: ")
        Outlined(text = "Buttons", onClick = {})
        Outlined(text = "Buttons", enabled = false, onClick = {})
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MoWidTheme {
        Greeting("Android")
    }
}