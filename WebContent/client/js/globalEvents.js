//jQuery Mobile Page Events
//loginSelectionPage
$(document).on('pageinit', '#loginSelectionPage', function() {
	loginSelectionController.pageInit();
});

$(document).on('pagebeforeshow', '#loginSelectionPage', function() {
	loginSelectionController.pageBeforeShow();
});

$(document).on('pageshow', '#loginSelectionPage', function() {
	loginSelectionController.pageShow();
});

//loginPage
$(document).on('pageinit', '#loginPage', function() {
	loginController.pageInit();
});

$(document).on('pagebeforeshow', '#loginPage', function() {
	loginController.pageBeforeShow();
});

$(document).on('pageshow', '#loginPage', function() {
	loginController.pageShow();
});

//select page 
$(document).on('pageinit', '#selectPage', function() {
	selectController.pageInit();
});

$(document).on('pagebeforeshow', '#selectPage', function() {
	selectController.pageBeforeShow();
});

$(document).on('pageshow', '#selectPage', function() {
	selectController.pageShow();
});

//lobby page
$(document).on('pageinit', '#lobbyPage', function() {
	lobbyController.pageInit();
});

$(document).on('pagebeforeshow', '#lobbyPage', function() {
	lobbyController.pageBeforeShow();
});

$(document).on('pageshow', '#lobbyPage', function() {
	lobbyController.pageShow();
});

//game page
$(document).on('pageinit', '#gamePage', function() {
	gameController.pageInit();
});

$(document).on('pagebeforeshow', '#gamePage', function() {
	gameController.pageBeforeShow();
});

$(document).on('pageshow', '#gamePage', function() {
	gameController.pageShow();
});

//gamespectator page
$(document).on('pageinit', '#gameSpectatorPage', function() {
	gameSpectatorController.pageInit();
});

$(document).on('pagebeforeshow', '#gameSpectatorPage', function() {
	gameSpectatorController.pageBeforeShow();
});

$(document).on('pageshow', '#gameSpectatorPage', function() {
	gameSpectatorController.pageShow();
});

//highscore page
$(document).on('pageinit', '#highscorePage', function() {
	highscoreController.pageInit();
});

$(document).on('pagebeforeshow', '#highscorePage', function() {
	highscoreController.pageBeforeShow();
});

$(document).on('pageshow', '#highscorePage', function() {
	highscoreController.pageShow();
});

//sponsor page
$(document).on('pageinit', '#sponsorPage', function() {
	sponsorController.pageInit();
});

$(document).on('pagebeforeshow', '#sponsorPage', function() {
	sponsorController.pageBeforeShow();
});

$(document).on('pageshow', '#sponsorPage', function() {
	sponsorController.pageShow();
});

//document ready events
$(document).ready(function() {
	bindSocketMessages();
	socketConnection.establishConnection();
	facebookHandler.init();
});

//detect when back button of browser is pressed
//if current page is selectPage then logout
$(window).on("navigate", function (event, data) {
	  var direction = data.state.direction;
	  if (direction == 'back') {
		  var page = $.mobile.activePage.attr('id');
		  if (page === "selectPage") {
			  amplify.publish('Exit', {username : accountData.getUsername(), userid : accountData.getUserID()});
		  }
	  }
});


window.onbeforeunload = function() {
	return "Ein Verlassen bzw. eine Aktualisierung kann zu einem inkonsistenten Spielzustand führen!";
};

window.onunload = function() {
	//check if user is logged in
	if (accountData.getUsername()) {
		if (accountData.isFacebookAccount()) {
			facebookHandler.logout();
		} 
		amplify.publish('Exit', {username : accountData.getUsername(), userid : accountData.getUserID()});
		accountData.reset();
	}
};

//socket closed
function bindSocketMessages() {
	amplify.subscribe('SocketClosed', function() {
		amplify.publish('Exit', {username : accountData.getUsername(), userid : accountData.getUserID()});
		accountData.reset();
		$.mobile.changePage("#loginSelectionPage", {transition : "pop", changeHash : true});

	});
	amplify.subscribe('Ping', function (message) {
		amplify.publish('Pong', {username:message.username, userid:message.userid,secondAttempt:message.secondAttempt, pingid:message.pingid});
	});
}

//browser window events
$(window).on('resize', function() {
	if ($.mobile.activePage) {
		//resize map when window size changes
		if ($.mobile.activePage.attr('id') === "gamePage") {
			gameController.resizeMap();
		} else if ($.mobile.activePage.attr('id') === "gameSpectatorPage") {
			gameSpectatorController.resizeMap();
		}
	}
});