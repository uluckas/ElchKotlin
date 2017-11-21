package de.musoft.kotlinutils.events

typealias onPropertyChanged<P> = (oldValue: P, newValue: P) -> Unit

class PropertyChangeEvent<T> : ArrayList<onPropertyChanged<T>>() {

    fun fireChange(oldValue: T, newValue: T) {
        forEach { listener ->
            listener(oldValue, newValue)
        }
    }

}
