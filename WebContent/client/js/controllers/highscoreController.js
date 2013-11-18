var highscoreController = (function() {
	
	//page events
	function pageInit() {
		bindUIActions();
		bindServiceMessages();
	}
	
	function pageBeforeShow() {
		$("#highscoreList li").remove();
		$('#highscoreList').listview('refresh');	
		amplify.publish('HighscoreRequest', {username: accountData.getUsername(), 
											 userid: accountData.getUserID(),
											 topx: 5});
	}
	
	function pageShow() {
		$.mobile.showPageLoadingMsg("a", "Highscore wird geladen");
	}
	
	function bindUIActions() {
	}
	
	function bindServiceMessages() {
		amplify.subscribe('Highscore', function (message) {
			processHighscoreData(message);
		});
	}
	
	function processHighscoreData(message) {
		var highscores = message.highscore;
		var username, points;
		for (var i=0; i<highscores.length; i++){
			username = highscores[i].username;
			points = highscores[i].points;
			
			$('#highscoreList').append('<li><img class=\'sponsoricon\' src=\'img/playerImg.png\'><h2>'+
										username+'</h2><h3><span class=\'totalPoints\'>'+
										points+' Punkte</h3></li>');			
		}
		$('#highscoreList').listview('refresh');
		$.mobile.hidePageLoadingMsg();
	}	
		
	//public module functions (API)
	return {
		pageInit : pageInit,
		pageBeforeShow : pageBeforeShow,
		pageShow : pageShow
	}
})();