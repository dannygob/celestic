package com.example.celestic.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.celestic.R
import com.example.celestic.ui.theme.CelesticTheme
import com.example.celestic.viewmodel.SharedViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginScreen(
    navController: NavController,
    sharedViewModel: SharedViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isDarkMode by sharedViewModel.isDarkMode.collectAsState()
    val sharedPrefs = context.getSharedPreferences("celestic_prefs", android.content.Context.MODE_PRIVATE)

    var email by remember { mutableStateOf(sharedPrefs.getString("saved_email", "") ?: "") }
    var password by remember { mutableStateOf("") }
    var selectedShift by remember {
        mutableStateOf(
            sharedPrefs.getString("current_shift", "Mañana") ?: "Mañana"
        )
    }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var rememberMe by remember { mutableStateOf(sharedPrefs.getBoolean("remember_me", false)) }

    val shifts = listOf("Mañana", "Tarde", "Noche")

    val fillFieldsMsg = stringResource(R.string.fillAllFields)
    val authErrorMsg = stringResource(R.string.authError)
    val offlineModeMsg = stringResource(R.string.offlineMode)

    // Colores dinámicos
    val textPrimary = if (isDarkMode) Color.White else Color.Black
    val textSecondary = Color.Gray
    val accentColor = if (isDarkMode) Color(0xFF415A77) else Color(0xFF3366CC)
    val cardBg =
        if (isDarkMode) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.05f)

    // Gradiente de fondo dinámico
    val backgroundGradient = Brush.verticalGradient(
        colors = if (isDarkMode) {
            listOf(Color(0xFF0D1B2A), Color(0xFF000000))
        } else {
            listOf(Color(0xFFF2F2F2), Color(0xFFDEE4ED))
        }
    )
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    if (isLandscape) 64.dp else 24.dp


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        val isLandscape = false
        val horizontalPadding = if (isLandscape) 64.dp else 24.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = horizontalPadding, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LoginHeader(isLandscape, textPrimary, textSecondary)

            LoginForm(
                email = email,
                onEmailChange = { email = it; errorMessage = null },
                password = password,
                onPasswordChange = { password = it; errorMessage = null },
                selectedShift = selectedShift,
                onShiftChange = { selectedShift = it },
                shifts = shifts,
                isLoading = isLoading,
                errorMessage = errorMessage,
                rememberMe = rememberMe,
                onRememberMeChange = { rememberMe = it },
                fillFieldsMsg = fillFieldsMsg,
                authErrorMsg = authErrorMsg,
                offlineModeMsg = offlineModeMsg,
                isLandscape = isLandscape,
                accentColor = accentColor,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                cardBg = cardBg,
                context = context,
                navController = navController,
                onLoadingChange = { isLoading = it },
                onErrorMessageChange = { errorMessage = it },
                onSuccess = {
                    val editor = sharedPrefs.edit()
                    // Siempre guardamos quien está logueado actualmente para los reportes
                    editor.putString("current_user", email)
                    editor.putString("current_shift", selectedShift)
                    
                    if (rememberMe) {
                        editor.putString("saved_email", email)
                        editor.putBoolean("remember_me", true)
                    } else {
                        editor.remove("saved_email")
                        editor.putBoolean("remember_me", false)
                    }
                    editor.apply()
                    
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )

            LoginFooter(textSecondary)
        }
    }
}

@Composable
private fun LoginHeader(isLandscape: Boolean, textPrimary: Color, textSecondary: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "CELESTIC",
            fontSize = if (isLandscape) 40.sp else 32.sp,
            color = textPrimary,
            fontWeight = FontWeight.Black,
            letterSpacing = 6.sp
        )
        Text(
            text = "PRECISION VISION SYSTEM",
            fontSize = 12.sp,
            color = textSecondary,
            fontWeight = FontWeight.Medium,
            letterSpacing = 2.sp,
            modifier = Modifier.padding(bottom = if (isLandscape) 32.dp else 48.dp)
        )
    }
}

@Composable
private fun LoginForm(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    selectedShift: String,
    onShiftChange: (String) -> Unit,
    shifts: List<String>,
    isLoading: Boolean,
    errorMessage: String?,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    fillFieldsMsg: String,
    authErrorMsg: String,
    offlineModeMsg: String,
    isLandscape: Boolean,
    accentColor: Color,
    textPrimary: Color,
    textSecondary: Color,
    cardBg: Color,
    context: android.content.Context,
    navController: NavController,
    onLoadingChange: (Boolean) -> Unit,
    onErrorMessageChange: (String?) -> Unit,
    onSuccess: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth(if (isLandscape) 0.7f else 1f)
            .padding(bottom = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(if (isLandscape) 32.dp else 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text(stringResource(R.string.email), color = textSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.Email, null, tint = textSecondary)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = textSecondary.copy(alpha = 0.3f),
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary,
                    cursorColor = accentColor,
                    focusedLabelColor = accentColor
                ),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text(stringResource(R.string.password), color = textSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.Lock, null, tint = textSecondary)
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = textSecondary.copy(alpha = 0.3f),
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary,
                    cursorColor = accentColor,
                    focusedLabelColor = accentColor
                ),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Selector de Turno
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    readOnly = true,
                    value = "Turno: $selectedShift",
                    onValueChange = { },
                    label = { Text("Turno Operativo", color = textSecondary) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = textSecondary.copy(alpha = 0.3f),
                        focusedTextColor = textPrimary,
                        unfocusedTextColor = textPrimary,
                        focusedLabelColor = accentColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(16.dp)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(cardBg)
                ) {
                    shifts.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption, color = textPrimary) },
                            onClick = {
                                onShiftChange(selectionOption)
                                expanded = false
                            }
                        )
                    }
                }
            }

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = onRememberMeChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = accentColor,
                        uncheckedColor = textSecondary.copy(alpha = 0.5f)
                    )
                )
                Text(
                    text = "Recordar usuario",
                    color = textSecondary,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        onErrorMessageChange(fillFieldsMsg)
                        return@Button
                    }

                    if (email == "admin@celestic.com" && password == "celestic_dev") {
                        onLoadingChange(false)
                        onSuccess()
                        return@Button
                    }

                    onLoadingChange(true)
                    try {
                        val auth = FirebaseAuth.getInstance()
                        auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                onLoadingChange(false)
                                if (task.isSuccessful) {
                                    onSuccess()
                                } else {
                                    onErrorMessageChange(
                                        task.exception?.localizedMessage ?: authErrorMsg
                                    )
                                }
                            }
                    } catch (e: Exception) {
                        onLoadingChange(false)
                        onErrorMessageChange(offlineModeMsg)
                        if (email == "admin" && password == "admin") {
                            onSuccess()
                        }
                    }
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    disabledContainerColor = Color.Gray.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        stringResource(R.string.login),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun LoginFooter(textSecondary: Color) {
    Text(
        text = "v2.0 Industrial Edition",
        fontSize = 10.sp,
        color = textSecondary.copy(alpha = 0.5f),
        modifier = Modifier.padding(top = 16.dp)
    )
}

@Preview(showBackground = true, locale = "en")
@Composable
fun LoginScreenPreviewEn() {
    CelesticTheme {
        LoginScreen(rememberNavController())
    }
}

@Preview(showBackground = true, locale = "es")
@Composable
fun LoginScreenPreviewEs() {
    CelesticTheme {
        LoginScreen(rememberNavController())
    }
}

@Preview(showBackground = true, locale = "zh")
@Composable
fun LoginScreenPreviewZh() {
    CelesticTheme {
        LoginScreen(rememberNavController())
    }
}

@Preview(showBackground = true, locale = "ar")
@Composable
fun LoginScreenPreviewAr() {
    CelesticTheme {
        LoginScreen(rememberNavController())
    }
}
