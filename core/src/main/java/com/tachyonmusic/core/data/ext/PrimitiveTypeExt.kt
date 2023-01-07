package com.tachyonmusic.core.data.ext

import java.security.InvalidParameterException

fun Boolean.toInt() = if (this) 1 else 0
fun Int.toBoolean() =
    if (this == 0) false
    else if (this > 0) true
    else throw InvalidParameterException("Value $this invalid for conversion to boolean")