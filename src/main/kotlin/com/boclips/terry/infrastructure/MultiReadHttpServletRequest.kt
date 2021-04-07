package com.boclips.terry.infrastructure

import org.apache.tomcat.util.http.fileupload.IOUtils
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStreamReader
import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

// Adapted from https://stackoverflow.com/a/30748533
class MultiReadHttpServletRequest(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
    private var cachedBytes: ByteArrayOutputStream? = null

    override fun getInputStream(): ServletInputStream {
        if (cachedBytes == null)
            cacheInputStream()

        return CachedServletInputStream()
    }

    override fun getReader(): BufferedReader {
        return BufferedReader(InputStreamReader(inputStream))
    }

    private fun cacheInputStream() {
        cachedBytes = ByteArrayOutputStream()
        IOUtils.copy(super.getInputStream(), cachedBytes)
    }

    inner class CachedServletInputStream : ServletInputStream() {
        private val input = ByteArrayInputStream(cachedBytes!!.toByteArray())

        override fun isFinished(): Boolean {
            return input.available() == 0
        }

        override fun isReady(): Boolean {
            return true
        }

        override fun setReadListener(listener: ReadListener) {
            TODO()
        }

        @Throws(IOException::class)
        override fun read(): Int {
            return input.read()
        }
    }
}
