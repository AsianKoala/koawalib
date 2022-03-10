package com.asiankoala.koawalib.gamepad

import com.asiankoala.koawalib.gamepad.functionality.CommandButton

class KButton(private val buttonState: () -> Boolean) : CommandButton() {
    override fun invokeBoolean(): Boolean {
        return buttonState.invoke()
    }
}
