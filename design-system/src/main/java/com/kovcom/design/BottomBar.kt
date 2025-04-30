package com.kovcom.design

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kovcom.design.obj.button.Fab
import com.kovcom.design.obj.button.FabIconType


data class BottomBarState(
    val icons: List<BottomBarIconType>,
    val selectedIcon: BottomBarIconType? = null,
    val fabIcon: FabIconType = FabIconType.Add,
)

enum class BottomBarIconType {
    Home,
    Favorites,
    Profile
}

val BottomBarIconType.icon: Int
    get() = when (this) {
        BottomBarIconType.Home -> R.drawable.home
        BottomBarIconType.Favorites -> R.drawable.saved_quotes
        BottomBarIconType.Profile -> R.drawable.profile
    }

val BottomBarIconType.iconFilled: Int
    get() = when (this) {
        BottomBarIconType.Home -> R.drawable.home_filled
        BottomBarIconType.Favorites -> R.drawable.saved_quotes_filled
        BottomBarIconType.Profile -> R.drawable.profile_filled
    }

@Composable
fun BottomBar(
    state: BottomBarState,
    onFabClick: () -> Unit = {},
    onIconClick: (BottomBarIconType) -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = MaterialTheme.colorScheme.secondary,
        shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            state.icons.forEach { icon ->
                IconButton(
                    onClick = { onIconClick(icon) },
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        painter = painterResource(if (state.selectedIcon == icon) icon.iconFilled else icon.icon),
                        contentDescription = icon.name
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Fab(state.fabIcon, onClick = onFabClick)
        }
    }
}

@Preview
@Composable
fun BottomBarPreview() {
    BottomBar(
        state = BottomBarState(
            icons = listOf(
                BottomBarIconType.Home,
                BottomBarIconType.Favorites,
                BottomBarIconType.Profile
            ),
            selectedIcon = BottomBarIconType.Home,
        ),
        onFabClick = {},
        onIconClick = {}
    )
}