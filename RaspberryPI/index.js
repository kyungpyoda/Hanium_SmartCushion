var mqtt = require('mqtt')
var client  = mqtt.connect('mqtt://169.56.84.167:1883')

const SerialPort = require('serialport')
const Readline = require('@serialport/parser-readline')
const port = new SerialPort('/dev/ttyACM0')

const parser = port.pipe(new Readline({deliniter: '\r\n'}))

client.on('connect', function () {
	//testSampling();
	// 아두이노 직렬 통신값
	parser.on('data', function(data) {
		client.publish('cushion', data);
	});
	client.subscribe('androidTopic1');
});
 
client.on('message', function (topic, message) {
	console.log(topic + ' ' + message);
});

/*
var testSampling = function() {
	setInterval(function() {
		client.publish('topic1', 'data');
	}, 500);
}
*/
