Collaborator gateway for Burp Suite
===================================

Simple socket-based gateway to the Burp Collaborator.

Building
--------

 - Install the dependencies, in case of libraries, put the JARs into `lib`
 - Save the Burp Extender API from Burp and unpack it into `src`
 - Execute `ant`, and you'll have the plugin ready in `burp-collab-gw.jar`

Dependencies
------------

 - JDK 1.7+ (tested on OpenJDK `1.8.0_91`, Debian/Ubuntu package: `openjdk-7-jdk`)
 - Apache ANT (Debian/Ubuntu package: `ant`)
 - MessagePack for Java https://github.com/msgpack/msgpack-java/

License
-------

The whole project is available under MIT license, see `LICENSE.txt`.
