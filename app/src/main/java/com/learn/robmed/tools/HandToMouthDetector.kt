package com.learn.robmed.tools


import android.graphics.PointF
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import kotlin.math.hypot

/** حالة الرصد الحالية. */
data class IntakeState(
    val handNear: Boolean = false,   // اليد قريبة من الفم دلوقتي
    val events: Int = 0,             // عدد حركات "اليد للفم" المكتملة
    val completed: Boolean = false,  // وصلنا للعدد المطلوب
    val ratio: Float? = null         // المسافة (يد↔فم) ÷ عرض الكتفين، للضبط والتجربة
)

/**
 * يرصد حركة اليد للفم من نقاط الجسم (ML Kit Pose).
 *
 * الحركة تتحسب لما اليد تفضل قريبة من الفم [minHoldMs] متواصلة،
 * وبعدها لازم تبعد عشان تتحسب حركة جديدة.
 * بيستخدم عتبتين (nearRatio / farRatio) عشان الحالة ما تتذبذبش.
 *
 * مهم: ده بيرصد *الحركة* بس، مش بيشوف الحباية ولا بيتأكد إنها اتبلعت.
 */
class HandToMouthDetector(
    private val nearRatio: Float = 0.55f,
    private val farRatio: Float = 0.80f,
    private val minHoldMs: Long = 400L,
    private val requiredEvents: Int = 1
) {
    private var near = false
    private var nearSince = -1L
    private var counted = false
    private var events = 0

    fun reset() {
        near = false; nearSince = -1L; counted = false; events = 0
    }

    /** نادِها مع كل إطار. pose = null لو مفيش جسم اتكتشف. */
    fun update(pose: Pose?, nowMs: Long): IntakeState {
        val ratio = pose?.let { handMouthRatio(it) }

        if (ratio == null) {
            // مفيش جسم/يد واضحة: نعتبرها بعيدة
            near = false; nearSince = -1L; counted = false
        } else {
            if (ratio < nearRatio) near = true
            else if (ratio > farRatio) near = false

            if (near) {
                if (nearSince < 0) nearSince = nowMs
                if (!counted && nowMs - nearSince >= minHoldMs) {
                    events++
                    counted = true
                }
            } else {
                nearSince = -1L
                counted = false
            }
        }
        return IntakeState(near, events, events >= requiredEvents, ratio)
    }

    // ───────────── حسابات النقاط ─────────────

    private fun handMouthRatio(pose: Pose): Float? {
        val mouth = mouthPoint(pose) ?: return null
        val scale = referenceScale(pose) ?: return null
        if (scale < 1f) return null

        val hands = listOfNotNull(
            pose.point(PoseLandmark.LEFT_INDEX) ?: pose.point(PoseLandmark.LEFT_WRIST),
            pose.point(PoseLandmark.RIGHT_INDEX) ?: pose.point(PoseLandmark.RIGHT_WRIST)
        )
        if (hands.isEmpty()) return null

        return hands.minOf { dist(it, mouth) } / scale
    }

    private fun mouthPoint(pose: Pose): PointF? {
        val l = pose.point(PoseLandmark.LEFT_MOUTH)
        val r = pose.point(PoseLandmark.RIGHT_MOUTH)
        return if (l != null && r != null) PointF((l.x + r.x) / 2f, (l.y + r.y) / 2f)
        else pose.point(PoseLandmark.NOSE)
    }

    /** مقياس يخلي النتيجة مش معتمدة على بعد المريض عن الكاميرا. */
    private fun referenceScale(pose: Pose): Float? {
        val ls = pose.point(PoseLandmark.LEFT_SHOULDER)
        val rs = pose.point(PoseLandmark.RIGHT_SHOULDER)
        if (ls != null && rs != null) return dist(ls, rs)

        val le = pose.point(PoseLandmark.LEFT_EAR)
        val re = pose.point(PoseLandmark.RIGHT_EAR)
        if (le != null && re != null) return dist(le, re) * 2.5f
        return null
    }

    private fun Pose.point(type: Int, minLikelihood: Float = 0.5f): PointF? {
        val l = getPoseLandmark(type) ?: return null
        return if (l.inFrameLikelihood >= minLikelihood) l.position else null
    }

    private fun dist(a: PointF, b: PointF): Float = hypot(a.x - b.x, a.y - b.y)
}