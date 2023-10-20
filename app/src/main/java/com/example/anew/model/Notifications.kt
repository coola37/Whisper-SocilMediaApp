package com.example.anew.model

data class Notifications(
    var targetUser: String? = null,
    var title: String? = null,
    var body:String? = null,
    var postId:String? = null,
    var commentId:String? = null,
    var senderProfileImg:String? = null,
    var senderUsername:String? = null
)
