package me.leepsky.esftp

import me.leepsky.esftp.input.SftpInputReader
import me.leepsky.esftp.output.SftpOutputWriter
import me.leepsky.esftp.processing.SftpPacketProcessor
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback
import org.apache.sshd.server.channel.ChannelSession
import org.apache.sshd.server.command.Command
import java.io.InputStream
import java.io.OutputStream
import java.nio.BufferUnderflowException

class ESftpSubsystem(
    private val channel: ChannelSession?,
    private val inp: SftpInputReader,
    private val out: SftpOutputWriter,
    private val processor: SftpPacketProcessor
): Command, Runnable {

    private lateinit var err: OutputStream
    private lateinit var exitCallback: ExitCallback
    private var destroyed = false

    override fun start(channel: ChannelSession?, env: Environment?) {
        Thread(this).start()
    }

    override fun destroy(channel: ChannelSession?) {
        destroyed = true
        exitCallback.onExit(0)
    }

    override fun setInputStream(`in`: InputStream) {
        this.inp.setInputStream(`in`)
    }

    override fun setOutputStream(out: OutputStream) {
        this.out.setOutputStream(out)
    }

    override fun setErrorStream(err: OutputStream) {
        this.err = err
    }

    override fun setExitCallback(callback: ExitCallback) {
        this.exitCallback = callback
    }

    override fun run() {
        while (!destroyed) { mainLoop() }
    }

    private fun mainLoop() {
        val request = try { inp.read() } catch (e: BufferUnderflowException) { return destroy(this.channel) }
        val response = processor.process(request)
        out.write(response)
    }

}
