# NETJLO
Package for creating connections over the java.net.Socket, which deals with incoming and outgoing data. Mainly to use for not writing boilerplate code. 

## [NETJLO-core]
The core package.  
Contains everything to make Connections from a client to a server and back. Make your own interpreter to decide what to do with the information sent over the socket. And make your very own custom package to send whatever type of data you want over the connection.

## [NETJLO-fos]
The file transfer package.  
"fos" stands for *file over socket*. And quintessentially means a way to send a file over a java.net.Socket without using libraries with FTP/FTPS/SFTP support.  
Send one file or a folder with all its subdirectories and files. Also, multiple folders at once!  
Has zip support. Files can be zipped before sending over the connection and unzipped when receiving them. Works with a directory too!

## [NETJLO-timed]
The core package but with timings.  
Decide when a specific package should be sent and when it should be interpreted by the other side of the connection.

## Roadmap
- SSL implementation with the java.net.SSLSocket, for all connections
- Option to bundle sender-, receiver- and interpreter-threads into ThreadPoolExecutors
- Documentation
- Examples, explanations and quick start guide
