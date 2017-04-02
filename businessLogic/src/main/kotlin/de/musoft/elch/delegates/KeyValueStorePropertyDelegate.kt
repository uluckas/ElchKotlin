package de.musoft.elch.delegates

import kotlin.reflect.KProperty

/**
 * Created by luckas on 19.08.16.
 */

open class KeyValueStorePropertyDelegate<K, V>(val keyValueStore: KeyValueStore<K, V>,
                                               val key: K,
                                               default: V) {

    private var backingField: V = keyValueStore.read(key, default)

    operator fun getValue(thisRef: Any?, property: KProperty<*>): V {
        return backingField
    }

    open operator fun setValue(thisRef: Any?, property: KProperty<*>, value: V) {
        backingField = value
        keyValueStore.write(key, value)
    }
}
