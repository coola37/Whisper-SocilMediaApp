package com.example.anew.model

data class ChatChannel(
    var senderId: String? = null,
    var recevierId: String? = null,
    var receiverProfileImg: String? = null,
    var receiverUsername: String? = null,
    var messages: List<Messages>? = null,
    var lastMessage: String? = null
)
