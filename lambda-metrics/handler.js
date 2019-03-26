'use strict';

let AWS = require('aws-sdk');
let sqs = new AWS.SQS();
var cloudwatch = new AWS.CloudWatch();

module.exports.metricspublisher = async (event, context) => {
  try {
    console.log("begin", event);

    var params = {
      QueueUrl: process.env.SQS_QUEUE,
      AttributeNames: ['ApproximateNumberOfMessages']
    };
    var sqsData = await sqs.getQueueAttributes(params).promise();

    var params2 = {
      MetricData: [
        {
          MetricName: process.env.METRIC_NAME,
          Dimensions: [
            {
              Name: 'queue',
              Value: process.env.SQS_QUEUE
            }
          ],
          StorageResolution: 60,
          Timestamp: new Date,
          Unit: 'Count',
          Value: sqsData.Attributes.ApproximateNumberOfMessages
        }
      ],
      Namespace: process.env.NAMESPACE
    };
    var cwResult = await cloudwatch.putMetricData(params2).promise();

    console.log("success", cwResult);
  }
  catch (err) {
    console.log("error", err, err.stack);
  }
  return;
};
