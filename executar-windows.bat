@echo off
call mvn clean package
if errorlevel 1 exit /b 1
java -cp target/classes br.edu.entregas.Main
