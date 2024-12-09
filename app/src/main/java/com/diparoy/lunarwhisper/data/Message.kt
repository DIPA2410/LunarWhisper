package com.diparoy.lunarwhisper.data

class Message{
    var message: String? = null
    var senderId: String? = null
    var receiverId: String? = null
    var recipientUsername: String? = null
    var recipientImageUri: String? = null
    var timestamp: String? = null
    var type: String? = null
    //var seen: Boolean? = false

    constructor(){

    }

    constructor(message: String?, senderId: String, receiverId: String, recipientUsername: String, recipientImageUri: String, timestamp: String,  type: String){
        this.message = message
        this.senderId = senderId
        this.receiverId = receiverId
        this.recipientUsername =recipientUsername
        this.recipientImageUri = recipientImageUri
        this.timestamp = timestamp
        this.type = type
        //this.seen = seen

    }
}
