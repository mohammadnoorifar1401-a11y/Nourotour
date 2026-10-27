package com.yadyar.app.security

import android.content.Context
import java.security.MessageDigest

object AppLock {
    private const val PREFS = "mono_security"
    private const val KEY_HASH = "credential_hash"
    private const val KEY_MODE = "credential_mode"

    fun isEnabled(context: Context): Boolean = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).contains(KEY_HASH)
    fun isPattern(context: Context): Boolean = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_MODE, "pin") == "pattern"
    fun setPin(context: Context, pin: String) = save(context, pin, "pin")
    fun setPattern(context: Context, pattern: String) = save(context, pattern, "pattern")
    private fun save(context: Context, credential: String, mode: String) {
        val hash = MessageDigest.getInstance("SHA-256").digest(credential.toByteArray()).joinToString("") { "%02x".format(it) }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_HASH, hash).putString(KEY_MODE, mode).apply()
    }
    fun removePin(context: Context) { context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().remove(KEY_HASH).remove(KEY_MODE).apply() }
    fun verify(context: Context, credential: String): Boolean {
        val saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_HASH, null) ?: return true
        val candidate = MessageDigest.getInstance("SHA-256").digest(credential.toByteArray()).joinToString("") { "%02x".format(it) }
        return MessageDigest.isEqual(saved.toByteArray(), candidate.toByteArray())
    }
}
