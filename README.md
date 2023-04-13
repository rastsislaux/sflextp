# Sflextp Subsystem for Apache Mina
## Overview
*Sflextp* is a simple and extensible SFTP subsystem for Apache Mina. It provides a way to handle SFTP requests and responses in a flexible way, by allowing users to customize the input reader, output writer, and packet processor used by the subsystem.

## Getting Started
To use sflextp, you need to add the following dependency to your project:

Maven:
```xml
<dependency>
    <groupId>me.leepsky</groupId>
    <artifactId>sflextp</artifactId>
    <version>1.0.0</version>
</dependency>
```
Gradle:
```groovy
implementation("me.leepsky:sflextp:1.0.0")
```

Once you have added the dependency, you can create an SFTP server and add the sflextp subsystem to it like this:

```kotlin
fun main() {
    val sshd = SshServer.setUpDefaultServer()
    sshd.port = 2000
    sshd.keyPairProvider = SimpleGeneratorHostKeyProvider(File("hosts.ser").toPath())
    sshd.subsystemFactories = listOf(SflextpSubsystemFactory({ SflextpInputReader() },
                                                             { SflextpOutputWriter() },
                                                             { SflextpPacketProcessor() }))
    sshd.setPasswordAuthenticator { _, _, _ -> true}
    sshd.start()

    while (true) { }
}
```
This code creates an SFTP server on port 2000 and adds the sflextp subsystem to it. It uses the default input reader, output writer, and packet processor, but you can customize these by passing your own instances to the SflextpSubsystemFactory constructor.

## Customization
To customize the sflextp subsystem, you can implement your own input reader, output writer, or packet processor, and pass them to the SflextpSubsystemFactory constructor. For example, here's how you could create a custom packet processor:

```kotlin
class MyPacketProcessor: SflextpPacketProcessor() {

    override fun process(packet: SftpPacket1): SftpPacket2 {
        println("Overriden method!")
        return super.process(packet)
    }

}
```
You could then use this packet processor in your SFTP server like this:

```kotlin
val sshd = SshServer.setUpDefaultServer()
sshd.port = 2000
sshd.keyPairProvider = SimpleGeneratorHostKeyProvider(File("hosts.ser").toPath())
sshd.subsystemFactories = listOf(SflextpSubsystemFactory({ SflextpInputReader() },
                                                         { SflextpOutputWriter() },
                                                         { MyPacketProcessor() }))
sshd.setPasswordAuthenticator { _, _, _ -> true}
sshd.start()

while (true) { }
```

## License
Sflextp is released under the Apache License, Version 2.0. See the LICENSE file for more information.

## Contributing
We welcome contributions to sflextp! If you would like to contribute code, documentation, or anything else, please submit a pull request on GitHub.
