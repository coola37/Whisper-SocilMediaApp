package com.example.anew.model

data class Posts(
    var senderID: String? = null,
    var senderImg: String? = null,
    var text: String? = null,
    var postImg: String? = null,
    var date: String? = null,
    var like: Int? = null
)
