#!/usr/bin/env python

import socket
import msgpack

class Client(object):
    def __init__(self):
        sock = socket.socket()
        sock.connect(('localhost', 8452))
        self.sock = sock

    def get_collaborator_server_location(self):
        return self._exchange_packet(2)

    def generate_payload(self, include_location=True):
        return self._exchange_packet(int(include_location))

    def fetch_collaborator_interactions_for(self, payload):
        return self._exchange_packet(payload)

    def _exchange_packet(self, data):
        s = self.sock
        s.send(msgpack.packb(data))
        unpacker = msgpack.Unpacker()
        while True:
            unpacker.feed(s.recv(1))
            for value in unpacker:
                return value


if __name__ == '__main__':
    from urllib2 import urlopen
    c = Client()
    print repr(c.get_collaborator_server_location())
    print repr(c.generate_payload(False))
    payload = c.generate_payload(True)
    print repr(payload)
    print repr(urlopen('http://{0}/'.format(payload)).read())
    print repr(c.fetch_collaborator_interactions_for(payload))
