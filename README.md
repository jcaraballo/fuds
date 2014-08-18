fuds
====

[![Travis badge](https://travis-ci.org/jcaraballo/fuds.svg?branch=master)](https://travis-ci.org/jcaraballo/fuds)

File Uploading and Downloading Service

... or poor man's [WebDav](http://en.wikipedia.org/wiki/WebDAV)

Download the latest jar from https://hallon.org.uk:9010/fuds

Usage
-----

By default

    $ java -jar fuds.jar
    2014-08-17 17:34:14.350:INFO:oejs.Server:jetty-8.y.z-SNAPSHOT
    2014-08-17 17:34:14.414:INFO:oejs.AbstractConnector:Started SocketConnector@0.0.0.0:8080
    Server started on port 8080

You can upload a file

    $ echo hi >foo
    $ curl -T foo http://localhost:8080/foo

and download it

    $ curl http://localhost:8080/foo
    hi

With some options, you can do HTTPS

    $ java -jar fuds.jar --https keystore-local.jks:dummypass
    2014-08-17 17:38:38.758:INFO:oejs.Server:jetty-8.y.z-SNAPSHOT
    2014-08-17 17:38:39.013:INFO:oejus.SslContextFactory:Enabled Protocols [SSLv2Hello, SSLv3, TLSv1, TLSv1.1, TLSv1.2] of [SSLv2Hello, SSLv3, TLSv1, TLSv1.1, TLSv1.2]
    2014-08-17 17:38:39.033:INFO:oejs.AbstractConnector:Started $anon$1@0.0.0.0:8080
    Server started on port 8080
    [...]

    $ curl --cacert fuds-local.cacert -3T foo https://localhost:8080/bar
    $ curl --cacert fuds-local.cacert -3 https://localhost:8080/bar
    hi

Or basic auth for uploads (should be combined with HTTPS)

    $ echo bob:secret >uploads_credentials
    $ java -jar fuds.jar --https keystore-local.jks:dummypass --uploads-white-list uploads_credentials
    [...]

    $ curl --cacert fuds-local.cacert --user bob:secret -3T foo https://localhost:8080/qux
    $ curl -k3 https://localhost:8080/qux
    hi

And a couple of more things. There's a --help

    $ $ java -jar fuds.jar --help
    fuds 1.0
    Usage: fuds [options]
    
      --port <value>
        
      --https <file>[:<password>]
            Serve HTTPS using <file> key store. If no <password> is provided, the password will be requested from the console. The default is HTTP (no HTTPS).
      --storage <directory>
            Directory to store the uploaded files and to serve them for downloading.  (Default "files/".)
      --content-white-list <file>
            White list restricting the content that can be uploaded. When no --content-white-list, any content can be uploaded. --content-white-list blank_file will reject all uploads.
      --uploads-white-list <file>
            White list restricting which user:password credentials are allowed to upload files. When no --uploads-white-list, uploads do not require authentication. --uploads-white-list blank_file will reject all uploads.
      --list
            When GETing a url that match a directory, a list of the content will be returned (see --no-list).
      --no-list
            Do not allow GETing urls that match a directory (see --list).
      --help
            Prints this usage text


