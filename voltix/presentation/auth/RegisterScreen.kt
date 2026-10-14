package com.example.voltix.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voltix.core.ValidationUtils
import com.example.voltix.data.repository.UserPreferencesRepository
import com.example.voltix.ui.theme.VoltixBackground
import com.example.voltix.ui.theme.VoltixTeal
import kotlinx.coroutines.launch

import com.example.voltix.ui.theme.voltixPalette
import com.example.voltix.ui.theme.onAccentWhite()
enum class UserRole(val title: String) {
    SELLER("Продавец"),
    BUYER("Покупатель")
}

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val palette = voltixPalette()
    val palette = voltixPalette()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userPrefs = remember { UserPreferencesRepository(context) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }
    var isRoleExpanded by remember { mutableStateOf(false) }
    var isPrivacyAccepted by remember { mutableStateOf(false) }

    val isEmailValid = ValidationUtils.isValidEmail(email)
    val isPasswordValid = ValidationUtils.isValidPassword(password)
    val isFormValid = name.isNotBlank() && isEmailValid && isPasswordValid && (selectedRole != null) && isPrivacyAccepted

    val fieldBg = palette.chipUnselected
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoltixBackground)
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(top = 48.dp, bottom = 90.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Регистрация",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Что бы зарегистрироваться в\nприложении, введите имя, email и\nпароль",
                fontSize = 13.sp,
                color = palette.textSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Имя", color = palette.textTertiary, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Name Icon",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = fieldBg,
                    unfocusedContainerColor = fieldBg,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("E-mail", color = palette.textTertiary, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Email,
                        contentDescription = "Email Icon",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = fieldBg,
                    unfocusedContainerColor = fieldBg,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Пароль", color = palette.textTertiary, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Password Icon",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = fieldBg,
                    unfocusedContainerColor = fieldBg,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clickable { isRoleExpanded = !isRoleExpanded },
                    shape = RoundedCornerShape(12.dp),
                    color = fieldBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedRole?.title ?: "Выберите роль...",
                            fontSize = 14.sp,
                            color = if (selectedRole == null) palette.textTertiary else palette.textPrimary
                        )
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = "Expand Role",
                            tint = palette.textSecondary
                        )
                    }
                }

                DropdownMenu(
                    expanded = isRoleExpanded,
                    onDismissRequest = { isRoleExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.88f)
                ) {
                    DropdownMenuItem(
                        text = { Text("Продавец", fontSize = 14.sp) },
                        onClick = {
                            selectedRole = UserRole.SELLER
                            isRoleExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Покупатель", fontSize = 14.sp) },
                        onClick = {
                            selectedRole = UserRole.BUYER
                            isRoleExpanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isPrivacyAccepted = !isPrivacyAccepted },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = isPrivacyAccepted,
                    onCheckedChange = { isPrivacyAccepted = it },
                    colors = CheckboxDefaults.colors(checkedColor = palette.teal)
                )
                Text(
                    text = "Я соглашаюсь с условиями политики\nконфиденциальности",
                    fontSize = 12.sp,
                    color = palette.textSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Уже есть аккаунт? ", fontSize = 13.sp, color = palette.textSecondary)
                Text(
                    text = "Войти",
                    fontSize = 13.sp,
                    color = palette.blue,
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (isFormValid) {
                        coroutineScope.launch {
                            userPrefs.saveSession(email.trim(), selectedRole?.name ?: "BUYER")
                            onRegisterSuccess()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = isFormValid,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = palette.teal,
                    disabledContainerColor = palette.teal.copy(alpha = 0.45f)
                )
            ) {
                Text("Продолжить", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = onAccentWhite())
            }
        }

        Text(
            text = "Нажимая на кнопку \"Продолжить\" Вы\nсоглашаетесь на обработку персональных\nданных",
            fontSize = 11.sp,
            color = palette.divider,
            textAlign = TextAlign.Center,
            lineHeight = 15.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
        )
    }
}