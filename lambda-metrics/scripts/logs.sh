#!/bin/bash
BASEDIR=$(dirname "$0")
cd $BASEDIR/..

serverless logs -f metricspublisher -t
