package de.musoft.elch.delegates

import android.content.SharedPreferences

/**
 * Created by luckas on 8/19/16.
 */

open class BooleanSharedPreferenceShadow(sharedPreferences: SharedPreferences, key: String, default: Boolean) :
        KeyValueShadow<Boolean>(key, sharedPreferences.getBoolean(key, default),
                { key, value -> sharedPreferences.edit().putBoolean(key, value).apply() })
