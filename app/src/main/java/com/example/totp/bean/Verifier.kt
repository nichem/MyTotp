package com.example.totp.bean

import java.util.UUID

data class Verifier(
    var id: String,
    var name: String,
    var secret: String
)
