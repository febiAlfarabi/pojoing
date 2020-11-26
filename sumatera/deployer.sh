#!/bin/bash
mvn clean install && mvn release:prepare && mvn release:perform
exit 0
