package com.example.totp.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.graphics.scaleMatrix
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.MutableLiveData
import com.example.totp.bean.Verifier
import com.example.totp.databinding.ActivityVerifierBinding
import com.example.totp.utils.Repository
import com.example.totp.utils.DialogUtil.showAlertDialog
import com.example.totp.utils.totp.Base32String
import com.google.gson.Gson
import java.util.UUID

class VerifierActivity : AppCompatActivity() {
    private val binding: ActivityVerifierBinding by lazy {
        ActivityVerifierBinding.inflate(layoutInflater)
    }

    private var verifier = MutableLiveData(
        Verifier(UUID.randomUUID().toString(), "", "")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val json = intent.getStringExtra(EXTRA_VERIFIER)
        if (json != null) verifier.value = Gson().fromJson(json, Verifier::class.java)
        verifier.observe(this) {
            binding.etName.setText(it.name)
            binding.etSecret.setText(it.secret)
        }

        binding.etName.addTextChangedListener {
            verifier.value?.name = it.toString()
        }

        binding.etSecret.addTextChangedListener {
            verifier.value?.secret = it.toString().uppercase()
        }

        binding.btnSave.setOnClickListener {
            if (secretIsEmpty()) Toast.makeText(
                this@VerifierActivity,
                "密钥不能为空",
                Toast.LENGTH_SHORT
            ).show()
            else if (!secretIsValid()) Toast.makeText(
                this@VerifierActivity,
                "密钥含有不合法字符",
                Toast.LENGTH_SHORT
            ).show()
            else showAlertDialog("是否保存？") {
                Repository.updateVerifier(verifier.value!!)
                finish()
            }
        }
    }

    private fun secretIsEmpty(): Boolean {
        val secret = verifier.value?.secret ?: return true
        return secret.isEmpty()
    }

    private fun secretIsValid(): Boolean {
        val secret = verifier.value?.secret ?: return false
        secret.forEach {
            if (it.uppercase() !in Base32String.KEYS) {
                return false
            }
        }
        return true
    }

    companion object {
        const val EXTRA_VERIFIER = "verifier"
        fun start(context: Context, verifier: Verifier?) {
            context.startActivity(Intent(context, VerifierActivity::class.java).apply {
                if (verifier != null)
                    putExtra(EXTRA_VERIFIER, Gson().toJson(verifier))
            })
        }
    }
}