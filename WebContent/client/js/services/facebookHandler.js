var facebookHandler = (function() {
	function init() {
		$.ajaxSetup({ cache: true });
		$.getScript('//connect.facebook.net/de_DE/all.js', function(){
		    FB.init({
		      appId: '325957930876505',
		      //channelUrl: '//gfoellner.funpic.de/eatemup/channel.html',
		      channelUrl: '//localhost:8080/eatemup/channel.html',
		      status: true,
		      cookie     : true, // enable cookies to allow the server to access the session
			  oauth      : true, // enable OAuth 2.0
			  xfbml      : true  // parse XFBML
		    });     
		    
		  // Here we subscribe to the auth.authResponseChange JavaScript event. This event is fired
		  // for any authentication related change, such as login, logout or session refresh. This means that
		  // whenever someone who was previously logged out tries to log in again, the correct case below 
		  // will be handled. 
		  FB.Event.subscribe('auth.authResponseChange', authResponseChange); 
		  FB.Event.subscribe('auth.logout', logoutEvent);
		});
	}
	
	function login() {
		console.log("Login to Facebook!");
		//check statuså
		FB.getLoginStatus(function(response) {
			if (response.status == 'connected') {
				requestUserName(function(username) {
					setAccountData(response, username);
					notifyServerForLogin();
			    });
		  } else if (response.status == 'not_authorized') {
		  	doLogin();
		  } else {
		  	doLogin();
		  }
		});
	}
	
	function doLogin() {
		FB.login(function(response) {
            if (response.authResponse) {             
               //Do stuff
              requestUserName(function(username) {
	              setAccountData(response, username);
	              notifyServerForLogin();
              });
            } else {
				//do nothing now
            }
        });
	}
	
	function notifyServerForLogin() {
		$.mobile.showPageLoadingMsg("a", "Daten werden geprüft...");
		amplify.publish('Login', {username: accountData.getUsername(), type: "facebook", facebookid: accountData.getFacebookID()});
	}
	
	function setAccountData(response, username) {
		var uid = response.authResponse.userID;
		accountData.setIsFacebookAccount(true);
		accountData.setFacebookID(uid);
		accountData.setUsername(username);			
	}
	
	function logout() {
		FB.logout();
	}
	
	function logoutEvent() {
		console.log("Logout from Facebook!");
		$.mobile.changePage("#loginSelectionPage", { transition: "slide", changeHash: true });
	}
	
	function authResponseChange(response) {
		// Here we specify what we do with the response anytime this event occurs. 
		    if (response.status === 'connected') {
		      // The response object is returned with a status field that lets the app know the current
		      // login status of the person. In this case, we're handling the situation where they 
		      // have logged in to the app.
		      console.log("Connected to Facebook!");
		    //  $.mobile.changePage("#selectPage", { transition: "pop", changeHash: true });
		    } else if (response.status === 'not_authorized') {
		      // In this case, the person is logged into Facebook, but not into the app, so we call
		      // FB.login() to prompt them to do so. 
		      // In real-life usage, you wouldn't want to immediately prompt someone to login 
		      // like this, for two reasons:
		      // (1) JavaScript created popup windows are blocked by most browsers unless they 
		      // result from direct interaction from people using the app (such as a mouse click)
		      // (2) it is a bad experience to be continually prompted to login upon page load.
		   //   $.mobile.changePage("#loginSelectionPage", { transition: "pop", changeHash: true });
		    } else {
		      // In this case, the person is not logged into Facebook, so we call the login() 
		      // function to prompt them to do so. Note that at this stage there is no indication
		      // of whether they are logged into the app. If they aren't then they'll see the Login
		      // dialog right after they log in to Facebook. 
		      // The same caveats as above apply to the FB.login() call here.
		  //    $.mobile.changePage("#loginSelectionPage", { transition: "pop", changeHash: true });
		    }
	}
	
	// Here we run a very simple test of the Graph API after login is successful. 
	// This testAPI() function is only called in those cases. 
	function requestUserName(callback) {
	    FB.api('/me', function(response) {
	      callback(response.name);
	    });
	}  
	  	
				
	//public module functions (API)
	return {
		init : init,
		login : login,
		logout : logout
	}
})();