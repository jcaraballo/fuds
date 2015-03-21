#!/bin/bash

cd "$( dirname "$0" )"
source function_moan.bash

function show_logs(){
  echo 'Smoke test logs:' 1>&2
  cat fuds.log 1>&2
}

function show_logs_and_moan(){
  show_logs
  moan "$1"
}

temp=$( mktemp -d )
echo Using temporary directory ${temp}. Starting fuds in the background.
java -jar target/scala-2.11/fuds-*.jar --port 8888 --storage ${temp}/storage --https src/test/resources/certs/keystore-local.jks:dummypass >>fuds.log 2>&1 &
fuds_pid=$!

mkdir ${temp}/storage && echo foo >${temp}/storage/foo || show_logs_and_moan "Failed to create foo"

times_to_try=10
until curl -v -s3 --cacert src/test/resources/certs/fuds-local.cacert --fail https://localhost:8888/foo >${temp}/downloaded_foo || [ ${times_to_try} -eq 0 ]; do
  echo Retrying foo retrieval up to $(( times_to_try-- )) times >&2
  sleep 1
done

if [ ${times_to_try} -eq 0 ] ; then
  show_logs_and_moan "Never managed to download foo"
fi

cmp ${temp}/storage/foo ${temp}/downloaded_foo || show_logs_and_moan 'Comparison between original and downloaded foo failed'

kill ${fuds_pid} || show_logs_and_moan "Failed to kill fuds with pid ${fuds_pid}"

echo All good

show_logs
