package com.example.totp.utils.totp

import android.util.Log
import com.example.totp.utils.totp.Base32String.DecodingException
import java.security.GeneralSecurityException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Created by zhangyf on 2017/3/8.
 */
object TotpUtil {
    private const val PIN_LENGTH = 6
    private const val REFLECTIVE_PIN_LENGTH = 9

    /**
     * 生成6位数的手机令牌号
     *
     * @return
     */
    fun generate(seed: String?): String {
        return try {
            // 加上与服务器的时间差，再计算结果
            computePin(
                seed, CountUtils.getValueAtTime(
                    CountUtils.millisToSeconds(
                        CountUtils.currentTimeMillis()
                    )
                ), null
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun getLeftTime(): Long {
        val second =
            CountUtils.millisToSeconds(CountUtils.currentTimeMillis())
        return CountUtils.mTimeStep - second % CountUtils.mTimeStep
    }

    /**
     * Computes the one-time PIN given the secret key.
     *
     * @param secret    the secret key
     * @param otp_state current token state (counter or time-interval)
     * @param challenge optional challenge bytes to include when computing passcode.
     * @return the PIN
     * @author zhangyf
     */
    @Throws(OtpSourceException::class)
    private fun computePin(secret: String?, otp_state: Long, challenge: ByteArray?): String {
        if (secret == null || secret.length == 0) {
            throw OtpSourceException("Null or empty secret")
        }
        return try {
            val signer = getSigningOracle(secret)
            val pcg = PasscodeGenerator(
                signer,
                if (challenge == null) PIN_LENGTH else REFLECTIVE_PIN_LENGTH
            )
            if (challenge == null) pcg.generateResponseCode(otp_state) else pcg.generateResponseCode(
                otp_state,
                challenge
            )
        } catch (e: GeneralSecurityException) {
            throw OtpSourceException(
                "Crypto failure",
                e
            )
        }
    }

    private fun getSigningOracle(secret: String): PasscodeGenerator.Signer? {
        try {
            val keyBytes = decodeKey(secret)
            val mac = Mac.getInstance("HMACSHA1")
            mac.init(SecretKeySpec(keyBytes, ""))

            // Create a signer object out of the standard Java MAC
            // implementation.
            return PasscodeGenerator.Signer { data -> mac.doFinal(data) }
        } catch (error: DecodingException) {
            Log.e("Mlog", error.message!!)
        } catch (error: NoSuchAlgorithmException) {
            Log.e("Mlog", error.message!!)
        } catch (error: InvalidKeyException) {
            Log.e("Mlog", error.message!!)
        }
        return null
    }

    @Throws(DecodingException::class)
    private fun decodeKey(secret: String): ByteArray {
        return Base32String.decode(secret)
    }
}