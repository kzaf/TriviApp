package com.example.triviapp.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.triviapp.R

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private val HandwrittenFont = FontFamily(
    Font(R.font.handwritten_font, FontWeight.Normal),
)

private val AppTypography = androidx.compose.material3.Typography(
    titleLarge = TextStyle(
        fontFamily = HandwrittenFont,
        fontSize = 30.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    ),
    titleMedium = TextStyle(
        fontFamily = HandwrittenFont,
        fontSize = 26.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Black
    ),
    bodyLarge = TextStyle(
        fontFamily = HandwrittenFont,
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Black
    ),
    bodyMedium = TextStyle(
        fontFamily = HandwrittenFont,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        color = Color.Black
    )
)

@Composable
fun TriviAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}