package com.diparoy.lunarwhisper.data

data class ClickedUser(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val profileBackgroundImage: String = "",
    val imageUri: String = "",
    val fbLink: String? = null,
    val instaLink: String? = null,
    val bio: String? = null,
    val details: String? = null
)

