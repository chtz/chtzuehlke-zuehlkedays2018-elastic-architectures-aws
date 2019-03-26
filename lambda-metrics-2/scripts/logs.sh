#!/bin/bash
BASEDIR=$(dirname "$0")
cd $BASEDIR/..

serverless logs -f metricspublisher2 -t
