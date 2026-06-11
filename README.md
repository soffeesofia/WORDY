# WORDY — CORBA-Based Word Game

A distributed word game application built with Java CORBA (Common Object Request Broker Architecture), supporting both a Java GUI client and a non-Java Python client. Game data is persisted in a MySQL database.

Developed as a Final Group Project for CS/IT 222 at Saint Louis University, Baguio City.

---

## Team Members

- Arellano, Mark Gian
- Balagtey, Gregg Andres
- Bosaing, Ryeth
- Chegyem, Roger
- Marquez, John Andrei
- Surro, Jaymee Sofia

**Instructor:** Mr. Roderick Makil  
**Course:** CS/IT 222

---

## Overview

WORDY is a client-server word game that communicates over CORBA using an Object Request Broker. The server manages game logic and a MySQL database, while clients (Java or Python) connect through the ORBD running on port 9999.

---

## Tech Stack

| Component | Technology |
|---|---|
| Language (Server & Java Client) | Java (SDK 1.8) |
| Language (Non-Java Client) | Python 3.9 |
| Middleware | CORBA (omniORB / omniORBpy) |
| Database | MySQL |
| GUI | Java Swing / AWT |
| ORB Daemon | ORBD (Java SDK 1.8) |

---

## Prerequisites

- Java SDK 1.8
- WampServer and MySQL Workbench
- Python 3.9 (for the Python client)
- omniORBpy 4.3.0 (for the Python client)
- Pillow (for the Python client)
- PyCharm (recommended IDE for the Python client)

---

## Setup

### Database

1. Open WampServer and start the local server.
2. Open MySQL Workbench and connect to `localhost:3306`.
3. Go to **Server > Data Import**, select **Import from Self-Contained File**, and locate `lib/wordy.sql`.
4. Set the target schema to `wordy` and click **Start Import**.

### Python Client Environment (omniORBpy)

1. Install Python 3.9 from https://www.python.org/downloads/release/python-390/
2. Add the omniORB `bin` directory to your system `PATH`:
   ```
   omniORBpy-4.3.0\bin\x86_win32
   ```
3. Add the omniORBpy library directory as a new system variable `PYTHONPATH`:
   ```
   omniORBpy-4.3.0\lib\python
   ```
4. Add the path to `python310.dll` to your `PATH` variable.
5. Verify the setup by running `echo %PATH%` and `omniidl -h` in CMD.
6. Install the omnipy package:
   ```
   pip install omnipy
   ```
7. Add the omniORB package in PyCharm under the project's Python interpreter settings.
8. Generate client stubs from the IDL file:
   ```
   omniidl -bpython <nameOfTheFile>.idl
   ```

---

## Running the Application

### Running the Server

1. Complete the database setup above.
2. Start the ORBD from the Java SDK 1.8 `bin` directory:
   ```
   orbd -ORBInitialPort 9999 -ORBInitialHost localhost
   ```
3. Set the program arguments for `WordyServer` in your IDE:
   ```
   -ORBInitialPort 9999 -ORBInitialHost localhost
   ```
4. Run `WordyServer.java`.

### Running the Java Client

1. Ensure ORBD is running (see above).
2. Set the program arguments for `WordyClient` in your IDE:
   ```
   -ORBInitialPort 9999 -ORBInitialHost localhost
   ```
3. Run `WordyClient.java`.

### Running the Python Client

1. Ensure ORBD is running and `WordyServer` is up.
2. Open the Python client project in PyCharm.
3. Set the script parameters for `Client.py`:
   ```
   -ORBInitRef NameService=corbaname::localhost:9999
   ```
4. Set the environment variable `PYTHONUNBUFFERED=1` and select Python 3.9 as the interpreter.
5. Run `Client.py`.

---

## License

This project was created for academic purposes at Saint Louis University. All rights reserved by the respective authors.
