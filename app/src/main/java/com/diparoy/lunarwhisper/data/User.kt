package com.diparoy.lunarwhisper.data

class  User {
    var uid: String? = null
    var name: String? = null
    var email: String? = null
    var imageUri: String? = null
    var status: String? = null
    var profileBackgroundImage: String? = null
    var fbLink: String? = null
    var instaLink: String? = null
    var userType: String? = null

    constructor() {

    }

    constructor(
        uid: String?,
        name: String?,
        email: String?,
        imageUri: String?,
        status: String?,
        fbLink: String?,
        instaLink: String?,
        profileBackgroundImage: String?,
        userType: String?

    ) {
        this.uid = uid
        this.name = name
        this.email = email
        this.imageUri = imageUri
        this.status = status
        this.profileBackgroundImage = profileBackgroundImage
        this.fbLink = fbLink
        this.instaLink = instaLink
        this.userType = userType
    }
}



