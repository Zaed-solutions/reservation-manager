package com.zaed.reservationmanager.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.zaed.reservationmanager.R
import com.zaed.reservationmanager.app.navigation.NavigationDrawerItem
import com.zaed.reservationmanager.app.navigation.NavigationHost
import com.zaed.reservationmanager.app.navigation.Route
import com.zaed.reservationmanager.ui.theme.ReservationManagerTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReservationManagerTheme {
                App()
            }
        }
    }
}

@Composable
fun App() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = (navBackStackEntry?.destination?.route
        ?: Route.ReservationListRoute::class.qualifiedName.orEmpty()).substringBefore("?")
    val navDrawerRoutes = NavigationDrawerItem.entries.map { it.route::class.qualifiedName.orEmpty() }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedNavDrawerItem by remember {
        mutableStateOf(NavigationDrawerItem.HOME)
    }
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = navDrawerRoutes.contains(currentRoute),
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_launcher_foreground),
                        contentDescription = "App Logo",
                        modifier = Modifier.size(250.dp)
                    )

                }
                HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
                NavigationDrawerItem.entries.forEach { item ->
                    NavigationDrawerItem(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        label = { Text(text = stringResource(item.titleRes)) },
                        selected = item == selectedNavDrawerItem,
                        onClick = {
                            selectedNavDrawerItem = item
                            navController.navigate(item.route)
                            scope.launch {
                                drawerState.apply {
                                    if (isClosed) open() else close()
                                }
                            }
                        }
                    )
                }
            }
        },
    ) {
        NavigationHost(
            modifier = Modifier,
            navController = navController,
            onShowNavDrawer = {
                scope.launch {
                    drawerState.apply {
                        if (isClosed) open() else close()
                    }
                }
            }
        )
    }


}