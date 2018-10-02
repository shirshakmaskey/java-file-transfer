# java-file-transfer
Small java project to concurrently send files

I want you to write a simple client-server socket programming in Java. This program will be used to transfer files from client to server. Server will be listening on a port (say port# 5050) and Client will connect to Server and transfer files to Server. Here are details:

Client should accept command line parameter to set the number of concurrent file transfers. For example, java Client 5 will transfer five files concurrently at any given time as long as there are enough files.  If concurrency number is not entered, it should transfer one file at a time (aka concurrency=1), by default.
    
The application should support integrity verification. That is, your client and server will calculate checksum of files after it’s transferred and compare them to make sure data is transferred without any error.

Once the code is working, please try with following test scenarios.

Create a dataset with 100 files each 10MB size and transfer with concurrency 1, 2,4 and 8 and measure throughput

Create a dataset with 10 files each 1GB size and transfer with concurrency 1, 2, 4 and 8 and measure throughput

Combine above two datasets in a single dataset and transfer with concurrency 1,2,4 and 8 and measure throughput

After tests are done, draw figure for each tests case in item#2 where x-axis is concurrency value and y-axis is throughput.

