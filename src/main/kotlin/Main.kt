import me.leepsky.esftp.MySftpSubsystemFactory
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import java.io.File

fun main() {
    val sshd = SshServer.setUpDefaultServer()
    sshd.port = 2000
    sshd.keyPairProvider = SimpleGeneratorHostKeyProvider(File("hosts.ser").toPath())
    sshd.subsystemFactories = listOf(MySftpSubsystemFactory())
    sshd.setPasswordAuthenticator { _, _, _ -> true}
    sshd.start()

    while (true) { }
}