'use strict';

let uuidV4 = require('uuid/v4')
let AWS = require('aws-sdk');
let s3 = new AWS.S3();
let polly = new AWS.Polly();

module.exports.txt2speech = async (event, context) => {
  let records = event["Records"];
  let record = records[0];
  let message = JSON.parse(record["body"]);

  try {
    var pollyData = await polly.synthesizeSpeech({
      OutputFormat: "mp3",
      Text: message.text,
      VoiceId: "Marlene"
    }).promise();

    console.log("polly success", pollyData);

    var s3Data = await s3.putObject({
      Body: pollyData.AudioStream,
      Bucket: process.env.RESPONSE_BUCKET,
      Key: message.id == null ? uuidV4() : message.id,
      ACL: "public-read",
      ContentType: pollyData.ContentType
    }).promise();

    console.log("s3 success", s3Data);
  }
  catch (err) {
      console.log("Error (ignored)", err, err.stack);
  }
  return; 
};
