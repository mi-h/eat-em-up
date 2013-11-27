var socketConnection = (function() {
	var connection = null;

	function establishConnection() {
		connection = new WebSocket('ws://localhost:8080/websocket');
		//connection = new WebSocket('ws://eat-em-up.marce155.eu.cloudbees.net/websocket');
		bindSocketEvents();
		bindSendMessages();
	}

	//web socket events
	function bindSocketEvents() {
		connection.onopen = function(event) {
			console.log("Websocket Opened!");
		}

		connection.onmessage = function(event) {
			console.log("message received " + event.data);
			var dataObject = JSON.parse(event.data);
			if (dataObject.type === "ReadyForGame") {
				if (accountData.isFacebookAccount()) {
					amplify.publish('ReadyForGameFacebook', dataObject.message);
				} else {
					amplify.publish('ReadyForGame', dataObject.message);
				}
			} else if (dataObject.type === "AlreadyLoggedIn") {
				if (accountData.isFacebookAccount()) {
					amplify.publish('AlreadyLoggedInFacebook', dataObject.message);
				} else {
					amplify.publish("AlreadyLoggedIn", dataObject.message);
				}
			} else if (dataObject.type === "Highscore") {
				amplify.publish('Highscore', dataObject.message);
			} 
			else if (dataObject.type=="GameStandbyUpdate") {
				amplify.publish("GameStandbyUpdate", dataObject.message);
		   }
		   else if (dataObject.type=="GameStartSurvey") {
				amplify.publish("GameStartSurvey", dataObject.message);
		   }
		   else if (dataObject.type==="Ping") {
				amplify.publish("Ping", dataObject.message);
		   }
		   else if (dataObject.type=="GameState") {
				amplify.publish("GameState", dataObject.message);
		   }
		   else if (dataObject.type=="Logout") {
				amplify.publish("Logout", dataObject.message);
		   }
		}

		connection.onerror = function(event) {
			console.log("Websocket Error!");
		}

		connection.onclose = function(event) {
			console.log("Websocket Closed!");
			amplify.publish('SocketClosed');
		}
	}

	//messages from client to server
	function bindSendMessages() {
		amplify.subscribe('Login', function(loginInfo) {
			var message = {
				type : "Login",
				message : loginInfo
			}
			sendLoginRequest(message);
		});

		amplify.subscribe('Exit', function(userInfo) {
			var message = {
				type : "Exit",
				message : userInfo
			}
			sendLogoutRequest(message);
		});

		amplify.subscribe('HighscoreRequest', function(requestInfo) {
			var message = {
				type : "HighscoreRequest",
				message : requestInfo
			}
			sendHighscoreRequest(message);
		});
		amplify.subscribe('Play', function (playInfo) {
			var message = {
					type:"Play",
					message: playInfo
			}
			sendPlayRequest(message);
		});
		amplify.subscribe('Position', function (posInfo) {
			var message = {
					type:"Position",
					message: posInfo
			}
			sendPositionRequest(message);
		});
		amplify.subscribe('LeaveGame', function (posInfo) {
			var message = {
					type:"LeaveGame",
					message: posInfo
			}
			sendLeaveGameMessage(message);
		});
		
		amplify.subscribe('RequestForGameStart', function (reqInfo) {
			var message = {
					type:"RequestForGameStart",
					message: reqInfo
			}
			sendRequestForGameStart(message);
		});
		
		//PING-PONG-System
		amplify.subscribe('Pong', function (reqInfo) {
			var message = {
					type:"Pong",
					message: reqInfo
			}
			sendPongRequest(message);
		});
	}

	function sendLoginRequest(message) {
		//send message
		console.log(JSON.stringify(message));
		connection.send(JSON.stringify(message));
	}

	function sendLogoutRequest(message) {
		//send message
		console.log(JSON.stringify(message));
		connection.send(JSON.stringify(message));

	}

	function sendHighscoreRequest(message) {
		//send message
		console.log(JSON.stringify(message));
		connection.send(JSON.stringify(message));
	}
	function sendRequestForGameStart(message) {
		//send message
		console.log(JSON.stringify(message));
		connection.send(JSON.stringify(message));
	}
	function sendPlayRequest(message) {
		//send message
		console.log(JSON.stringify(message));
		connection.send(JSON.stringify(message));
	}
	function sendPongRequest(message) {
		//send message
		console.log(JSON.stringify(message));
		connection.send(JSON.stringify(message));
	}
	
	function sendPositionRequest(message) {
		//send message
		console.log(JSON.stringify(message));
		connection.send(JSON.stringify(message));
	}
	function sendLeaveGameMessage(message) {
		console.log(JSON.stringify(message));
		connection.send(JSON.stringify(message));	
	}
	return {
		establishConnection : establishConnection
	}

})();
