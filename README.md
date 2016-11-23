Collaborator gateway for Burp Suite
===================================

Simple socket-based gateway to the Burp Collaborator.

Protocol
--------

 - The gateway listens on TCP port 8452 of the loopback interface.
 - The protocol uses [MessagePack][1] serialization without additional framing.
 - Sending `0` or `1` to the gateway results in a string reply containing
   a new payload, which includes the Collaborator location if the number
   sent is `1`.
 - Sending `2` to the gateway results in a string reply containing the
   Collaborator location, this can be used to construct FQDNs if the
   payload was requested with the number `0`.
 - Sending a string to the gateway results in the string being interpreted
   as a payload and the gateway will reply with an array of the interactions
   with the specified payload. Elements of the array will be maps with
   string keys, the values will be almost always strings, except for
   DNS raw queries, HTTP requests and responses and client IP addresses,
   which will be represented as binaries.

A sample client in Python is provided for easier understanding of the above
in the `clients` directory of this repository, in depends only on the
[msgpack-python][2] library, run `pip install msgpack-python` to install it.

  [1]: http://msgpack.org/
  [2]: https://github.com/msgpack/msgpack-python

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
