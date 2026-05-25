package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertRed
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldPrimaryVariant
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.LightOnSurface
import com.example.ui.theme.LightSurface
import com.example.ui.theme.SuccessGreen

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Strict permanent credentials
    val targetPhone = "01410112006"
    val targetPassword = "Maruf2006"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        EmeraldPrimaryVariant,
                        EmeraldPrimary,
                        MaterialTheme.colorScheme.background
                    ),
                    startY = 0f,
                    endY = 1000f
                )
            )
            .imePadding()
            .testTag("login_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // App Brand/Logo Header
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .background(GoldAccent.copy(alpha = 0.15f), CircleShape)
                    .border(2.dp, GoldAccent, CircleShape)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountBalance,
                    contentDescription = "Mosque Logo",
                    tint = GoldAccent,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mosque Title in beautiful Bengali calligraphy styling
            Text(
                text = "মসজিদ",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "হিসাব ও নোটিশ বোর্ড ব্যবস্থাপনা",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = GoldAccent,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Main Credential Input Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = LightSurface),
                border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.15f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "লগইন করুন",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Error Message Callout
                    AnimatedVisibility(
                        visible = errorMessage.isNotEmpty(),
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AlertRed.copy(alpha = 0.1f))
                                .border(1.dp, AlertRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = AlertRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Phone Number Input
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = {
                            phoneNumber = it
                            errorMessage = ""
                        },
                        label = { Text("মোবাইল নম্বর", fontSize = 13.sp) },
                        placeholder = { Text("যেমন: 014XXXXXXXX", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Phone, contentDescription = null)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_phone_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LightOnSurface,
                            unfocusedTextColor = LightOnSurface,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = EmeraldPrimary.copy(alpha = 0.3f),
                            focusedLabelColor = EmeraldPrimary,
                            unfocusedLabelColor = LightOnSurface.copy(alpha = 0.6f),
                            focusedLeadingIconColor = EmeraldPrimary,
                            unfocusedLeadingIconColor = LightOnSurface.copy(alpha = 0.5f),
                            focusedPlaceholderColor = LightOnSurface.copy(alpha = 0.4f),
                            unfocusedPlaceholderColor = LightOnSurface.copy(alpha = 0.4f),
                            cursorColor = EmeraldPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Password Input
                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = ""
                        },
                        label = { Text("পাসওয়ার্ড", fontSize = 13.sp) },
                        placeholder = { Text("গোপন পাসওয়ার্ড", fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "পাসওয়ার্ড লুকান" else "পাসওয়ার্ড দেখান"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                focusManager.clearFocus()
                            }
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = LightOnSurface,
                            unfocusedTextColor = LightOnSurface,
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = EmeraldPrimary.copy(alpha = 0.3f),
                            focusedLabelColor = EmeraldPrimary,
                            unfocusedLabelColor = LightOnSurface.copy(alpha = 0.6f),
                            focusedLeadingIconColor = EmeraldPrimary,
                            unfocusedLeadingIconColor = LightOnSurface.copy(alpha = 0.5f),
                            focusedTrailingIconColor = EmeraldPrimary,
                            unfocusedTrailingIconColor = LightOnSurface.copy(alpha = 0.5f),
                            focusedPlaceholderColor = LightOnSurface.copy(alpha = 0.4f),
                            unfocusedPlaceholderColor = LightOnSurface.copy(alpha = 0.4f),
                            cursorColor = EmeraldPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            if (phoneNumber.trim() == targetPhone && password == targetPassword) {
                                onLoginSuccess()
                            } else {
                                errorMessage = "দুঃখিত, মোবাইল নম্বর অথবা পাসওয়ার্ডটি সঠিক নয়! পুনরায় চেষ্টা করুন।"
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldPrimary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "লগইন করুন",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Soft footer message
            Text(
                text = "© মসজিদ পরিচালনা কমিটি কর্তৃক সংরক্ষিত",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.61f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp, bottom = 20.dp)
            )
        }
    }
}
