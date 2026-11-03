package com.learn.robmed.ui.theme.Screen

import androidx.annotation.DrawableRes
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.learn.robmed.tools.AndroidSpeechEngine
import com.learn.robmed.tools.RobMedSpeech
import com.learn.robmed.tools.SpeechEngine

// ───────────── الألوان ─────────────
private val DeepBlue = Color(0xFF35477A)
private val Teal = Color(0xFF4AA3BE)
private val CardBg = Color(0xFFE9F3F8)
private val FrameBlue = Color(0xFF7FD3F0)
private val SuccessGreen = Color(0xFF2FB344)
private val BottleBlue = Color(0xFF2E7BE0)

// ───────────── القطع المشتركة ─────────────

/** خلفية موحدة + اتجاه عربي (RTL) لكل الصفحات. radial = true للصفحات اللي نورها في النص. */
@Composable
fun ScreenBackground(radial: Boolean = false, content: @Composable BoxScope.() -> Unit) {
    val brush = if (radial) {
        Brush.radialGradient(listOf(Color(0xFF9BE8FA), Color(0xFF4FB3D9), DeepBlue))
    } else {
        Brush.linearGradient(
            colors = listOf(DeepBlue, Teal),
            start = Offset(0f, 0f),
            end = Offset(1400f, 900f)
        )
    }
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(Modifier
            .fillMaxSize()
            .background(brush), content = content)
    }
}

@Composable
fun ScreenTitle(text: String, color: Color = Color.White) {
    Text(
        text = text,
        color = color,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CardText(
    text: String,
    size: TextUnit = 16.sp,
    bold: Boolean = false,
    color: Color = DeepBlue,
    align: TextAlign = TextAlign.Center
) {
    Text(
        text = text,
        color = color,
        fontSize = size,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        textAlign = align
    )
}

/** الكارت الأبيض بإطار أزرق مزدوج زي التصميم. */
@Composable
fun FancyCard(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier.height(275.dp)
            .border(5.dp, FrameBlue, RoundedCornerShape(14.dp))
            .padding(5.dp)
            .border(2.dp, DeepBlue.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .background(CardBg, RoundedCornerShape(10.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = horizontalAlignment,
            verticalArrangement = Arrangement.Center,
            content = content
        )
    }
}

/** إطار صورة بنفس شكل الكارت. لو مفيش صورة بيظهر مربع رمادي مؤقت. */
@Composable
fun PhotoFrame(@DrawableRes image: Int?, modifier: Modifier = Modifier) {
    Box(
        modifier
            .border(5.dp, FrameBlue, RoundedCornerShape(14.dp))
            .padding(5.dp)
            .clip(RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (image != null) {
            Image(
                painter = painterResource(image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFFB8C7D1)),
                contentAlignment = Alignment.Center
            ) { CardText("صورة", color = Color.White) }
        }
    }
}

/** علامة صح خضراء داخل دايرة. */
@Composable
fun CheckBadge(diameter: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(diameter)) {
        val s = this.size.minDimension
        val stroke = s * 0.09f
        drawCircle(SuccessGreen, radius = s / 2 - stroke / 2, style = Stroke(stroke))
        val check = Path().apply {
            moveTo(s * 0.27f, s * 0.52f)
            lineTo(s * 0.44f, s * 0.68f)
            lineTo(s * 0.75f, s * 0.30f)
        }
        drawPath(
            check, SuccessGreen,
            style = Stroke(stroke * 1.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

/** أيقونة زجاجة دواء مرسومة بالكود. */
@Composable
fun MedicineBottleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier.size(90.dp)) {
        val w = size.width
        val h = size.height
        drawRoundRect(FrameBlue, Offset(w * 0.25f, 0f), Size(w * 0.5f, h * 0.14f), CornerRadius(8f))
        drawRoundRect(
            BottleBlue, Offset(w * 0.12f, h * 0.2f), Size(w * 0.76f, h * 0.78f),
            CornerRadius(w * 0.15f), style = Stroke(w * 0.07f)
        )
        val cx = w * 0.5f
        val cy = h * 0.58f
        val a = w * 0.17f
        drawLine(BottleBlue, Offset(cx - a, cy), Offset(cx + a, cy), strokeWidth = w * 0.1f)
        drawLine(BottleBlue, Offset(cx, cy - a), Offset(cx, cy + a), strokeWidth = w * 0.1f)
    }
}

// ───────────── 1) تم الوصول لسرير المريض ─────────────

@Composable
fun PatientArrivalContent(
    loading: Boolean,
    name: String?,
    room: String?,
    bed: String?,
    @DrawableRes bedimage: Int? = null
) {
    ScreenBackground {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ScreenTitle("تم الوصول الى سرير المريض")

            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PhotoFrame(bedimage, Modifier
                    .weight(1.2f)
                    .fillMaxHeight())
                FancyCard(Modifier.weight(1f)) {
                    if (loading) {
                        CircularProgressIndicator(color = DeepBlue)
                    } else {
                        CardText("اسم المريض : ${name.orEmpty()}", size = 25.sp, bold = true)
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            if (room != null) CardText("غرفة $room", size = 25.sp,bold = true)
                            CardText("سرير رقم ${bed.orEmpty()}", size = 25.sp,bold = true)
                        }
                    }
                }
            }

            ScreenTitle("جاري الاستعداد لتسليم الدواء")
        }
    }
}

// ───────────── 2) جارى التحقق من هوية المريض ─────────────


@Composable
fun PatientVerifiedContent(
    verified: Boolean,
    name: String?,
    room: String?,
    bed: String?,
    cameraPreview: PreviewView?,
    cameraHint: String = "ضع وجهك داخل الإطار",
    faceAligned: Boolean = false
) {
    ScreenBackground {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenTitle("جارى التحقق من هوية المريض")

            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // بيانات المريض
                FancyCard(
                    Modifier
                        .weight(1.2f)
                        .fillMaxHeight()
                ) {
                    if (verified) {
                        CheckBadge(56.dp)

                        Spacer(Modifier.height(8.dp))

                        CardText(
                            "تم التعرف على المريض",
                            size = 20.sp,
                            bold = true,
                            color = SuccessGreen
                        )

                        Spacer(Modifier.height(8.dp))

                        CardText(
                            "السيد: ${name.orEmpty()}",
                            size = 18.sp,
                            bold = true
                        )

                        Spacer(Modifier.height(8.dp))

                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            if (room != null) {
                                CardText("غرفة $room", bold = true)
                            }

                            CardText(
                                "سرير رقم ${bed.orEmpty()}",
                                bold = true
                            )
                        }

                    } else {
                        CircularProgressIndicator(
                            color = DeepBlue
                        )

                        Spacer(Modifier.height(12.dp))

                        CardText(
                            "جاري التحقق من هوية المريض...",
                            size = 18.sp,
                            bold = true
                        )

                        Spacer(Modifier.height(12.dp))

                        CardText(
                            cameraHint,
                            size = 16.sp,
                            color = if (faceAligned)
                                SuccessGreen
                            else DeepBlue
                        )
                    }
                }

                // الكاميرا الحية
                Box(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {

                    if (cameraPreview != null) {
                        AndroidView(
                            factory = { cameraPreview },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // إطار تحديد الوجه
                    Box(
                        Modifier
                            .fillMaxWidth(0.70f)
                            .fillMaxHeight(0.72f)
                            .border(
                                width = 4.dp,
                                color = if (faceAligned || verified)
                                    SuccessGreen
                                else Color.White,
                                shape = CircleShape
                            )
                    )

                    // التعليمات أسفل الكاميرا
                    Column(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(
                                Color.Black.copy(alpha = 0.65f)
                            )
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (verified)
                                "تم التحقق بنجاح"
                            else cameraHint,
                            color = if (faceAligned || verified)
                                Color(0xFF69E58A)
                            else Color.White,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}


// ───────────── 3) جارى التأكد من أخذ الدواء ─────────────

/** preview: لو عايز تحط فيديو الكاميرا الحقيقي بدل الصورة، مرره هنا. */
@Composable
fun MedicationCheckContent(
    @DrawableRes photo: Int? = null,
    status: String? = null,
    preview: (@Composable () -> Unit)? = null
) {
    ScreenBackground {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScreenTitle("جارى التأكد من أخذ الدواء")

            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FancyCard(Modifier
                    .weight(1.2f)
                    .fillMaxHeight()) {
                    CardText("يتم تحليل الفيديو باستخدام الذكاء الاصطناعي", size = 24.sp, bold = true)
                    if (status != null) {
                        Spacer(Modifier.height(12.dp))
                        CardText(status, size = 18.sp)
                    }
                }
                if (preview != null) {
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .border(5.dp, FrameBlue, RoundedCornerShape(14.dp))
                            .padding(5.dp)
                            .clip(RoundedCornerShape(10.dp))
                    ) { preview() }
                } else {
                    PhotoFrame(photo, Modifier
                        .weight(1f)
                        .fillMaxHeight())
                }
            }
        }
    }
}

// ───────────── 4) تم أخذ العلاج بنجاح ─────────────

@Composable
fun TreatmentSuccessContent(
    closing: String = "عفاك الله",
    @DrawableRes image: Int? = null
) {
    ScreenBackground(radial = true) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (image != null) {
                Image(painterResource(image), contentDescription = null, modifier = Modifier.size(96.dp))
            } else {
                CheckBadge(96.dp)
            }
            Spacer(Modifier.height(12.dp))
            CardText("تم أخذ العلاج بنجاح", size = 30.sp, bold = true)
            Spacer(Modifier.height(6.dp))
            CardText("بناءً علي تحليل الفيديو", size = 16.sp, bold = true)
            Spacer(Modifier.height(18.dp))
            Box(
                Modifier
                    .background(Color(0xFFBDEBFA), RoundedCornerShape(50))
                    .padding(horizontal = 36.dp, vertical = 12.dp)
            ) { CardText(closing, size = 24.sp, bold = true) }
        }
    }
}

// ───────────── 5) الموعد القادم ─────────────


@Composable

fun NextDoseContent2(
    drugName: String,
    whenText: String,
    speaker: SpeechEngine,
    @DrawableRes image: Int? = null
) {
    LaunchedEffect(drugName, whenText) {
        RobMedSpeech.nextDose(
            speaker = speaker,
            drugName = drugName,
            doseTime = whenText
        )
    }

    ScreenBackground(radial = true) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            ScreenTitle("الموعد القادم", color = DeepBlue)

            FancyCard(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        CardText(
                            drugName,
                            size = 36.sp,
                            bold = true,
                            align = TextAlign.Start
                        )

                        Spacer(Modifier.height(8.dp))

                        CardText(
                            whenText,
                            size = 28.sp,
                            align = TextAlign.Start
                        )
                    }

                    if (image != null) {
                        Image(
                            painterResource(image),
                            contentDescription = null,
                            modifier = Modifier.size(90.dp)
                        )
                    } else {
                        MedicineBottleIcon()
                    }
                }
            }

            ScreenTitle("ستذكرك في الوقت المحدد")
        }
    }
}
// ───────────── Previews ─────────────
 @Composable
fun NextDoseContent1(
    drugName: String,
    speaker: SpeechEngine,
    image: Int? = null
) {
    ScreenBackground(radial = true) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(10.dp))

            FancyCard(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.Start
                    ) {
                        CardText(
                            drugName,
                            size = 45.sp,
                            bold = true,
                            align = TextAlign.Start
                        )

                        Spacer(Modifier.height(8.dp))
                    }

                    if (image != null) {
                        Image(
                            painterResource(image),
                            contentDescription = null,
                            modifier = Modifier.size(90.dp)
                        )
                    } else {
                        MedicineBottleIcon()
                    }
                }
            }
        }
    }

    LaunchedEffect(drugName) {
        RobMedSpeech.announceMedication(
            speaker = speaker,
            drugName = drugName,
            doseTime = "" // مرر الموعد الفعلي إذا كان مطلوبًا عرضه صوتيًا
        )
    }
}
private const val W = 800
private const val H = 360

@Preview(widthDp = W, heightDp = H)
@Composable
fun ArrivalPreview() = PatientArrivalContent(false, "مصطفى جبوشي", "333", "2")

@Preview(widthDp = W, heightDp = H)
@Composable
fun ArrivalLoadingPreview() = PatientArrivalContent(true, null, null, null)



@Preview(widthDp = W, heightDp = H)
@Composable
fun MedicationCheckPreview() = MedicationCheckContent()

@Preview(widthDp = W, heightDp = H)
@Composable
fun SuccessPreview() = TreatmentSuccessContent()

//@Preview(widthDp = W, heightDp = H)
//@Composable
//fun NextDosePreview() = NextDoseContent2("باراسيتامول 500 مجم", "غدا 10:00 صباحا")

@Preview(widthDp = W, heightDp = H)
@Composable
fun NextDosePreview1() {
    val speechEngine = AndroidSpeechEngine(LocalContext.current)
    NextDoseContent1(
    "باراسيتامول 500 مجم",
    speaker = speechEngine ,
    image = null
)
}

