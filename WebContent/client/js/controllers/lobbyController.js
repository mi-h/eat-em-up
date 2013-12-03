var lobbyController = (function() {
	var checkBoxIdCurrentUser="checkbox-0";

	//page events
	function pageInit() {
		//alert("page init login");

	}
	
	function bindServiceMessages() {
		lobbyGameResponse();
		gameStartSurveyResponse();
		gameStateResponse();
	}
	function play() {
		amplify.publish('Play', {username: accountData.getUsername(), userid:accountData.getUserID()});
	}
	function requestForGameStart(startGame) { //Param startGame bool - true if player is reader
		amplify.publish('RequestForGameStart', {username:accountData.getUsername(), userid:accountData.getUserID(), startGame:startGame});
	}
	function getCurrentPosition() {
	  if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(sendCurrentPosition, geolocationError, {enableHighAccuracy: true});
		
		  } else {
  		alert("Positionsbestimmung auf diesem Gerät nicht möglich");
  	}
		
	}
	function sendCurrentPosition(position) {
		var timestamp = new Date().getTime();
		accountData.setCurrentPosition(position);
		amplify.publish('Position', {username: accountData.getUsername(), userid:accountData.getUserID(), latitude:accountData.getCurrentPosition().coords.latitude, longitude:accountData.getCurrentPosition().coords.longitude, timestamp:timestamp});
	}
	function geolocationError(error){
	}
	function pageBeforeShow() {
	}
	
	function pageShow() {
		bindUIActions();
		bindServiceMessages();

		play();
	}
	
	function bindUIActions(){
		checkboxClicked();
		backButtonPressed();
	}
	function backButtonPressed() {
		$("#leaveLobby").on("click", function(event, ui) {
			amplify.publish('LeaveGame', {username:accountData.getUsername(), userid:accountData.getUserID()});
		});
	}
	function gameStateResponse() {
		amplify.subscribe('GameState', function (message) {
			//TODO: Objekt mit GameState ertellen
			var goodies = JSON.parse(JSON.stringify(message.goodies));
			gameState.setGoodies(goodies);
			var playerInfos = JSON.parse(JSON.stringify(message.playerInfo));
			gameState.setPlayerInfos(playerInfos);
			gameState.setTime(message.remainingTime);
			$.mobile.changePage("#gamePage", { transition: "pop", changeHash: true });
		});
	}
	function lobbyGameResponse() {
		amplify.subscribe('GameStandbyUpdate', function (message) {
			getCurrentPosition();
			var players = JSON.parse(JSON.stringify(message.players));
			gameData.setUsers(players);
			
			$("#countPlayer").text(""+gameData.getUsers().length);
			
			var blueHTML ='<fieldset id="blue-fieldset" data-role="controlgroup">';
			var redHTML = '<fieldset id="red-fieldset" data-role="controlgroup">';
			$.each( players, function( index, player ) {
				var isDisabled = "disabled";
				if(player.username==accountData.getUsername()) {
					isDisabled="";
					checkBoxIdCurrentUser="checkbox-"+accountData.getUserID();
				}
				if(!player.teamRed) {
					blueHTML+='<input type="checkbox" name="checkbox-'+player.userid+'" id="checkbox-'+player.userid+'" class="custom" '+isDisabled+'/><label for="checkbox-'+player.userid+'">'+player.username+'</label>';
				}
				else {
					redHTML+='<input type="checkbox" name="checkbox-'+player.userid+'" id="checkbox-'+player.userid+'" class="custom" '+isDisabled+'/><label for="checkbox-'+player.userid+'">'+player.username+'</label>';
				}
				var checkboxid = "checkbox-"+player.userid+"";
				$("#"+checkboxid).prop("checked",player.readyForStart).checkboxradio("refresh");
				
			});

			blueHTML += '</fieldset>';
			redHTML += '</fieldset>';
			
			$("#teamBlue").html(blueHTML);
			$('#teamBlue').trigger('create');
			$("#teamRed").html(redHTML);
			$('#teamRed').trigger('create');
			checkboxClicked();
			$.mobile.changePage("#lobbyPage", { transition: "pop", changeHash: true });
		});
	}
	function gameStartSurveyResponse() {
		amplify.subscribe('GameStartSurvey', function (message) {
			var players = JSON.parse(JSON.stringify(message.players));
			$.each( players, function( index, player ) {
				var checkboxid = "checkbox-"+player.userid+"";
				$("#"+checkboxid).prop("checked",player.ready).checkboxradio("refresh");
			});
		});

	}
	
	checkboxClicked = function(){
		var checkboxID = "#"+checkBoxIdCurrentUser;
		$(checkboxID).on("click", function(event, ui) {
			if ($(checkboxID).prop("checked") == true){
				$(checkboxID).prop("checked",true).checkboxradio("refresh");
				requestForGameStart(true);
				setTimeout(function(){
			//		$(checkboxID).prop("checked",false).checkboxradio("refresh");
			//		$.mobile.changePage("#gamePage", { transition: "pop", changeHash: true });
				},2000);	
			
				//player is ready
				
			}else{
				requestForGameStart(false);
			}				  
		});
	}
	
	//public module functions (API)
	return {
		pageInit : pageInit,
		pageBeforeShow : pageBeforeShow,
		pageShow : pageShow
	}
})();