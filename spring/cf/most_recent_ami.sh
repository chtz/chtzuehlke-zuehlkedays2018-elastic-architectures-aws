#!/bin/bash
aws ec2 describe-images --owners self --filters Name=name,Values=txt2speech* | jq -r '.Images[] | .CreationDate + " " + .ImageId' | sort | tail -n 1 | cut -d' ' -f2
