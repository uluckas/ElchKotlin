package de.musoft.elch.platform

/**
 * Created by ulu on 31/03/17.
 */
interface TimeSource {
    fun getTimeMs(): Long
}