#!/bin/bash
(cd sumatera && mvn clean install) &&
(cd pojo-user && mvn clean install)
exit 0
