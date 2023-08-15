package com.example.anew.model

data class UserDetails(
    var name: String? = null,
    var bio: String? = null,
    var followers: Int? = null,
    var followed: Int? = null,
    var profileImg: String? = null
)
