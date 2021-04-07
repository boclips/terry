package com.boclips.terry.infrastructure

import com.boclips.terry.Fake

class FakeClock : Fake, Clock {
    var nextTime: Long? = null

    init {
        reset()
    }

    override fun reset(): Fake = this
        .also { nextTime = System.currentTimeMillis() / 1000 }

    override fun read(): Long = nextTime!!
}
