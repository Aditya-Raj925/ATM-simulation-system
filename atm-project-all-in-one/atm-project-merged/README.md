# ATM Simulation System (Java)

A console-based ATM simulation demonstrating object-oriented design,
PIN-based security, and explicit session state management — built for
a Java programming course project.

## Features
- Balance inquiry, cash withdrawal, cash deposit, PIN change, mini statement
- PIN verification with auto-lock after 3 failed attempts
- Daily withdrawal limit and denomination checks
- Transaction history per account
- Clear session state machine (IDLE → CARD_INSERTED → AUTHENTICATED → ...)

## Class Structure

| Class          | Responsibility                                                  |
|----------------|-------------------------------------------------------------------|
| `Account`      | Stores balance, PIN, transaction log; handles deposit/withdraw logic |
| `Card`         | Wraps an Account; tracks failed PIN attempts and block status   |
| `Bank`         | Repository of all cards/accounts (acts as the "database")       |
| `SessionState` | Enum of all possible ATM session states                         |
| `ATM`          | Drives the state machine, handles all user I/O                  |
| `Main`         | Entry point — wires up Bank + ATM and starts the session         |

## Session State Machine

```
IDLE --(insert valid card)--> CARD_INSERTED --(correct PIN)--> AUTHENTICATED
  ^                                |  (3x wrong PIN)               |
  |                                v                                v
  |                          CARD_BLOCKED                TRANSACTION_IN_PROGRESS
  |                                |                                |
  +-----------(card ejected / session ends)-----------SESSION_ENDED
```

## How to Compile & Run

All files are in **one folder** — no subfolders, no setup needed.

**Easiest way:**
- **Windows:** double-click `run.bat`
- **Mac/Linux:** open a terminal in this folder and run `./run.sh`

**Manual way (any OS):**
```bash
javac *.java
java Main
```

Just make sure you have the Java JDK installed (not just the JRE) — `javac` is the compiler that comes with the JDK.

## Demo Cards (pre-loaded test data)

| Card Number       | PIN  | Holder       | Starting Balance |
|--------------------|------|--------------|-------------------|
| 1111222233334444   | 1234 | Asha Verma   | Rs. 15,000.00     |
| 5555666677778888   | 4321 | Rohan Mehta  | Rs. 5,000.00      |
| 9999000011112222   | 0000 | Priya Singh  | Rs. 250,000.00    |

## Notes for Extension
- Swap `Bank`'s in-memory `HashMap` for a database (JDBC) to persist data.
- Add a `TransactionLogger` class if you want logs written to a file.
- Add multi-currency support by parameterizing `Account.formatCurrency`.
