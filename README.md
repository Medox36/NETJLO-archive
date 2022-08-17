# NETJLO
Package for creating connections over the java.net.Socket, wich deals with incomming and outgoing data. Mainly to use for not writing boilerplate code. 

<h2>[NETJLO-core]</h2>
The core package.<br>
Contains everthing to make Connections from a client to a server and back. Make your own interpreter to decide what to do with the information sent over the socket. And make your very own custom package to send whatever type of data you want over the connection.

<h2>[NETJLO-fos]</h2>
The file transfere package.<br>
"fos" stands for file over socket. And quintessentially means a way to send a file over a java.net.Socket without using libraries with FTP/FTPS/SFTP support.<br>
Send one file or a folder with all its subdirectories and files. Also multiple folders at once!
Has zip support. Files can be zipped before sending over the connection and unzipped when receiving them. Works with a directory too!<br>

<h2>[NETJLO-timed]</h2>
THe core package but with timings.<br>
Decide when a specific package should be sent and when it should be interpreted by the other side of the connection.
