#!/bin/bash

# CI-oriented build script

cd "$( dirname "$0" )"

./dist.bash && ./smoke.bash
