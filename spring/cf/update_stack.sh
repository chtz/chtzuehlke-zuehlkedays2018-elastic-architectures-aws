#!/bin/bash
BASEDIR=$(dirname "$0")
MOST_RECENT_AMI_ID=$($BASEDIR/most_recent_ami.sh)
DEFAULT_VPC_ID=$(aws ec2 describe-vpcs --filters "Name=isDefault, Values=true" | jq -r ".Vpcs[0].VpcId")
DEFAULT_VPC_SUBNET_IDS=$(aws ec2 describe-subnets --filters Name=vpc-id,Values=$DEFAULT_VPC_ID | jq -j '.Subnets[].SubnetId|.+="\\,"' | ruby -ne 'puts $_[0..-3]')
aws cloudformation update-stack  \
  --parameters ParameterKey=BackendAMI,ParameterValue=$MOST_RECENT_AMI_ID \
  ParameterKey=VPCId,ParameterValue=$DEFAULT_VPC_ID \
  ParameterKey=SubnetIds,ParameterValue=$DEFAULT_VPC_SUBNET_IDS \
  --capabilities CAPABILITY_IAM  \
  --stack-name $1 --template-body file://$BASEDIR/txt2speech.json
