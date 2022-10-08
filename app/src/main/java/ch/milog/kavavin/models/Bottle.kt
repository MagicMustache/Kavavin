package ch.milog.kavavin.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Bottle(
    val id: String? = null,
    val type: Long? = null,
    val name: String? = null,
    val country: String? = null,
    val region: String? = null,
    val year: Long? = null,
    val grape: String? = null,
    val price: Long? = null,
    val producer: String? = null,
    var quantity: Long? = null,
    var userId: String? = null
) : Parcelable
