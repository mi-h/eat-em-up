var loginController = (function() {
	
	//page events
	function pageInit() {
		//alert("page init login");
		bindUIActions();
		bindServiceMessages();
		
		$("#loginForm").validate({focusInvalid: false,
			  submitHandler: function(form) {
			  	login();
			  }	  
		});
	}
	
	function pageBeforeShow() {
	}
	
	function pageShow() {
		//alert("page show login");
	}
	
	function bindUIActions() {
	}
	
	function bindServiceMessages() {
		loginResponseMsg();
	}
	
	function loginResponseMsg() {
		amplify.subscribe('ReadyForGame', function (message) {
		//	$.mobile.hidePageLoadingMsg();
			if (message.loginSuccess) {
				//prepare select view
				accountData.setUserID(message.userid);
				accountData.setUsername(document.getElementById("usernameField").value);
				accountData.setPoints(message.points);
				accountData.setCode(message.adCode);
				$.mobile.changePage("#selectPage", { transition: "pop", changeHash: true });
			}else {
				//display loginFailure
				accountData.reset();
				$("#loginFailedPopup").popup("open");
			}
		});
	}
	
	function login() {
		var username = document.getElementById("usernameField").value;
		var password = document.getElementById("passwordField").value;
		$.mobile.showPageLoadingMsg("a", "Daten werden geprüft...");
		amplify.publish('Login', {username: username, password: password, type: "standard"});
	}
	
		
	//public module functions (API)
	return {
		pageInit : pageInit,
		pageBeforeShow : pageBeforeShow,
		pageShow : pageShow
	}
})();