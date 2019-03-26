#!/bin/bash
aws cloudformation describe-stacks --stack-name $1 | jq -r '.Stacks[].Outputs[] | select(.OutputKey=="Queue") | .OutputValue'

