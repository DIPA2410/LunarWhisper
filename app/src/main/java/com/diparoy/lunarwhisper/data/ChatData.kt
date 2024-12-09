package com.diparoy.lunarwhisper.data

data class ChatData(
    val chatPartnerId: String = "",
    var name: String = "",
    var imageUri: String = "",
    val message: String = "",
    val timestamp: String = "",
    var unseenMessageCount: Int
)



