package me.leepsky.sflextp

import me.leepsky.sflextp.input.SflextpInputReader
import me.leepsky.sflextp.output.SflextpOutputWriter
import me.leepsky.sflextp.processing.FilesystemPacketProcessor
import me.leepsky.sflextp.processing.InMemoryPacketProcessor
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class ServerTest {

    @Test
    fun runServer() {
        val ssh = SshServer.setUpDefaultServer()
        ssh.port = 2000
        ssh.keyPairProvider = SimpleGeneratorHostKeyProvider(Path("hosts.ser"))
        ssh.subsystemFactories = listOf(SflextpSubsystemFactory({ SflextpInputReader() },
                                                                { SflextpOutputWriter() },
                                                                { InMemoryPacketProcessor() }))
        ssh.setPasswordAuthenticator { _, _, _ -> true }
        ssh.start()

        while (true) { }
    }

}