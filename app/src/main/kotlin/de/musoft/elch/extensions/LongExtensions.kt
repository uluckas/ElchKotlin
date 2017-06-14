package de.musoft.elch.extensions

import java.util.concurrent.TimeUnit

fun Long.mToS(): Long {
    return TimeUnit.SECONDS.convert(this, TimeUnit.MINUTES)
}

