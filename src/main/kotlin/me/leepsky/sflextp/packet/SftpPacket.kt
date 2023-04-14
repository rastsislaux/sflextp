package me.leepsky.sflextp.packet

import java.util.Locale

object SftpPacketType {
    const val SSH_FXP_INIT     = 1
    const val SSH_FXP_VERSION  = 2
    const val SSH_FXP_OPEN     = 3
    const val SSH_FXP_CLOSE    = 4
    const val SSH_FXP_READ     = 5
    const val SSH_FXP_WRITE    = 6
    const val SSH_FXP_LSTAT    = 7
    const val SSH_FXP_FSTAT    = 8
    const val SSH_FXP_SETSTAT  = 9
    const val SSH_FXP_OPENDIR  = 11
    const val SSH_FXP_READDIR  = 12
    const val SSH_FXP_REMOVE   = 13
    const val SSH_FXP_MKDIR    = 14
    const val SSH_FXP_RMDIR    = 15
    const val SSH_FXP_REALPATH = 16
    const val SSH_FXP_STAT     = 17
    const val SSH_FXP_RENAME   = 18
    const val SSH_FXP_READLINK = 19
    const val SSH_FXP_SYMLINK  = 20
    const val SSH_FXP_STATUS   = 101
    const val SSH_FXP_HANDLE   = 102
    const val SSH_FXP_DATA     = 103
    const val SSH_FXP_NAME     = 104
    const val SSH_FXP_ATTRS    = 105
    const val SSH_FXP_EXTENDED = 200
    const val SSH_FXP_EXTENDED_REPLY = 201
}

sealed class SftpPacket(
    val typeId: Int
)

/**
 * Represents SSH_FXP_INIT
 */
data class SftpPacket1(
    val version: Int
): SftpPacket(SftpPacketType.SSH_FXP_INIT)

/**
 * Represents SSH_FXP_VERSION
 */
data class SftpPacket2(
    val version: Int
): SftpPacket(SftpPacketType.SSH_FXP_VERSION)

/**
 * Represents SSH_FXP_OPEN
 */
data class SftpPacket3(
    val id: Int,
    val filename: String,
    val pflags: PFlags,
    val attrs: FileAttributes
): SftpPacket(SftpPacketType.SSH_FXP_OPEN) {

    companion object {

        data class PFlags(val value: Int) {
            val isRead   get() = value and 0x1  != 0
            val isWrite  get() = value and 0x2  != 0
            val isAppend get() = value and 0x4  != 0
            val isCreate get() = value and 0x8  != 0
            val isTrunc  get() = value and 0x10 != 0
            val isExcl   get() = value and 0x20 != 0
        }

    }

}

/**
 * Represents SSH_FXP_CLOSE
 */
data class SftpPacket4(
    val id: Int,
    val handle: String
): SftpPacket(SftpPacketType.SSH_FXP_CLOSE)

/**
 * Represents SFTP_FXP_READ
 */
data class SftpPacket5(
    /**
     * the request identifier
     */
    val id: Int,

    /**
     * an open file handle returned by SSH_FXP_OPEN
     */
    val handle: String,

    /**
     * the offset (in bytes) relative to the beginning of the file from where to start reading
     */
    val offset: Long,

    /**
     * the maximum number of bytes to read
     */
    val len: Int
): SftpPacket(SftpPacketType.SSH_FXP_READ)

/**
 * Represents SSH_FXP_WRITE
 *
 * Writing to a file is achieved using the SSH_FXP_WRITE message.
 */
data class SftpPacket6(
    /**
     * The request identifier.
     */
    val id: Int,

    /**
     * A file handle, returned by SSH_FXP_OPEN.
     */
    val handle: String,

    /**
     * The offset (in bytes) from the beginning of the file where to start writing
     */
    val offset: Long,

    /**
     * The data to be written.
     */
    val data: ByteArray
): SftpPacket(SftpPacketType.SSH_FXP_WRITE) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SftpPacket6

        if (id != other.id) return false
        if (handle != other.handle) return false
        if (offset != other.offset) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + handle.hashCode()
        result = 31 * result + offset.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}

/**
 * Represents SSH_FXP_OPENDIR
 */
data class SftpPacket11(
    val id: Int,
    val path: String
): SftpPacket(SftpPacketType.SSH_FXP_OPENDIR)

/**
 * Represents SSH_FXP_READDIR
 */
data class SftpPacket12(
    val id: Int,
    val handle: String
): SftpPacket(SftpPacketType.SSH_FXP_READDIR)

/**
 * Represents SSH_FXP_REMOVE
 *
 * Files can be removed using the SSH_FXP_REMOVE message.
 */
data class SftpPacket13(
    /**
     * The request identifier.
     */
    val id: Int,

    /**
     * The name of the file to be removed.
     */
    val filename: String
): SftpPacket(SftpPacketType.SSH_FXP_REMOVE)

/**
 * Represents SSH_FXP_MKDIR
 *
 * New directories can be created using the SSH_FXP_MKDIR request.
 */
data class SftpPacket14(
    /**
     * The request identifier
     */
    val id: Int,

    /**
     * Path of the directory to be created
     */
    val path: String,

    /**
     * Attributes of created directory
     */
    val attrs: FileAttributes
): SftpPacket(SftpPacketType.SSH_FXP_MKDIR)

/**
 * Represents SSH_FXP_RMDIR.
 *
 * Directories can be removed using the SSH_FXP_RMDIR request
 */
data class SftpPacket15(
    /**
     * The request identifier.
     */
    val id: Int,

    /**
     * The directory to be removed.
     */
    val path: String
): SftpPacket(SftpPacketType.SSH_FXP_RMDIR)

/**
 * Represents SSH_FXP_REALPATH
 */
data class SftpPacket16(
    val id: Int,
    val path: String
): SftpPacket(SftpPacketType.SSH_FXP_REALPATH)

/**
 * Represents SSH_FXP_STAT
 */
data class SftpPacket17(
    val id: Int,
    val path: String
): SftpPacket(SftpPacketType.SSH_FXP_STAT)

/**
 * Represents SSH_FXP_RENAME.
 *
 *  Files (and directories) can be renamed using the SSH_FXP_RENAME message.
 */
data class SftpPacket18(
    /**
     * The request identifier.
     */
    val id: Int,

    /**
     * The name of existing file or directory.
     */
    val oldPath: String,

    /**
     * The new name for the file or directory.
     */
    val newPath: String
): SftpPacket(SftpPacketType.SSH_FXP_RENAME)

/**
 * Represents SSH_FXP_STATUS
 */
data class SftpPacket101(
    val id: Int,
    val statusCode: StatusCode,
    val errorMessage: String,
    val languageTag: Locale
): SftpPacket(SftpPacketType.SSH_FXP_STATUS) {

    companion object {

        enum class StatusCode(val value: Int) {
            SSH_FX_OK               (0),
            SSH_FX_EOF              (1),
            SSH_FX_NO_SUCH_FILE     (2),
            SSH_FX_PERMISSION_DENIED(3),
            SSH_FX_FAILURE          (4),
            SSH_FX_BAD_MESSAGE      (5),
            SSH_FX_NO_CONNECTION    (6),
            SSH_FX_CONNECTION_LOST  (7),
            SSH_FX_OP_UNSUPPORTED   (8)
        }

    }

}

/**
 * Represents SSH_FXP_HANDLE
 */
data class SftpPacket102(
    val id: Int,
    val handle: String
): SftpPacket(SftpPacketType.SSH_FXP_HANDLE)

/**
 * Represents SSH_FXP_DATA
 */
data class SftpPacket103(
    /**
     * the request identifier
     */
    val id: Int,

    /**
     * An arbitrary byte
     *    string containing the requested data.  The data string may be at most
     *    the number of bytes requested in a SSH_FXP_READ request, but may also
     *    be shorter if end of file is reached or if the read is from something
     *    other than a regular file.
     */
    val data: ByteArray
): SftpPacket(SftpPacketType.SSH_FXP_DATA) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SftpPacket103

        if (id != other.id) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + data.contentHashCode()
        return result
    }

}

/**
 * Represents SSH_FXP_NAME
 */
data class SftpPacket104(
    val id: Int,
    val content: List<SftpFile>,
): SftpPacket(SftpPacketType.SSH_FXP_NAME)

/**
 * Represents SSH_FXP_ATTRS
 */
data class SftpPacket105(
    val id: Int,
    val attrs: FileAttributes
): SftpPacket(SftpPacketType.SSH_FXP_ATTRS)
