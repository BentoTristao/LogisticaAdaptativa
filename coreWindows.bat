@echo off
:: Arquivo de comandos para compilação e execução do projeto
:: Windows - Sem Maven

if not exist bin mkdir bin

echo Compilando...
javac -cp "src;core.jar" -d bin/ src/*.java

echo Executando...
java -cp "bin;core.jar" -ea Sketch
