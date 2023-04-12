package me.leepsky.esftp

import me.leepsky.esftp.input.ESftpInputReader
import me.leepsky.esftp.input.SftpInputReader
import me.leepsky.esftp.output.ESftpOutputWriter
import me.leepsky.esftp.output.SftpOutputWriter
import me.leepsky.esftp.processing.ESftpPacketProcessor
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback
import org.apache.sshd.server.channel.ChannelSession
import org.apache.sshd.server.command.Command
import java.io.InputStream
import java.io.OutputStream

class ESftpSubsystem(
    private val channel: ChannelSession?
): Command, Runnable {

    private lateinit var out: SftpOutputWriter
    private lateinit var err: OutputStream
    private lateinit var inp: SftpInputReader
    private val processor: ESftpPacketProcessor = ESftpPacketProcessor()
    private var exitCallback: ExitCallback? = null

    private var destroyed = false

    override fun start(channel: ChannelSession?, env: Environment?) {
        Thread(this).start()
    }

    override fun destroy(channel: ChannelSession?) {
        destroyed = true
        exitCallback?.onExit(0)
    }

    override fun setInputStream(`in`: InputStream) {
        this.inp = ESftpInputReader()
        this.inp.setInputStream(`in`)
    }

    override fun setOutputStream(out: OutputStream?) {
        this.out = ESftpOutputWriter()
        this.out.setOutputStream(out)
    }

    override fun setErrorStream(err: OutputStream) {
        this.err = err
    }

    override fun setExitCallback(callback: ExitCallback?) {
        this.exitCallback = callback
    }

    override fun run() {
        while (!destroyed) {
            mainLoop()
        }
    }

    private fun writeError(str: String) {
        err.write(str.toByteArray())
        err.flush()
    }

    private fun mainLoop() {
        val request = inp.read()
        println("Received packet: $request")
        val response = processor.process(request)
        println("Sent packet: $response")
        out.write(response)
    }

}
