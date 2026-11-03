package com.learn.robmed.ui.theme.Screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learn.robmed.R
import com.learn.robmed.tools.AndroidSpeechEngine
import com.learn.robmed.tools.RobMedSpeech
import com.learn.robmed.tools.SpeechEngine
import com.learn.robmed.ui.theme.MainColor
import com.learn.robmed.ui.theme.RobMedTheme
import com.learn.robmed.ui.theme.alberb
import com.learn.robmed.ui.theme.alberm
import com.learn.robmed.ui.theme.albersb
import com.learn.robmed.ui.theme.second
import com.learn.robmed.ui.theme.support.TypewriterText
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(speaker: SpeechEngine, onFinished: () -> Unit = {}) {
    var step by remember { mutableIntStateOf(0) }



    LaunchedEffect(Unit) {
        RobMedSpeech.welcome(speaker)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(MainColor, second),
                    start = Offset(0f, 800f),
                    end = Offset(800f, 0f)
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Image(
                painter = painterResource(R.drawable.first),
                contentDescription = "Logo",
                modifier = Modifier.fillMaxHeight(),
                contentScale = ContentScale.FillHeight
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // تم استخدام التطويل (ـ) بين الحروف لمد الكلمة أفقياً وتغطية عرض أكبر من الجملة التي تحتها
                TypewriterText(
                    text = "مــرحــبـاً", // أو "مـــرحــبـاً" للحصول على عرض أكبر حسب رغبتك
                    style = TextStyle(
                        fontSize = 55.sp,
                        fontWeight = FontWeight.W900,
                        fontFamily = albersb,
                        color = Color(0xFF35477A)
                    ),
                    onFinished = { step = 1 }
                )

                if (step >= 1) {
                    TypewriterText(
                        text = "أنا الروبوت الطبي",
                        style = TextStyle(
                            fontSize = 28.sp,
                            fontFamily = alberb,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF35477A)
                        ),
                        onFinished = { step = 2 }
                    )
                }
                if (step >= 2) {
                    TypewriterText(
                        text = "سأقوم بتوصيل دوائك في الوقت المحدد",
                        Modifier.padding(bottom = 20.dp),
                        style = TextStyle(fontSize = 18.sp, fontFamily = alberm,  fontWeight = FontWeight.SemiBold,color = Color(0xFF35477A)),
                        onFinished = { step = 3 }
                    )
                }
                if (step >= 3) {
                    TypewriterText(
                        text = "★ مـعـاً من أجـل صـحـتـك ★",
                        style = TextStyle(fontSize = 16.sp, fontFamily = alberb, fontWeight = FontWeight.W900, color = Color(0xffced1d3)),
                        onFinished = { step = 4 }
                    )
                }

                if (step >= 4) {
                    LaunchedEffect(Unit) {
                        delay(6000L)
                        onFinished()
                    }
                }
            }
        }
    }
}

//@Preview(
//    showBackground = true,
//    widthDp = 800,
//    heightDp = 360
//)
//@Composable
//fun HomeScreenPreview() {
//    RobMedTheme {
//        WelcomeScreen(
//            speaker = TODO(),
//            onFinished = TODO()
//        )
//    }
//}