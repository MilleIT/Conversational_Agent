import sys
import pathfinder

if __name__ == '__main__':
    print("pythonLoc: ")
    print(sys.executable[:-4])
    print("pythonPath: ")
    print(pathfinder.get_parent_path())
    print("replace the values in the python.kt file in furhatos.app.fruitseller with the above values or emotions will not work")
    print("run main.py in this same directory from a terminal BEFORE starting the furhatos skill, main.py does not need to be restarted everytime you restart a skill")
    print("if main.py does not work, make sure no other program is using your webcam(and that you have one)")