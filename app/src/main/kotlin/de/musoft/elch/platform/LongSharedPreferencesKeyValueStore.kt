package de.musoft.elch.platform

import android.content.Context
import android.util.Log

/**
 * Created by ulu on 31/03/17.
 */

class LongSharedPreferencesKeyValueStore(context: Context, name: String) :
        SharedPreferencesKeyValueStoreBase<Long>(context, name) {

    override fun read(key: String, default: Long): Long {
        val value = sharedPreferences.getLong(key, default)
        Log.d("Prefs", "read '$key' ($default) -> '$value'")
        return value
    }

    override fun write(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
        Log.d("Prefs", "write '$key', '$value'")
    }
}
