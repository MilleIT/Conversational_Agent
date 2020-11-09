import select
import socket
import sys
import threading
import time

from connector import Connection


class Tunnel(threading.Thread):
    """
    Tunnel class, main class of robot_connector.
    """

    def __init__(self):
        """
        init part 1, non-reusable.
        Set up variables
        """
        threading.Thread.__init__(self)
        self.buffer_size = 4096
        self.delay = 0.0001
        self.count_tunnels = 0
        self.connected = False
        self.tunnel = False
        self.socket = None
        self.data = False

        self.connection_list = []  # Input list for the select in main_loop, contains all open sockets.
        self.connection_mapping = {}  # Used to map server connection to naoqi os

        self.init()

    def init(self):
        """
        init part 2, reusable.
        Sets the server socket
        """
        global server_address
        while not self.connected:
            self.tunnel = Connection().connect(server_address)
            if self.tunnel:
                self.connection_list.append(self.tunnel)
                self.connected = True
                #print("Connected to the server on: " + str(server_address))
            else:
                print("Could not connect to the server on: " + str(server_address) + " Will try again.")
                time.sleep(self.delay)
                if server_address[1] is not server_port:
                    server_address = (server_address[0], server_port)

    def main_loop(self):
        """
        Main loop of the connector, here the connections and data streams are managed
        """
        while True:
            while self.connected:
                time.sleep(self.delay)
                inputready, outputready, exceptready = select.select(self.connection_list, [], [], 10)
                for self.socket in inputready:
                    try:
                        self.input = self.socket.recv(self.buffer_size)
                        self.data = self.input.decode("utf-8")
                        #print(self.data)

                        if self.socket == self.tunnel and len(self.data) == 0:
                            raise socket.error

                        if self.socket != self.tunnel and len(self.data) == 0:
                            self.close_tunnel()
                            break

                        else:
                            self.receive_data()

                    except socket.error as e:
                        print("Error received. Close all connections")
                        # print(e)
                        self.close_all()
                        break
            self.init()


    def close_all(self):
        """
        Close all sockets and reconnect to manager on a new socket.
        """
        for self.socket in self.connection_list:
            self.socket.close()
        self.connection_list = []
        self.connection_mapping = {}
        self.count_tunnels = 0
        self.connected = False


    def receive_data(self):
        """
        Handle the received data.
        """
        if self.data.startswith("emote:"):
            print(self.data.split(":")[1])
            self.socket.close()
            sys.exit(1)

        elif self.socket != self.tunnel:
            self.connection_mapping[self.socket].send(self.data)


if __name__ == '__main__':
    server_address = ('127.0.0.1', 50000)
    server_port = 50000

    tunnel = Tunnel()
    try:
        tunnel.main_loop()
    except KeyboardInterrupt:
        sys.exit(1)