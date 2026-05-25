package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import com.example.ui.screens.FundScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SalaryScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MasjidViewModel
import com.example.ui.viewmodel.MasjidViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: MasjidViewModel by viewModels {
        MasjidViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Supports full edge-to-edge rendering
        enableEdgeToEdge()

        val sharedPrefs = getSharedPreferences("masjid_auth", Context.MODE_PRIVATE)

        setContent {
            MyApplicationTheme {
                var isLoggedIn by remember { 
                    mutableStateOf(sharedPrefs.getBoolean("is_logged_in", false)) 
                }

                if (isLoggedIn) {
                    MasjidAppLayout(viewModel = viewModel)
                } else {
                    LoginScreen(
                        onLoginSuccess = {
                            sharedPrefs.edit().putBoolean("is_logged_in", true).apply()
                            isLoggedIn = true
                        }
                    )
                }
            }
        }
    }
}

enum class ScreenTab {
    HOME,
    FUND,
    SALARY
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MasjidAppLayout(viewModel: MasjidViewModel) {
    var selectedTab by remember { mutableStateOf(ScreenTab.HOME) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing, // Perfect handling of notch/camera cutout insets
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    // Float navigation slightly above screen bottom for premium look or standard full-bleed
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Item 1: Home Announcement Screen
                NavigationBarItem(
                    selected = selectedTab == ScreenTab.HOME,
                    onClick = { selectedTab = ScreenTab.HOME },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == ScreenTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Mosque Announcements Hub"
                        )
                    },
                    label = { Text("প্রথম পাতা", fontSize = 11.sp) }, // Home in Bangla
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        unselectedIconColor = EmeraldPrimary.copy(alpha = 0.5f),
                        selectedTextColor = EmeraldPrimary,
                        unselectedTextColor = EmeraldPrimary.copy(alpha = 0.5f),
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                // Item 2: Fund Ledger Tracker
                NavigationBarItem(
                    selected = selectedTab == ScreenTab.FUND,
                    onClick = { selectedTab = ScreenTab.FUND },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == ScreenTab.FUND) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                            contentDescription = "Fund & Donations ledger"
                        )
                    },
                    label = { Text("ফান্ড খতিয়ান", fontSize = 11.sp) }, // Fund History in Bangla
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        unselectedIconColor = EmeraldPrimary.copy(alpha = 0.5f),
                        selectedTextColor = EmeraldPrimary,
                        unselectedTextColor = EmeraldPrimary.copy(alpha = 0.5f),
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("nav_fund")
                )

                // Item 3: Imam Salary Management Book
                NavigationBarItem(
                    selected = selectedTab == ScreenTab.SALARY,
                    onClick = { selectedTab = ScreenTab.SALARY },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == ScreenTab.SALARY) Icons.Filled.Payments else Icons.Outlined.Payments,
                            contentDescription = "Imam Salary tracking ledger"
                        )
                    },
                    label = { Text("হুজুর সম্মানি", fontSize = 11.sp) }, // Huzoor Income in Bangla
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = EmeraldPrimary,
                        unselectedIconColor = EmeraldPrimary.copy(alpha = 0.5f),
                        selectedTextColor = EmeraldPrimary,
                        unselectedTextColor = EmeraldPrimary.copy(alpha = 0.5f),
                        indicatorColor = EmeraldPrimary.copy(alpha = 0.1f)
                    ),
                    modifier = Modifier.testTag("nav_salary")
                )
            }
        }
    ) { paddingValues ->
        // Slide / Fade animations when tabs are clicked (comparable to Framer Motion on native Kotlin)
        AnimatedContent(
            targetState = selectedTab,
            transitionSpec = {
                if (initialState.ordinal < targetState.ordinal) {
                    (slideInHorizontally(animationSpec = tween(280)) { width -> width / 2 } + fadeIn(animationSpec = tween(200)))
                        .togetherWith(slideOutHorizontally(animationSpec = tween(280)) { width -> -width / 2 } + fadeOut(animationSpec = tween(150)))
                } else {
                    (slideInHorizontally(animationSpec = tween(280)) { width -> -width / 2 } + fadeIn(animationSpec = tween(200)))
                        .togetherWith(slideOutHorizontally(animationSpec = tween(280)) { width -> width / 2 } + fadeOut(animationSpec = tween(150)))
                }.using(SizeTransform(clip = false))
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) { tab ->
            when (tab) {
                ScreenTab.HOME -> HomeScreen(viewModel = viewModel)
                ScreenTab.FUND -> FundScreen(viewModel = viewModel)
                ScreenTab.SALARY -> SalaryScreen(viewModel = viewModel)
            }
        }
    }
}
