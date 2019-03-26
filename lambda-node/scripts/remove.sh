#!/bin/bash
BASEDIR=$(dirname "$0")
cd $BASEDIR/..

BUCKET=$(aws cloudformation describe-stacks --stack-name txt2speechjs-dev | jq -r '.Stacks[].Outputs[] | select(.OutputKey=="Bucket") | .OutputValue')
aws s3 rm s3://$BUCKET/ --recursive

serverless remove
