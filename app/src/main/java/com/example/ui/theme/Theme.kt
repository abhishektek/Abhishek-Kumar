package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = SweetMagentaPrimary,
  secondary = GoldenCoinSecondary,
  tertiary = SoftMintTertiary,
  background = CaramelDarkBackground,
  surface = PlumDarkSurface,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onTertiary = Color.Black,
  onBackground = DarkText,
  onSurface = DarkText
)

private val LightColorScheme = lightColorScheme(
  primary = SweetMagentaPrimary,
  secondary = GoldenCoinSecondary,
  tertiary = SoftMintTertiary,
  background = CaramelDarkBackground,
  surface = PlumDarkSurface,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onTertiary = Color.Black,
  onBackground = DarkText,
  onSurface = DarkText
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Disable dynamic colors to enforce the sweet candy arcade vibe brand identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme // keep brand identity uniform

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
