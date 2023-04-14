package me.leepsky.sflextp.packet

import java.nio.ByteBuffer

object FileAttributeFlag {
    const val SSH_FILEXFER_ATTR_SIZE        = 0x0000001
    const val SSH_FILEXFER_ATTR_UIDGID      = 0x0000002
    const val SSH_FILEXFER_ATTR_PERMISSIONS = 0x0000004
    const val SSH_FILEXFER_ATTR_ACMODTIME   = 0x0000008
    const val SSH_FILEXFER_ATTR_EXTENDED    = 0x8000000
}

/**
 * A new compound data type, 'ATTRS', is defined for encoding file
 * attributes.  The same encoding is used both when returning file
 * attributes from the server and when sending file attributes to the
 * server.
 */
data class FileAttributes(
    /**
     * The `flags' specify which of the fields are present.  Those fields
     *    for which the corresponding flag is not set are not present (not
     *    included in the packet).
     */
    val flags: Int,

    /**
     * Present only if flag SSH_FILEXFER_ATTR_SIZE.
     *
     * Specifies the size of the file in bytes.
     */
    val size: Long? = null,

    /**
     * Present only if flag SSH_FILEXFER_ATTR_UIDGID
     *
     * Unix-like user identifier.
     */
    val uid: Int? = null,

    /**
     * Present only if flag SSH_FILEXFER_ATTR_UIDGID
     *
     * Unix-like group identifier.
     */
    val gid: Int? = null,

    /**
     * Present only if flag SSH_FILEXFER_ATTR_PERMISSIONS
     *
     * Contains a bit mask of file permissions as
     *    defined by posix.
     */
    val permissions: Int? = null,

    /**
     * Present only if flag SSH_FILEXFER_ACMODTIME
     *
     * Contains the access time of
     *    the files, represented as seconds from Jan 1,
     *    1970 in UTC.
     */
    val atime: Int? = null,

    /**
     * Present only if flag SSH_FILEXFER_ACMODTIME
     *
     * Contains the modification time of
     *    the files, represented as seconds from Jan 1,
     *    1970 in UTC.
     */
    val mtime: Int? = null,
) {
    fun toByteArray(): ByteArray = ByteBuffer.allocate(4)
        .putInt(flags)
        .array()

    val hasSize get()        = flags and FileAttributeFlag.SSH_FILEXFER_ATTR_SIZE
    val hasUidGid get()      = flags and FileAttributeFlag.SSH_FILEXFER_ATTR_UIDGID
    val hasPermissions get() = flags and FileAttributeFlag.SSH_FILEXFER_ATTR_PERMISSIONS
    val hasAcModTime get()   = flags and FileAttributeFlag.SSH_FILEXFER_ATTR_ACMODTIME
}