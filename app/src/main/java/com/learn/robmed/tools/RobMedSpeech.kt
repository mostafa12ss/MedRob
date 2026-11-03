package com.learn.robmed.tools


object RobMedSpeech {

    fun welcome(speaker: SpeechEngine) {
        speaker.speak(
            "مرحباً بك. أنا المساعدة الطبية، وسأساعدك على تناول أدويتك في مواعيدها."
        )
    }

    fun askPatientToLookAtCamera(speaker: SpeechEngine) {
        speaker.speak(
            "من فضلك، انظر إلى الكاميرا للتحقق من هويتك."
        )
    }

    fun patientVerified(speaker: SpeechEngine) {
        speaker.speak(
            "تم التحقق من هويتك بنجاح. أهلاً بك."
        )
    }

    fun announceMedication(
        speaker: SpeechEngine,
        drugName: String,
        doseTime: String
    ) {
        speaker.speak(
            "حان الآن موعد دواء $drugName، في تمام الساعة $doseTime."
        )
    }

    fun askToTakeMedication(
        speaker: SpeechEngine,
        drugName: String
    ) {
        speaker.speak(
            "من فضلك، تناول دواء $drugName الآن."
        )
    }

    fun askToSwallow(speaker: SpeechEngine) {
        speaker.speak(
            "من فضلك، ابتلع الدواء."
        )
    }

    fun askToDrinkWater(speaker: SpeechEngine) {
        speaker.speak(
            "من فضلك، اشرب كمية مناسبة من الماء."
        )
    }

    fun doseCompleted(speaker: SpeechEngine) {
        speaker.speak(
            "أحسنت. تم تسجيل الجرعة بنجاح."
        )
    }

    fun nextDose(
        speaker: SpeechEngine,
        drugName: String,
        doseTime: String
    ) {
        speaker.speak(
            "موعد الدواء القادم هو $drugName، في تمام الساعة $doseTime."
        )
    }

    fun error(speaker: SpeechEngine) {
        speaker.speak(
            "عذراً، لم نتمكن من إتمام هذه الخطوة. من فضلك حاول مرة أخرى."
        )
    }
}