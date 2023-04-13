package me.leepsky.sflextp.packet

data class SftpFile(
    val filename: String,
    val longname: String,
    val attrs: FileAttributes
)
