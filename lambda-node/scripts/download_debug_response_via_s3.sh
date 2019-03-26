#!/bin/bash
BUCKET=$(aws cloudformation describe-stacks --stack-name txt2speechjs-dev | jq -r '.Stacks[].Outputs[] | select(.OutputKey=="Bucket") | .OutputValue')
aws s3 cp s3://$BUCKET/hallo hallo.mp3
aws s3 rm s3://$BUCKET/hallo
