package de.musoft.elch.delegates

interface KeyValueStore<K, V> {
    fun read(key: K, default: V) : V
    fun write(key: K, value: V)
}