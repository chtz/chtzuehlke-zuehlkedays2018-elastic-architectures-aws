#!/bin/bash
BASEDIR=$(dirname "$0")
$BASEDIR/s3_empty_bucket.sh $1
aws cloudformation delete-stack --stack-name $1
