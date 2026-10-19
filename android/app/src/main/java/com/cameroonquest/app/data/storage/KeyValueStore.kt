package com.cameroonquest.app.data.storage

import android.content.SharedPreferences

/** Minimal key-value storage the repositories need; tests use an in-memory version. */
interface KeyValueStore {
    fun getInt(key: String, default: Int): Int

    fun getString(key: String): String?

    fun getBoolean(key: String, default: Boolean): Boolean

    fun edit(changes: Editor.() -> Unit)

    fun clear()

    interface Editor {
        fun putInt(key: String, value: Int)

        /** A null value removes the key. */
        fun putString(key: String, value: String?)

        fun putBoolean(key: String, value: Boolean)
    }
}

/** Adapter: exposes Android's SharedPreferences through the [KeyValueStore] interface. */
class SharedPreferencesStore(private val preferences: SharedPreferences) : KeyValueStore {
    override fun getInt(key: String, default: Int): Int = preferences.getInt(key, default)

    override fun getString(key: String): String? = preferences.getString(key, null)

    override fun getBoolean(key: String, default: Boolean): Boolean = preferences.getBoolean(key, default)

    override fun edit(changes: KeyValueStore.Editor.() -> Unit) {
        val editor = preferences.edit()
        val adapter = object : KeyValueStore.Editor {
            override fun putInt(key: String, value: Int) {
                editor.putInt(key, value)
            }

            override fun putString(key: String, value: String?) {
                if (value == null) editor.remove(key) else editor.putString(key, value)
            }

            override fun putBoolean(key: String, value: Boolean) {
                editor.putBoolean(key, value)
            }
        }
        adapter.changes()
        editor.apply()
    }

    override fun clear() {
        preferences.edit().clear().apply()
    }
}
