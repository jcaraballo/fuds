#!/bin/bash

cd "$( dirname "$0" )"
source function_ensure_no_uncommited_changes.bash


if [[ "$TRAVIS_BUILD_NUMBER" == "" ]] ; then
  FUDS_BUILD_NUMBER=dev-$(date --utc +%Y%m%d%H%M%S)-$(git rev-parse --short --verify HEAD)
else
  FUDS_BUILD_NUMBER=${TRAVIS_BUILD_NUMBER}-$(git rev-parse --short --verify HEAD)
fi

ensure_no_uncommited_changes

echo FUDS_BUILD_NUMBER: $FUDS_BUILD_NUMBER
FUDS_BUILD_NUMBER=$FUDS_BUILD_NUMBER sbt clean assembly
