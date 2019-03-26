#!/bin/bash
BASEDIR=$(dirname "$0")
cd $BASEDIR/..
serverless logs -f txt2speech -t
