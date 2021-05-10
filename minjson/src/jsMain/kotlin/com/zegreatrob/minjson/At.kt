package com.zegreatrob.minjson

import kotlin.js.Json

fun <T> Json.at(path: String): T? = path.split('/')
    .filterNot(String::isEmpty)
    .fold<String, Json?>(this) { accumulator, value -> accumulator?.get(value).unsafeCast<Json?>() }.unsafeCast<T?>()
