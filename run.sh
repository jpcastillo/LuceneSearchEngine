#!/bin/bash

# Current directory
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )";

# Compile
javac -d $DIR/ $DIR/Main.java $DIR/Parser.java;

# Run
# Note to self, cp stands for classpath and it is a colon delimited list of directories
java -cp $DIR/:$DIR/jsoup-1.7.3.jar:$DIR/lucene-core-4.8.1.jar:$DIR/lucene-analyzers-common-4.8.1.jar Main;