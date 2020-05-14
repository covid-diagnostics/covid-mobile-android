package com.example.coronadiagnosticapp.ui.fragments.recorder

import com.example.coronadiagnosticapp.R

object RecordingsFactory{
    fun getRecordings() = arrayOf(
        Recording(
            R.string.letsHearSound,
            R.string.soundA,
            R.string.soundA_like
        ),
        Recording(
            R.string.letsHearSound,
            R.string.soundE,
            R.string.soundE_like
        ),
        Recording(
            R.string.letsHear,
            R.string.count_1to10,
            R.string.can_use_he
        ),
        Recording(
            R.string.lastTimeLetsHear,
            R.string.cough3Times
        )
    )
}