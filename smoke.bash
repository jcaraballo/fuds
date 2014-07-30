#!/bin/bash

cd "$( dirname "$0" )"
source function_moan.bash

temp=$( mktemp -d )
echo Using temporary directory ${temp}. Starting fuds in the background.
java -jar target/scala-2.11/fuds-*.jar --port 8888 --storage ${temp}/storage --https src/test/resources/certs/keystore-local.jks:dummypass >>fuds.log 2>&1 &
fuds_pid=$!

mkdir ${temp}/storage && echo foo >${temp}/storage/foo || moan "Failed to create foo"

times_to_try=10
until curl -s3 --cacert src/test/resources/certs/fuds-local.cacert --fail https://localhost:8888/foo >${temp}/downloaded_foo || [ $times_to_try -eq 0 ]; do
  echo Retrying foo retrieval up to $(( times_to_try-- )) times >&2
  sleep 1
done

if [ $times_to_try -eq 0 ] ; then
  moan "Never managed to download foo"
fi

cmp ${temp}/storage/foo ${temp}/downloaded_foo || moan 'Comparison between original and downloaded foo failed'

kill $fuds_pid
