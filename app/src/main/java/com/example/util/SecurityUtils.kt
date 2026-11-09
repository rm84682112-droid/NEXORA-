package com.example.util

import java.security.MessageDigest

object SecurityUtils {
    fun hashPin(pin: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            pin.hashCode().toString()
        }
    }

    fun verifyPin(pin: String, hash: String): Boolean {
        if (hash.isEmpty()) return false
        return hashPin(pin) == hash
    }

    fun isValidMozambiquePhone(phone: String): Boolean {
        val clean = phone.replace("[^0-9]".toRegex(), "")
        val numberPart = if (clean.startsWith("258")) clean.substring(3) else clean
        if (numberPart.length != 9) return false
        if (numberPart[0] != '8') return false
        return numberPart[1] in listOf('2', '3', '4', '5', '6', '7')
    }

    fun formatMozambiquePhone(phone: String): String {
        val clean = phone.replace("[^0-9]".toRegex(), "")
        val numberPart = if (clean.startsWith("258")) clean.substring(3) else clean
        if (numberPart.length == 9) {
            return "+258 ${numberPart.substring(0, 2)} ${numberPart.substring(2, 5)} ${numberPart.substring(5)}"
        }
        return phone
    }

    fun generateRecoveryCodes(): List<String> {
        val codes = mutableListOf<String>()
        val chars = "0123456789"
        repeat(5) {
            val num = (1..4).map { chars.random() }.joinToString("")
            codes.add("NX-$num")
        }
        return codes
    }
}
