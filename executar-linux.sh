#!/usr/bin/env bash
set -e
mvn clean package
java -cp target/classes br.edu.entregas.Main
