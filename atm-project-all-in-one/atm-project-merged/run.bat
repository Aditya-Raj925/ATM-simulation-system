@echo off
echo Compiling Java files...
javac *.java
if %errorlevel% neq 0 (
    echo Compilation failed. Please check the errors above.
    pause
    exit /b 1
)
echo Compilation successful. Starting ATM...
echo.
java Main
pause
