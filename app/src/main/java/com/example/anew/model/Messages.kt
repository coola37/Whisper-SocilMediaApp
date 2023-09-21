package com.example.anew.model

data class Messages(
    var messageId: String? = null,
    var senderId: String? = null,
    var recevierId: String? = null,
    var msgText: String? = null,
    var date: String? = null,
    var senderUsername: String? = null,
    var senderProfileImg: String? = null
)
