#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"
mkdir -p bin
find src/main/java -name "*.java" > fontes.txt
javac -encoding UTF-8 -d bin @fontes.txt
java -cp bin com.algoritmos.App
