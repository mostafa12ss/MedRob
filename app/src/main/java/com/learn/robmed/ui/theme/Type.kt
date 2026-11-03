package com.learn.robmed.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.learn.robmed.R

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ))
    val PastoneFontFamily = FontFamily(
        Font(R.font.parastoo__variablefont_wght, FontWeight.Normal)
    )
val alberm= FontFamily(
    Font(R.font.abhayalibre_medium)
)
val alberb= FontFamily(
    Font(R.font.abhayalibre_bold)
)
val albersb= FontFamily(
    Font(R.font.abhayalibre_semibold)
)
val alberr= FontFamily(
    Font(R.font.abhayalibre_regular)
)
val albereb= FontFamily(
    Font(R.font.abhayalibre_extrabold)
)
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
