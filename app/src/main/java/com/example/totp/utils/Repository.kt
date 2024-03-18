package com.example.totp.utils

import com.example.totp.bean.Verifier
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV

object Repository {
    private val mmkv = MMKV.defaultMMKV()
    private val gson = Gson()
    private var verifierListJson: String
        get() = mmkv.decodeString("verifierListJson") ?: "[]"
        set(value) {
            mmkv.encode("verifierListJson", value)
        }

    val verifierList: List<Verifier>
        get() = gson.fromJson(
            verifierListJson,
            object : TypeToken<List<Verifier>>() {}.type
        )

    fun updateVerifier(verifier: Verifier) {
        val index = verifierList.indexOfFirst {
            it.id == verifier.id
        }
        if (index < 0) {
            val list = verifierList.toMutableList().apply {
                add(verifier)
            }
            verifierListJson = gson.toJson(list)
        } else {
            val list = verifierList.toMutableList().apply {
                set(index, verifier)
            }
            verifierListJson = gson.toJson(list)
        }
    }

    fun deleteVerifier(verifier: Verifier) {
        val list = verifierList.toMutableList().apply {
            removeAll {
                it.id == verifier.id
            }
        }
        verifierListJson = gson.toJson(list)
    }

}