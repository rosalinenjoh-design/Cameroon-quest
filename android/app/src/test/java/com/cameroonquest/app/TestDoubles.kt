package com.cameroonquest.app

import com.cameroonquest.app.data.storage.KeyValueStore
import kotlin.random.Random

/** Random source that always returns the same value, to make AI and aiming decisions predictable. */
class FixedRandom(private val value: Double) : Random() {
    override fun nextBits(bitCount: Int): Int = 0

    override fun nextDouble(): Double = value
}

/** In-memory replacement for SharedPreferences, so repositories run in plain JVM tests. */
class InMemoryStore : KeyValueStore {
    private val values = mutableMapOf<String, Any>()

    override fun getInt(key: String, default: Int): Int = values[key] as? Int ?: default

    override fun getString(key: String): String? = values[key] as? String

    override fun getBoolean(key: String, default: Boolean): Boolean = values[key] as? Boolean ?: default

    override fun edit(changes: KeyValueStore.Editor.() -> Unit) {
        val editor = object : KeyValueStore.Editor {
            override fun putInt(key: String, value: Int) {
                values[key] = value
            }

            override fun putString(key: String, value: String?) {
                if (value == null) values.remove(key) else values[key] = value
            }

            override fun putBoolean(key: String, value: Boolean) {
                values[key] = value
            }
        }
        editor.changes()
    }

    override fun clear() {
        values.clear()
    }
}
