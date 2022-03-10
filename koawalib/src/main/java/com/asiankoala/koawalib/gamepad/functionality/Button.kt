package com.asiankoala.koawalib.gamepad.functionality

import com.asiankoala.koawalib.util.KBoolean
import com.asiankoala.koawalib.util.Periodic

interface Button : KBoolean, Periodic {
    val isPressed: Boolean
    val isToggled: Boolean
    val hasChanged: Boolean
    val lastState: Boolean

    val isJustPressed: Boolean
        get() = isPressed && hasChanged

    val isJustReleased: Boolean
        get() = !isPressed && hasChanged

    val isReleased: Boolean
        get() = !isPressed

    val isJustToggled: Boolean
        get() = isToggled && hasChanged && isPressed

    val isJustUntoggled: Boolean
        get() = !isToggled && hasChanged && isPressed

    val isUntoggled: Boolean
        get() = !isToggled
}
