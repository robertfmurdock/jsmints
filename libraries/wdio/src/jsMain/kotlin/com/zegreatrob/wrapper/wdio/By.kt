package com.zegreatrob.wrapper.wdio

object By {
    fun className(className: String): String = ".$className"
    fun id(id: String): String = "#$id"
}
