package com.kovcom.mowid.playground

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kovcom.design.obj.button.*
import com.kovcom.design.theme.MoWidTheme

class ComponentsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MoWidTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    floatingActionButton = {
                        Fab(icon = FabIconType.Add, onClick = {})
                    },
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