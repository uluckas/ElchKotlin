package de.musoft.elch.delegates

import kotlin.reflect.KProperty

/**
 * Created by luckas on 19.08.16.
 */

open class KeyValueShadow<T>(val key: String, default: T, valueReader: (String, T) -> T, val valueWriter: (String, T) -> Unit) {

    private var backingField: T = valueReader(key, default)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return backingField
    }

    open operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        backingField = value
        valueWriter(key, value)
    }
}
