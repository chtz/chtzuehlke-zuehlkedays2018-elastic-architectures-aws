#!/bin/bash
BASEDIR=$(dirname "$0")
aws s3 rm s3://$($BASEDIR/stack_bucket_name.sh $1)/ --recursive
