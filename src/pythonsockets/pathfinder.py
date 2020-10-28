import os
import sys


def get_script_path(script=sys.argv[0]):
    """
    Get the directory of the script.
    Default is currently running script invoking the call.
    :return: The path to the directory of the script
    """
    path = os.path.realpath(script)
    return path

def get_parent_path(script=sys.argv[0]):
    """
    Get the parent directory of the script.
    Default is currently running script invoking the call.
    :return: The path to the parent directory of the script
    """
    parent = os.path.dirname(get_script_path(script))
    return parent

if __name__ == '__main__':
	path = get_parent_path()
	print(path)