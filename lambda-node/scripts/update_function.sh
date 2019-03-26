#!/bin/bash
BASEDIR=$(dirname "$0")
cd $BASEDIR/..

serverless deploy function -f txt2speech
