import socket

class Connection:
    """
    Tunnel class. For every connection a new connection object could e made using this class.
    """
    def __init__(self):
        """
        General connection settings setup
        """
        self.connection = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.connection.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        self.connection.setsockopt(socket.SOL_SOCKET, socket.SO_KEEPALIVE, 1)

    def connect(self, address):
        """
        Connect a specific socket to an address
        :param address: tuple of the ip and port.
        :return the socket.
        """
        try:
            self.connection.connect(address)
            return self.connection
        except socket.error as e:
            print("Could not connect to: " + str(address))
            # print(e)
            return False

    def listen(self, address, amount):
        """
        Let the socket listen to a specific address or port.
        :param address: tuple of the ip and port.
        :param amount: amount of incoming connections that can be accepted.
        :return the socket.
        """
        try:
            self.connection.bind(address)
            self.connection.listen(amount)
            return self.connection
        except socket.error as e:
            print("Could not bind socket to: " + str(address))
            # print(e)
            return False
