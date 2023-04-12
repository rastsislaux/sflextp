package me.leepsky.esftp

import me.leepsky.esftp.input.DefaultSftpInputReader
import me.leepsky.esftp.input.SftpInputReader
import me.leepsky.esftp.output.DefaultSftpOutputWriter
import me.leepsky.esftp.output.SftpOutputWriter
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback
import org.apache.sshd.server.channel.ChannelSession
import org.apache.sshd.server.command.Command
import java.io.InputStream
import java.io.OutputStream
import java.nio.Buffer

class MySftpSubsystem(
    private val channel: ChannelSession?
): Command, Runnable {

    private lateinit var out: SftpOutputWriter
    private lateinit var err: OutputStream
    private lateinit var inp: SftpInputReader
    private val processor: SftpPacketProcessor = SftpPacketProcessor()
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
        this.inp = DefaultSftpInputReader()
        this.inp.setInputStream(`in`)
    }

    override fun setOutputStream(out: OutputStream?) {
        this.out = DefaultSftpOutputWriter()
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
        val packet = inp.read()
        println(packet)
        out.write(processor.process(packet))
    }
}
