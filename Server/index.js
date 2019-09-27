var MongoClient = require('mongodb').MongoClient;
var url = "mongodb://localhost:27017/test";
var dbo

MongoClient.connect(url,{ useNewUrlParser: true }, function(err, db) {
  if (err) throw err;
  dbo = db.db("test");
});

//*******************************//
//              mqtt             //
//*******************************//
var mqtt = require('mqtt');
var url = 'mqtt://localhost:1883';
var client = mqtt.connect(url);
client.on('connect',function(data){
        client.subscribe('cushion');
});
client.on('message',function(topic, message){
        //ver 1.1
        var currentdate = new Date();
        var meg_date = currentdate.getFullYear() + ':' + (currentdate.getMonth()+1) + ':' + currentdate.getDate();
        var meg_time = currentdate.getHours() + ':' + currentdate.getMinutes() + ':' + currentdate.getSeconds();

        console.log(topic + ' : ' + message);
        dbo.collection("test").insertOne(
                {//Data Schema
                        //user : next version ; multi user,
                        date: meg_date,//message date ver1.1
                        time: meg_time,//message time ver1.1
                        position: parseInt(message)
                },
                function(err, res){
                        if(err)
                        {
                                console.error(err);
                                return;
                        }
                        else
                                console.log('MQTT_insert_success : ' + message);
        });
});

//*******************************//
//          scheduler            //
//*******************************//
var scheduler = require('cron').CronJob;

//const job = new scheduler('00 00 */1 * * *', function(){
const job = new scheduler('*/10 * * * * *',function(){
        //DB
        var date = new Date();
        var tmp_date = date.getFullYear() + ':' + (date.getMonth()+1) + ':' + date.getDate();
        var tmp_time = (date.getHours()-1) + ':';
        var query = {
                date : tmp_date,
                time : new RegExp('^' + tmp_time)
        };

        console.log('time is done, find Value : ' + tmp_date + ' ' + tmp_time);//debug
        //find time db //debug
        dbo.collection('test').find(query).toArray().then((docs) => {
                console.log(docs);
        }).catch((err) => {
                console.log(err)
        });

        //count function position에서 제일 많은 값 계산
        //ver1.3
        dbo.collection('test').aggregate([
                {$match:{date: tmp_date , time: new RegExp('^'+tmp_time)}},
                {$group: {_id: '$position', count:{$sum: 1}}},
                {$sort: {count: -1}},
                {$limit: 1}
        ]).toArray().then((docs) =>{
                console.log('------------------avg_db');
                console.log(docs[0]);
        //update avg collection
                dbo
                        .collection('avg')
                        .updateOne(
                                //해당쿼리 찾기
                                {_id: tmp_date},
                                //Schema
                                {$set:{_id: tmp_date, [tmp_time]: docs[0]._id}},
                                {upsert: true},function(err)
                                {
                                        if(err)
                                        {
                                                //insert error시, 앉아있지 않은것으로 판단, -1 기입
                                                dbo
                                                        .collection('avg')
                                                        .updateOne(
                                                        {_id: tmp_date},
                                                        {$set:{ _id: tmp_date,[tmp_time]: -1}},
                                                        {upsert: true});
                                        }
                                        else
                                                console.log('avg_insert ok');
                                }
                        );
                console.log('------------------------');
        }).catch((err) => {
                console.log('aggregate error : ' + err)
                //aggregate error시, 사용자 부재로 판단, -1기입
                dbo
                        .collection('avg')
                        .updateOne(
                                {_id: tmp_date},
                                {$set:{_id: tmp_date, [tmp_time]: -1}},
                                {upsert: true}
                        );
        });
});

//********************************//
//            http ver1.2         //
//********************************//


var express = require('express');
var app = express();
var bodyParser = require('body-parser');
app.use(bodyParser.urlencoded({extended: false}));

app.get('/data/:id/:date', function (req, res) {
        dbo
                .collection('avg')
                .find({_id: req.params.date})
                .toArray(function(err,items){
                        res.send(items);
                });
        console.log('app find : '+req.params.date);
        //res.send({id: '',date: '2019-9-22',position: '1234567890123456789012345678901234'});
});

app.listen(3000, function () {
  console.log('http start');
});


job.start();
