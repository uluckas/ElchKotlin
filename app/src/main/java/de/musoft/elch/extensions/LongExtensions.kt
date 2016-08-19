package de.musoft.elch.extensions

import java.util.concurrent.TimeUnit

/**
 * Created by luckas on 19.08.16.
 */

fun Long.msToS(): Long {
    return TimeUnit.SECONDS.convert(this, TimeUnit.MILLISECONDS)
}

fun Long.mToMs(): Long {
    return TimeUnit.MILLISECONDS.convert(this, TimeUnit.MINUTES)
}

fun Long.sToM(): Long {
    return TimeUnit.MINUTES.convert(this, TimeUnit.SECONDS)
}

fun Long.mToS(): Long {
    return TimeUnit.SECONDS.convert(this, TimeUnit.MINUTES)
}

