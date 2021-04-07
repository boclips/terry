package com.boclips.terry.infrastructure.outgoing.videos

import java.net.MalformedURLException
import java.net.URL

fun extractKalturaPlaybackId(url: String?): String? =
    url?.let { parsedUrl ->
        try {
            URL(parsedUrl)
        } catch (e: MalformedURLException) {
            null
        }
            ?.path
            ?.split("/")
            ?.dropWhile { it != "entryId" }
            ?.drop(1)
            ?.firstOrNull()
    }
