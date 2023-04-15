package me.leepsky.sflextp

import me.leepsky.sflextp.input.SftpInputReader
import me.leepsky.sflextp.output.SftpOutputWriter
import me.leepsky.sflextp.packet.SftpPacket101
import me.leepsky.sflextp.packet.SftpPacketWithId
import me.leepsky.sflextp.processing.SftpPacketProcessor
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback
import org.apache.sshd.server.channel.ChannelSession
import org.apache.sshd.server.command.Command
import java.io.InputStream
import java.io.OutputStream
import java.nio.BufferUnderflowException
import java.util.*

class SflextpSubsystem(
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
        var id = 0
        try {
            val request = inp.read()
            if (request is SftpPacketWithId) {
                id = request.id
            }
            val response = processor.process(request)
            out.write(response)
        } catch (ex: BufferUnderflowException) {
            destroy(this.channel)
        } catch (ex: Exception) {
            ex.printStackTrace()
            out.write(SftpPacket101(id, SftpPacket101.Companion.StatusCode.SSH_FX_OP_UNSUPPORTED,
                ex.message ?: "Unsupported.", Locale.ENGLISH))
        }
    }

}
