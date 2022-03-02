package com.asiankoala.koawalib.gamepad.functionality

import com.asiankoala.koawalib.util.KBoolean
import com.asiankoala.koawalib.util.Periodic

interface Button : KBoolean, Periodic {
    val isPressed: Boolean
    val isToggled: Boolean
    val recentAction: Boolean
    val pastState: Boolean

    val isJustPressed: Boolean
        get() = isPressed && recentAction

    val isJustReleased: Boolean
        get() = !isPressed && recentAction

    val isReleased: Boolean
        get() = !isPressed

    val isJustToggled: Boolean
        get() = isToggled && recentAction && isPressed

    val isJustUntoggled: Boolean
        get() = !isToggled && recentAction && isPressed

    val isUntoggled: Boolean
        get() = !isToggled
}
