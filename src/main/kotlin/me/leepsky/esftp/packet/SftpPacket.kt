package me.leepsky.esftp.packet

import me.leepsky.esftp.FileAttributes
import java.util.Locale

object SftpPacketType {
    const val SSH_FXP_INIT = 1
    const val SSH_FXP_VERSION = 2
    const val SSH_FXP_OPEN = 3
    const val SSH_FXP_CLOSE = 4
    const val SSH_FXP_READ = 5
    const val SSH_FXP_WRITE = 6
    const val SSH_FXP_OPENDIR = 11
    const val SSH_FXP_READDIR = 12
    const val SSH_FXP_REALPATH = 16
    const val SSH_FXP_STATUS = 101
    const val SSH_FXP_HANDLE = 102
    const val SSH_FXP_DATA = 103
    const val SSH_FXP_NAME = 104
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
 * Represents SSH_FXP_CLOSE
 */
data class SftpPacket4(
    val requestId: Int,
    val handle: String
): SftpPacket(SftpPacketType.SSH_FXP_CLOSE)

/**
 * Represents SSH_FXP_OPENDIR
 */
data class SftpPacket11(
    val requestId: Int,
    val path: String
): SftpPacket(SftpPacketType.SSH_FXP_OPENDIR)

/**
 * Represents SSH_FXP_READDIR
 */
data class SftpPacket12(
    val requestId: Int,
    val handle: String
): SftpPacket(SftpPacketType.SSH_FXP_READDIR)

/**
 * Represents SSH_FXP_REALPATH
 */
data class SftpPacket16(
    val requestId: Int,
    val originalPath: String,
    val controlByte: ControlByte?,
    val composePath: List<String>?
): SftpPacket(SftpPacketType.SSH_FXP_REALPATH) {
    companion object {
        enum class ControlByte(val value: Byte) {
            SSH_FXP_REALPATH_NO_CHECK(1),
            SSH_FXP_REALPATH_STAT_IF(2),
            SSH_FXP_REALPATH_STAT_ALWAYS(3);
        }
    }
}

/**
 * Represents SSH_FXP_STATUS
 */
data class SftpPacket101(
    val requestId: Int,
    val statusCode: StatusCode,
    val errorMessage: String,
    val languageTag: Locale
): SftpPacket(SftpPacketType.SSH_FXP_STATUS) {

    companion object {

        enum class StatusCode(val value: Int) {
            SSH_FX_OK                            (0),
            SSH_FX_EOF                           (1),
            SSH_FX_NO_SUCH_FILE                  (2),
            SSH_FX_PERMISSION_DENIED             (3),
            SSH_FX_FAILURE                       (4),
            SSH_FX_BAD_MESSAGE                   (5),
            SSH_FX_NO_CONNECTION                 (6),
            SSH_FX_CONNECTION_LOST               (7),
            SSH_FX_OP_UNSUPPORTED                (8),
            SSH_FX_INVALID_HANDLE                (9),
            SSH_FX_NO_SUCH_PATH                  (10),
            SSH_FX_FILE_ALREADY_EXISTS           (11),
            SSH_FX_WRITE_PROTECT                 (12),
            SSH_FX_NO_MEDIA                      (13),
            SSH_FX_NO_SPACE_ON_FILESYSTEM        (14),
            SSH_FX_QUOTA_EXCEEDED                (15),
            SSH_FX_UNKNOWN_PRINCIPAL             (16),
            SSH_FX_LOCK_CONFLICT                 (17),
            SSH_FX_DIR_NOT_EMPTY                 (18),
            SSH_FX_NOT_A_DIRECTORY               (19),
            SSH_FX_INVALID_FILENAME              (20),
            SSH_FX_LINK_LOOP                     (21),
            SSH_FX_CANNOT_DELETE                 (22),
            SSH_FX_INVALID_PARAMETER             (23),
            SSH_FX_FILE_IS_A_DIRECTORY           (24),
            SSH_FX_BYTE_RANGE_LOCK_CONFLICT      (25),
            SSH_FX_BYTE_RANGE_LOCK_REFUSED       (26),
            SSH_FX_DELETE_PENDING                (27),
            SSH_FX_FILE_CORRUPT                  (28),
            SSH_FX_OWNER_INVALID                 (29),
            SSH_FX_GROUP_INVALID                 (30),
            SSH_FX_NO_MATCHING_BYTE_RANGE_LOCK   (31)
        }

    }

}

/**
 * Represents SSH_FXP_HANDLE
 */
data class SftpPacket102(
    val requestId: Int,
    val handle: String
): SftpPacket(SftpPacketType.SSH_FXP_HANDLE)

/**
 * Represents SSH_FXP_NAME
 */
data class SftpPacket104(
    val requestId: Int,
    val content: Map<String, FileAttributes>,
    val endOfList: Boolean?
): SftpPacket(SftpPacketType.SSH_FXP_NAME)
