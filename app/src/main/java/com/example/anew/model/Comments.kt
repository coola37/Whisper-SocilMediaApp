package com.example.anew.model

data class Comments (
    var commentsID: String? = null,
    var senderID: String? = null,
    var postID: String? = null,
    var senderUsername: String? = null,
    var senderProfileImg: String? = null,
    var likeCounts: Int? = 0,
    var likeUsers: List<String> = emptyList(),
    var commentsText: String? = null,
    var date: String? = null
) {
    constructor() : this(null, null, null, null, null, 0, emptyList(), null, null)
}
