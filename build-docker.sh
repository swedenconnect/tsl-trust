#!/bin/bash
mvn clean install && mvn -f docker-sigval/pom.xml dockerfile:build dockerfile:push
