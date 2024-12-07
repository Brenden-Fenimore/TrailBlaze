package com.byteforce.trailblaze.ui.profile

data class Friends(
    val userId: String = "",
    val username: String = "",
    val profileImageUrl: String? = "",
    val isPrivateAccount: Boolean,
    val watcherVisible: Boolean
)