package com.example.celestic.ui.component

import android.content.Context
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.celestic.R
import com.example.celestic.utils.CalibrationPatternGenerator

/**
 * Data model for each step in the guided technical inspection wizard.
 */
data class WizardStep(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color,
    val bulletPoints: List<String>,
    val industrialTip: String,
    val actionText: String? = null,
    val onAction: ((Context) -> Unit)? = null
)

/**
 * Guided Operator Technical Inspection & Calibration Assistant.
 * Fully localized with zero hardcoded strings.
 */
@Composable
fun InspectionWizardDialog(
    onDismiss: () -> Unit,
    onNavigateToCalibration: () -> Unit = {},
    isDarkMode: Boolean = true
) {
    val context = LocalContext.current
    var currentStepIndex by remember { mutableIntStateOf(0) }

    val steps = remember(context) {
        listOf(
            WizardStep(
                title = context.getString(R.string.wizard_step1_title),
                subtitle = context.getString(R.string.wizard_step1_subtitle),
                icon = Icons.Default.Science,
                iconColor = Color(0xFF00E5FF),
                bulletPoints = listOf(
                    context.getString(R.string.wizard_step1_bullet1),
                    context.getString(R.string.wizard_step1_bullet2),
                    context.getString(R.string.wizard_step1_bullet3),
                    context.getString(R.string.wizard_step1_bullet4)
                ),
                industrialTip = context.getString(R.string.wizard_step1_tip),
                actionText = context.getString(R.string.wizard_action_print_charuco),
                onAction = { ctx ->
                    val file = CalibrationPatternGenerator.generateAndShareCalibrationPdf(ctx)
                    CalibrationPatternGenerator.openOrPrintPdf(ctx, file)
                }
            ),
            WizardStep(
                title = context.getString(R.string.wizard_step2_title),
                subtitle = context.getString(R.string.wizard_step2_subtitle),
                icon = Icons.Default.FlipCameraAndroid,
                iconColor = Color(0xFFFFB300),
                bulletPoints = listOf(
                    context.getString(R.string.wizard_step2_bullet1),
                    context.getString(R.string.wizard_step2_bullet2),
                    context.getString(R.string.wizard_step2_bullet3),
                    context.getString(R.string.wizard_step2_bullet4)
                ),
                industrialTip = context.getString(R.string.wizard_step2_tip)
            ),
            WizardStep(
                title = context.getString(R.string.wizard_step3_title),
                subtitle = context.getString(R.string.wizard_step3_subtitle),
                icon = Icons.Default.PrecisionManufacturing,
                iconColor = Color(0xFF76FF03),
                bulletPoints = listOf(
                    context.getString(R.string.wizard_step3_bullet1),
                    context.getString(R.string.wizard_step3_bullet2),
                    context.getString(R.string.wizard_step3_bullet3),
                    context.getString(R.string.wizard_step3_bullet4)
                ),
                industrialTip = context.getString(R.string.wizard_step3_tip)
            ),
            WizardStep(
                title = context.getString(R.string.wizard_step4_title),
                subtitle = context.getString(R.string.wizard_step4_subtitle),
                icon = Icons.Default.ReportProblem,
                iconColor = Color(0xFFFF1744),
                bulletPoints = listOf(
                    context.getString(R.string.wizard_step4_bullet1),
                    context.getString(R.string.wizard_step4_bullet2),
                    context.getString(R.string.wizard_step4_bullet3),
                    context.getString(R.string.wizard_step4_bullet4)
                ),
                industrialTip = context.getString(R.string.wizard_step4_tip)
            ),
            WizardStep(
                title = context.getString(R.string.wizard_step5_title),
                subtitle = context.getString(R.string.wizard_step5_subtitle),
                icon = Icons.Default.CheckCircle,
                iconColor = Color(0xFF29B6F6),
                bulletPoints = listOf(
                    context.getString(R.string.wizard_step5_bullet1),
                    context.getString(R.string.wizard_step5_bullet2),
                    context.getString(R.string.wizard_step5_bullet3),
                    context.getString(R.string.wizard_step5_bullet4)
                ),
                industrialTip = context.getString(R.string.wizard_step5_tip)
            ),
            WizardStep(
                title = context.getString(R.string.wizard_step6_title),
                subtitle = context.getString(R.string.wizard_step6_subtitle),
                icon = Icons.Default.AutoAwesome,
                iconColor = Color(0xFFE040FB),
                bulletPoints = listOf(
                    context.getString(R.string.wizard_step6_bullet1),
                    context.getString(R.string.wizard_step6_bullet2),
                    context.getString(R.string.wizard_step6_bullet3),
                    context.getString(R.string.wizard_step6_bullet4)
                ),
                industrialTip = context.getString(R.string.wizard_step6_tip)
            )
        )
    }

    val currentStep = steps[currentStepIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp)),
            color = if (isDarkMode) Color(0xFF101726) else Color(0xFFF8F9FA),
            border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF25334D) else Color(0xFFDEE2E6))
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = currentStep.iconColor.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = currentStep.icon,
                                    contentDescription = null,
                                    tint = currentStep.iconColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                stringResource(R.string.wizard_guide_title),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color(0xFF88A0C4) else Color(0xFF6C757D),
                                letterSpacing = 1.sp
                            )
                            Text(
                                stringResource(R.string.wizard_step_indicator, currentStepIndex + 1, steps.size),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDarkMode) Color.White else Color(0xFF212529)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = if (isDarkMode) Color.Gray else Color.DarkGray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Step Dots Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    steps.forEachIndexed { index, _ ->
                        val isSelected = index == currentStepIndex
                        val isPassed = index < currentStepIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .height(6.dp)
                                .width(if (isSelected) 24.dp else 10.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    when {
                                        isSelected -> currentStep.iconColor
                                        isPassed -> if (isDarkMode) Color(0xFF3B5278) else Color(0xFFADB5BD)
                                        else -> if (isDarkMode) Color(0xFF1E293B) else Color(0xFFE9ECEF)
                                    }
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Step Content
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "step_content"
                ) { step ->
                    Column {
                        Text(
                            text = step.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF1A1A1A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = step.subtitle,
                            fontSize = 13.sp,
                            color = currentStep.iconColor,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bullet Points
                        step.bulletPoints.forEach { point ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 6.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(step.iconColor)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = point,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = if (isDarkMode) Color(0xFFD1D7E0) else Color(0xFF333333)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Industrial Tip Card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkMode) Color(0xFF162133) else Color(0xFFEDF2F7)
                            ),
                            border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF233552) else Color(0xFFCBD5E0))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = step.iconColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = step.industrialTip,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = if (isDarkMode) Color(0xFFA0B3CC) else Color(0xFF4A5568)
                                )
                            }
                        }

                        // Action Button if available (e.g. Print ChArUco pattern)
                        if (step.actionText != null && step.onAction != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { step.onAction.invoke(context) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = step.iconColor
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Print,
                                    contentDescription = null,
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = step.actionText,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Bottom Navigation Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStepIndex > 0) {
                        OutlinedButton(
                            onClick = { currentStepIndex-- },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, if (isDarkMode) Color.Gray else Color.LightGray)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.wizard_btn_prev), fontSize = 13.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    if (currentStepIndex < steps.size - 1) {
                        Button(
                            onClick = { currentStepIndex++ },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDarkMode) Color(0xFF3366CC) else Color(0xFF1976D2)
                            )
                        ) {
                            Text(stringResource(R.string.wizard_btn_next), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(R.string.wizard_btn_finish), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
