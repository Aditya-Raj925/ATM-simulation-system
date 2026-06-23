#!/bin/bash
echo "Compiling Java files..."
javac *.java
if [ $? -ne 0 ]; then
    echo "Compilation failed. Please check the errors above."
    exit 1
fi
echo "Compilation successful. Starting ATM..."
echo ""
java Main
