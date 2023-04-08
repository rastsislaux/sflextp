package me.leepsky.esftp

import java.nio.ByteBuffer

object ValidAttributeMask {
    const val SSH_FILEXFER_ATTR_SIZE              = 0x00000001U
    const val SSH_FILEXFER_ATTR_PERMISSIONS       = 0x00000004U
    const val SSH_FILEXFER_ATTR_ACCESSTIME        = 0x00000008U
    const val SSH_FILEXFER_ATTR_CREATETIME        = 0x00000010U
    const val SSH_FILEXFER_ATTR_MODIFYTIME        = 0x00000020U
    const val SSH_FILEXFER_ATTR_ACL               = 0x00000040U
    const val SSH_FILEXFER_ATTR_OWNERGROUP        = 0x00000080U
    const val SSH_FILEXFER_ATTR_SUBSECOND_TIMES   = 0x00000100U
    const val SSH_FILEXFER_ATTR_BITS              = 0x00000200U
    const val SSH_FILEXFER_ATTR_ALLOCATION_SIZE   = 0x00000400U
    const val SSH_FILEXFER_ATTR_TEXT_HINT         = 0x00000800U
    const val SSH_FILEXFER_ATTR_MIME_TYPE         = 0x00001000U
    const val SSH_FILEXFER_ATTR_LINK_COUNT        = 0x00002000U
    const val SSH_FILEXFER_ATTR_UNTRANSLATED_NAME = 0x00004000U
    const val SSH_FILEXFER_ATTR_CTIME             = 0x00008000U
    const val SSH_FILEXFER_ATTR_EXTENDED          = 0x80000000U
}

/**
 * A new compound data type, 'ATTRS', is defined for encoding file
 * attributes.  The same encoding is used both when returning file
 * attributes from the server and when sending file attributes to the
 * server.
 */
data class FileAttributes(
    val validAttributeFlags: Int,
    val type: Type
) {

    fun toByteArray(): ByteArray = ByteBuffer.allocate(8)
        .putInt(validAttributeFlags)
        .putInt(type.value)
        .array()

    companion object {

        enum class Type(val value: Int) {
            SSH_FILEXFER_TYPE_REGULAR(1),
            SSH_FILEXFER_TYPE_DIRECTORY(2),
            SSH_FILEXFER_TYPE_SYMLINK(3),
            SSH_FILEXFER_TYPE_SPECIAL(4),
            SSH_FILEXFER_TYPE_UNKNOWN(5),
            SSH_FILEXFER_TYPE_SOCKET(6),
            SSH_FILEXFER_TYPE_CHAR_DEVICE(7),
            SSH_FILEXFER_TYPE_BLOCK_DEVICE(8),
            SSH_FILEXFER_TYPE_FIFO(9)
        }

    }

}