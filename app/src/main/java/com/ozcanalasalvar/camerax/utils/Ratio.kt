package com.ozcanalasalvar.camerax.utils

fun Int.getDimensionRatioString(portraitMode: Boolean): String {
    val ratio = when (this) {
        1 -> Pair(0, 0)
        0 -> Pair(4, 3)
        else -> throw UnsupportedOperationException()
    }

    return if (portraitMode) "V,${ratio.second}:${ratio.first}"
    else "H,${ratio.first}:${ratio.second}"
}

fun Int.getAspectRationString(portraitMode: Boolean): String {
    val ratio = when (this) {
        1 -> Pair(16, 9)
        0 -> Pair(4, 3)
        else -> throw UnsupportedOperationException()
    }

    return if (portraitMode) "${ratio.second}:${ratio.first}"
    else "${ratio.first}:${ratio.second}"
}