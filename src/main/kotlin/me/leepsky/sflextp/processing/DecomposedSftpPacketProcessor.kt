package me.leepsky.sflextp.processing

import me.leepsky.sflextp.packet.*

abstract class DecomposedSftpPacketProcessor: SftpPacketProcessor {

    override fun process(packet: SftpPacket): SftpPacket {
        return when (packet) {
            is SftpPacket1 -> process(packet)
            is SftpPacket3 -> process(packet)
            is SftpPacket4 -> process(packet)
            is SftpPacket5 -> process(packet)
            is SftpPacket6 -> process(packet)
            is SftpPacket11 -> process(packet)
            is SftpPacket12 -> process(packet)
            is SftpPacket13 -> process(packet)
            is SftpPacket14 -> process(packet)
            is SftpPacket15 -> process(packet)
            is SftpPacket16 -> process(packet)
            is SftpPacket17 -> process(packet)
            is SftpPacket18 -> process(packet)
            else -> TODO("Processing of packet ${packet.typeId} is not yet implemented.")
        }
    }

    /**
     * Process SSH_FXP_INIT. Always responds with SSH_FXP_VERSION.
     */
    protected abstract fun process(packet: SftpPacket1): SftpPacket

    /**
     * Process SSH_FXP_OPEN.
     *
     * Regardless the server operating system, the file will always be
     * opened in "binary" mode (i.e., no translations between different
     * character sets and newline encodings).
     *
     * The response to this message will be either SSH_FXP_HANDLE (if the
     * operation is successful) or SSH_FXP_STATUS (if the operation fails).
     */
    protected abstract fun process(packet: SftpPacket3): SftpPacket

    /**
     * Process SSH_FXP_CLOSE.
     *
     * For handles created by SSH_FXP_OPENDIR - does nothing, always returns SSH_FX_OK.
     * For handles created by SSH_FXP_OPEN - clears the memory, returns SSH_FX_OK.
     */
    protected abstract fun process(packet: SftpPacket4): SftpPacket

    /**
     * Process SSH_FXP_READ.
     *
     * In response to this request, the server will read as many bytes as it
     *    can from the file (up to `len'), and return them in a SSH_FXP_DATA
     *    message.  If an error occurs or EOF is encountered before reading any
     *    data, the server will respond with SSH_FXP_STATUS.  For normal disk
     *    files, it is guaranteed that this will read the specified number of
     *    bytes, or up to end of file.  For e.g.  device files this may return
     *    fewer bytes than requested.
     */
    protected abstract fun process(packet: SftpPacket5): SftpPacket

    /**
     * Process SSH_FXP_WRITE.
     *
     * The write will extend the file if writing beyond the end of the file.
     *    It is legal to write way beyond the end of the file; the semantics
     *    are to write zeroes from the end of the file to the specified offset
     *    and then the data.  On most operating systems, such writes do not
     *    allocate disk space but instead leave "holes" in the file.
     *
     * The server responds to a write request with a SSH_FXP_STATUS message.
     */
    protected abstract fun process(packet: SftpPacket6): SftpPacket

    protected abstract fun process(packet: SftpPacket11): SftpPacket

    protected abstract fun process(packet: SftpPacket12): SftpPacket

    protected abstract fun process(packet: SftpPacket13): SftpPacket

    /**
     * Process SSH_FXP_MKDIR
     *
     * An error will be returned if a file or
     *    directory with the specified path already exists.  The server will
     *    respond to this request with a SSH_FXP_STATUS message.
     */
    protected abstract fun process(packet: SftpPacket14): SftpPacket

    /**
     * Process SSH_FXP_RMDIR.
     *
     * An error will be returned if no directory
     *    with the specified path exists, or if the specified directory is not
     *    empty, or if the path specified a file system object other than a
     *    directory.  The server responds to this request with a SSH_FXP_STATUS
     *    message.
     */
    protected abstract fun process(packet: SftpPacket15): SftpPacket

    protected abstract fun process(packet: SftpPacket16): SftpPacket

    protected abstract fun process(packet: SftpPacket17): SftpPacket

    /**
     * Process SSH_FXP_RENAME.
     *
     * It is an error if there already exists a file
     *    with the name specified by newpath.  The server may also fail rename
     *    requests in other situations, for example if 'oldpath' and 'newpath'
     *    point to different file systems on the server.
     *
     * The server will respond to this request with a SSH_FXP_STATUS
     *    message.
     */
    protected abstract fun process(packet: SftpPacket18): SftpPacket

}