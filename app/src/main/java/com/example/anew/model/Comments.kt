package com.example.anew.model

data class Comments (
    var commentsID: String? = null,
    var senderID: String? = null,
    var postID: String? = null,
    var senderUsername: String? = null,
    var senderProfileImg: String? = null,
    var likeCounts: Int? = 0,
    var likeUsers: List<String>,
    var commentsText: String? = null
    )