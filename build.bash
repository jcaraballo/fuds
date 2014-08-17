#!/bin/bash

# CI-oriented build script

cd "$( dirname "$0" )"

if [[ "$TRAVIS_PULL_REQUEST" == 'false' && "$TRAVIS_BRANCH" == 'master' ]] ; then
  ./dist.bash && ./smoke.bash && ./publish.bash
else
  ./dist.bash && ./smoke.bash
fi

