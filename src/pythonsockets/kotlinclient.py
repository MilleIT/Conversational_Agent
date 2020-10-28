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
                print("Connected to the server on: " + str(server_address))
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

    # def new_tunnel(self):
    #     """
    #     Establish a new tunnel between naoqi and the server.
    #     """
    #     server_socket = Connection().connect(naoqi_address)
    #     naoqi_socket = Connection().connect(server_address)
    #     if server_socket and naoqi_socket:
    #         self.connection_list.append(naoqi_socket)
    #         self.connection_list.append(server_socket)
    #         self.connection_mapping[naoqi_socket] = server_socket
    #         self.connection_mapping[server_socket] = naoqi_socket
    #         self.count_tunnels += 1
    #         print("New tunnel is established. Total: " + str(self.count_tunnels))
    #         print("established tunnel is between: " + str(naoqi_address) + " and " + str(server_address))
    #     if not server_socket and naoqi_socket:
    #         naoqi_socket.close()
    #     if not naoqi_socket and server_socket:
    #         server_socket.close()

    def new_management_socket(self):
        """
        Close existing management socket and open a new one.
        """
        self.connection_list.remove(self.tunnel)
        self.tunnel.close()
        self.connected = False
        print("connection reset in order to connect ot a new management port")
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
        print("All connections are dropped. Robot will try to reconnect to the server again.")

    # def close_tunnel(self):
    #     """
    #     Close a tunnel and remove its mapping.
    #     """
    #     socket_pair = self.connection_mapping[self.socket]
    #     self.connection_list.remove(self.socket)
    #     self.connection_list.remove(socket_pair)
    #     self.connection_mapping[socket_pair].close()
    #     self.connection_mapping[self.socket].close()
    #     del self.connection_mapping[socket_pair]
    #     del self.connection_mapping[self.socket]
    #     self.count_tunnels -= 1
    #     print("A tunnel is closed. Total left: " + str(self.count_tunnels))

    def receive_data(self):
        """
        Handle the received data.
        """
        if self.data.startswith("emote:"):
            print(self.data.split(":")[1])
            # sequence = "ID " + str(robot_name) + " " + str(robot_id) + " " + str(robot_type)
            # sequence = "testing"
            # self.socket.send(bytes(sequence,"utf-8"))
            self.socket.close()
            sys.exit(1)

        elif self.data.startswith("open_new") and len(str(self.data)) == 8:
            self.new_tunnel()
        elif self.data.startswith("open_new"):
            query = str(self.data).split()
            global server_address
            server_address = (server_address[0], int(query[1]))
            self.new_management_socket()
        elif self.data == "heartbeat":
            print(self.data)
        elif self.socket != self.tunnel:
            self.connection_mapping[self.socket].send(self.data)


if __name__ == '__main__':
    # args = parse_arguments()

    # naoqi_address = (args[2], args[3])
    server_address = ('127.0.0.1', 50000)
    server_port = 50000

    # robot_id = robot_diagnostics.get_id()
    # robot_name = robot_diagnostics.get_name()
    # robot_type = robot_diagnostics.get_type()

    # logger = log.create_logger("robot_connector")

    tunnel = Tunnel()
    try:
        tunnel.main_loop()
    except KeyboardInterrupt:
        sys.exit(1)