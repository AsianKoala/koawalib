package com.asiankoala.koawalib.command.commands

import com.asiankoala.koawalib.gamepad.KGamepad

class RumbleCmd(gamepad: KGamepad, ms: Int) : InstantCmd({ gamepad.rumble(ms) })
