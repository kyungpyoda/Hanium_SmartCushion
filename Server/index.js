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
        //ver 1.1
        var currentdate = new Date();
        var meg_date = currentdate.getFullYear() + ':' + (currentdate.getMonth()+1) + ':' + currentdate.getDate();
        var meg_time = currentdate.getHours() + ':' + currentdate.getMinutes() + ':' + currentdate.getSeconds();

        console.log(topic + ' : ' + message);
        dbo.collection("test").insertOne(
                {//Data Schema
                        //user : next version ; multi user,
                        date : meg_date,//message date ver1.1
                        time : meg_time,//message time ver1.1
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

//time Scheduler ver1.1
var scheduler = require('cron').CronJob;

//const job = new scheduler('00 00 */1 * * *', function(){
const job = new scheduler('* * * * * *',function(){
        //DB
        var date = new Date();
        var tmp_date = date.getFullYear() + ':' + (date.getMonth()+1) + ':' + date.getDate();
        var tmp_time = date.getHours() + ':';
        var query = {
                date : tmp_date//, 변수로 넣으면 검색이안됨...
//              time : new RegExp('^' + tmp_time)
        };

        console.log('find Value : ' + tmp_date + ' ' + tmp_time);//debug

        dbo.collection('test').find(query).toArray().then((docs) => {
                console.log(docs);
        }).catch((err) => {
                console.log(err)
        });
});
job.start();
