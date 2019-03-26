#!/bin/bash
QUEUE=$(aws cloudformation describe-stacks --stack-name txt2speechjs-dev | jq -r '.Stacks[].Outputs[] | select(.OutputKey=="Queue") | .OutputValue')
aws sqs send-message --queue-url $QUEUE --message-body '{"id":"hallo","text":"hallo"}'
