import select
import socket
import sys
import time
from connector import Connection
from EmotionDetection.src import emotions



class Manager:

    def __init__(self, host, port):
        """
        Set up the server socket and other variables.
        :param host: The ip on which the Manager listens.
        :param port: The port on which the Manager listens.
        """
        self.manager = Connection().listen((host, port), 200)

        #self.web_listener = web_manager.get_listener()

        self.delay = 0.0001
        self.buffer_size = 4096
        self.data = False
        self.input = False
        self.socket = None
        self.container = False
        #self.webmanager = False

        self.threads = []  # Used for thread cleanup.
        self.to_remove = []  # Used to remove Relay threads from self.threads.
        self.addresses = {}
        self.containers = []  # Used for container cleanup
        self.free_ports = range(50002, 60001, 1)  # +1 because max value is exclusive
        self.connection_list = []  # Input list for the select in manage, contains all open sockets.

        #random.shuffle(self.free_ports)  # Shuffle the ports to make port prediction more difficult.

        print("Manager initialised on port: " + str(port))
        #print("WebManager initialised on port: " + str(port + 1))

    def manage(self):
        """
        Mainloop of the Manager, incoming robot connections are accepted and forwarded to Relay threads.
        Relay threads are created and managed here.
        """
        self.connection_list.append(self.manager)
        #self.connection_list.append(self.web_listener)
        while True:
            time.sleep(self.delay)
            input_ready, output_ready, except_ready = select.select(self.connection_list, [], [], 10)
            for self.socket in input_ready:
                if self.socket == self.manager:
                    self.accept_connection()
                    break

                # if self.socket == self.web_listener:
                #     self.accept_webserver()
                #     break

                try:
                    self.input = self.socket.recv(self.buffer_size)
                    self.data = self.input.decode("utf-8")
                    print(self.data)

                    if not self.data:
                        raise socket.error("connection closed by remote host")
                    # elif self.socket == self.webmanager:
                    #     self.web_manager.handle_data(self.data, self.socket)
                    else:
                        raise socket.error("Unrecognised data received")

                except socket.error as e:
                    print("error caught on socket: " + str(self.addresses[self.socket]))
                    # print("debug: " + e)
                    self.socket.close()
                    self.connection_list.remove(self.socket)
                    del self.addresses[self.socket]
                    # if self.socket == self.webmanager:
                    #     self.webmanager = False

    def accept_connection(self):
        """
        Accept incoming connection and return emotion
        """
        client_socket, client_address = self.manager.accept()
        self.connection_list.append(client_socket)
        self.addresses[client_socket] = client_address
        print("connected to: " + str(client_address))
        emote = "emote:"+emotions.emotion
        client_socket.send(bytes(emote,"utf-8"))


if __name__ == '__main__':
    manager = Manager('', 50000)
    emotions = emotions.Emotions('display')
    emotions.start()
    try:
        manager.manage()
    except KeyboardInterrupt:
        sys.exit(1)