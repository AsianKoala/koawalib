package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.gamepad.functionality.Button

@Suppress("unused")
class KButton(private val buttonState: () -> Boolean) : Button() {
    override fun invokeBoolean(): Boolean {
        return buttonState.invoke()
    }
}