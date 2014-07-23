#!/bin/bash

# Create the keystore
keytool -keystore keystore-local.jks -alias fuds-local -genkey -keyalg RSA -storepass dummypass -keypass dummypass -dname CN=localhost || exit 1

# Extract the certificate for the clients (e.g. curl --cacert fuds-local.cacert
keytool -keystore keystore-local.jks -export -alias fuds-local -rfc -file fuds-local.cacert -storepass dummypass
