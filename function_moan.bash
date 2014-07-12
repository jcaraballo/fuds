#!/bin/bash

function moan(){
  echo -e "$1" 1>&2
  exit 1
}
