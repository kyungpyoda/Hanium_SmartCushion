var MongoClient = require('mongodb').MongoClient;
var url = "mongodb://localhost:27017/test";
var dbo


MongoClient.connect(url,{ useNewUrlParser: true }, function(err, db) {
  if (err) throw err;
  dbo = db.db("test");
});



//mqtt
var mqtt = require('mqtt');
var url = 'mqtt://localhost:1883';
var client = mqtt.connect(url);
client.on('connect',function(data){
        client.subscribe('cushion');
});
client.on('message',function(topic, message){
        console.log(topic + ' : ' + message);
        dbo.collection("test").insertOne(
                {//Data Schema
                        //user : next version ; multi user,
                        //date : data insert date,
                        //time : data insert time,
                        position : parseInt(message)
                },
                function(err, res){
                        if(err)
                        {
                                console.error(err);
                                return;
                        }
                        else
                                console.log('success : ' + message);
        });
});
