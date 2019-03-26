'use strict';

let AWS = require('aws-sdk');
var cloudwatch = new AWS.CloudWatch();
var autoscaling = new AWS.AutoScaling();

module.exports.metricspublisher2 = async (event, context) => {
  try {
    console.log("begin", event);

    var endTime = new Date();
    var startTime = new Date();
    startTime.setMinutes(startTime.getMinutes() - 1);

    var params3 = {
      EndTime: endTime,
      MetricName: 'MessagesProcessed',
      Namespace: 'Txt2Speech',
      Period: 60,
      StartTime: startTime,
      Dimensions: [
        {
          Name: 'queue',
          Value: process.env.QUEUE_NAME
        }
      ],
      Statistics: [ 'Sum' ]
    };
    
    var cwData2 = await cloudwatch.getMetricStatistics(params3).promise();

    if (cwData2.Datapoints[0]) {
      var transactions = cwData2.Datapoints[0].Sum;
  
      var params = {
        AutoScalingGroupNames: [
          process.env.AUTOSCALING_GROUP
        ]
       };
      var asData = await autoscaling.describeAutoScalingGroups(params).promise();
  
      console.log("Auto scaling group capacity", asData.AutoScalingGroups[0].DesiredCapacity);
  
      var params2 = {
        MetricData: [
          {
            MetricName: "MessagesProcessedPerNode",
            Dimensions: [
              {
                Name: 'queue',
                Value: process.env.QUEUE_NAME
              }
            ],
            StorageResolution: 60,
            Timestamp: new Date,
            Unit: 'Count',
            Value: transactions / asData.AutoScalingGroups[0].DesiredCapacity /* FIXME actual capa */
          }
        ],
        Namespace: "Txt2Speech"
      };
      var cwResult = await cloudwatch.putMetricData(params2).promise();
  
      var params4 = {
        MetricData: [
          {
            MetricName: "MessageProcessingNodes",
            Dimensions: [
              {
                Name: 'queue',
                Value: process.env.QUEUE_NAME
              }
            ],
            StorageResolution: 60,
            Timestamp: new Date,
            Unit: 'Count',
            Value: asData.AutoScalingGroups[0].DesiredCapacity /* FIXME actual capa */
          }
        ],
        Namespace: "Txt2Speech"
      };
      var cwResult2 = await cloudwatch.putMetricData(params4).promise();
    }

    console.log("success", cwResult);
  }
  catch (err) {
    console.log("error", err, err.stack);
  }
  return;
};
