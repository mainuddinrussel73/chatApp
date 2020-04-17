'use strict'


const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);


exports.sendNotification = functions.database.ref('/Notifications/{receiver_user_id}/{notification_id}')
.onWrite((data, context) =>
{
	const receiver_user_id = context.params.receiver_user_id;
	const sender_user_id = data.after.val().from;
	const notification_id = context.params.notification_id;
    const postTitle = data.after.val().type;


	console.log('We have a notification to send to :' , receiver_user_id);


	if (!data.after.val()) 
	{
		console.log('A notification has been deleted :' , notification_id);
		return null;
	}

	const DeviceToken = admin.database().ref(`/Users/${receiver_user_id}/device_token`).once('value');




	return DeviceToken.then(result => 
	{
		const token_id = result.val();
		console.log(postTitle);

		console.log(sender_user_id)
		var userName = "nnnn";

		var starCountRef = admin.database().ref(`/Users/${sender_user_id}/name`);
		starCountRef.on('value', function(snapshot) {
		  userName =  snapshot.val();
		  console.log(snapshot.val());
		  console.log(userName);


			if(postTitle==="request")
			{
				console.log('request');
				const payload = 
				{
					notification:
					{
						title: `New Chat Request by ${userName}.`,
						body:  `You have a new Chat Request, please check`,
						icon: "default"
					}
				};

				return admin.messaging().sendToDevice(token_id, payload)
				.then(response => 
				{
					console.log('This was a notification feature.');
				});

			}
			else
			{
				console.log('message');
				const payload = 
				{
					notification:
					{
						title: `New Message by ${userName}.`,
						body:  postTitle,
						icon: "default"
					}
				};

				return admin.messaging().sendToDevice(token_id, payload)
				.then(response => 
				{
					console.log('This was a notification feature.');
				});
			}
		});
		
		

		

		
	});
});