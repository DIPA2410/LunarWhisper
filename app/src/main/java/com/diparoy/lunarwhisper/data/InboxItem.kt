package com.diparoy.lunarwhisper.data

data class InboxItem(
    val message: String? = null,
    val receiverId: String? = null,
    val senderId: String? = null,
    val timestamp: String? = null,
    var senderName: String? = null,
    var senderImageUri: String? = null
)
