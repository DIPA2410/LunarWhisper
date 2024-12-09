package com.diparoy.lunarwhisper.data

data class StarredChatroom(
    val senderUid: String,
    val chatPartnerUid: String,
    val chatPartnerName: String,
    val chatPartnerImageUri: String,
    val latestMessage: String,
    val timestamp: String,
)

