#!/bin/bash
(cd sumatera && mvn clean install ${1}) &&
(cd pojo-user && mvn clean install ${1})
exit 0
