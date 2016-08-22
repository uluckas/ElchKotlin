package de.musoft.elch.delegates

import android.content.SharedPreferences

/**
 * Created by luckas on 19.08.16.
 */

open class LongSharedPreferenceShadow(sharedPreferences: SharedPreferences, key: String, default: Long) :
        KeyValueShadow<Long>(key, sharedPreferences.getLong(key, default),
                { key, value -> sharedPreferences.edit().putLong(key, value).apply() }) {
}

