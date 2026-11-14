@echo off
setlocal
cd /d "%~dp0"

if not exist "bin" mkdir "bin"
if exist "fontes.txt" del "fontes.txt"
dir /s /b "src\main\java\*.java" > "fontes.txt"

echo Compilando o projeto...
javac -encoding UTF-8 -d bin @fontes.txt
if errorlevel 1 (
    echo.
    echo Nao foi possivel compilar. Confira se o JDK esta instalado.
    pause
    exit /b 1
)

echo.
echo Iniciando o programa...
java -cp bin com.algoritmos.App
pause
