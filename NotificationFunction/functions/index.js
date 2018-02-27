'use strict'
//import firebase functions modules
const functions = require('firebase-functions');
//import admin module
const admin = require ('firebase-admin');

admin.initializeApp(functions.config().firebase);

// Listens added
exports.sendNotification = functions.database.ref('/Notifications/{receiver_id}/{notification_id}').onWrite(event =>{
	
	const receiver_id = event.params.receiver_id;
	const notification_id = event.params.notification_id;
	console.log('We have a notification to send to : ', receiver_id);
	
	//  Grab the current value of what was written to the Realtime Database.
	if(!event.data.val()){
		return console.log('A notification has been deleted from the database : ', notification_id);
	}
	
	const sender_id = admin.database().ref(`/Notifications/${receiver_id}/${notification_id}`).once('value');
	
	return sender_id.then(fromUserResult =>{
		const from_sender_id = fromUserResult.val().from;
		console.log('You have new notification from: ', from_sender_id);
		
		 const senderUserQuery = admin.database().ref(`/Utilisateurs/${from_sender_id}/nom_Utilisateur`).once('value');
		 return senderUserQuery.then(senderUserNameResult =>{
			 //stocker le nom d'utilisateur dans une variable
			 const senderUserName = senderUserNameResult.val();
			 
			 
			 // Create a notification
			 const deviceToken = admin.database().ref(`/Utilisateurs/${receiver_id}/device_Token`).once('value');
			
			 return deviceToken.then(result => {
				const token_id = result.val();
				const payload = {
					notification:
					{
						title:"Nouvelle demande de contact",
						body:`${senderUserName} vous a envoyé une demande de contact`,
						icon: "default",
						click_action: "br.com.darksite.jazze_TARGET_NOTIFICATION"

					},
					data:{
						from_sender_id : from_sender_id
					}
				};
				 return admin.messaging().sendToDevice(token_id, payload).then(response => {
					console.log('This was the notification feature.');
				});
			});
		});
		
	});
	
	
});


