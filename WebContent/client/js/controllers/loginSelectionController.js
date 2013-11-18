var loginSelectionController = (function() {
	
	//page events
	function pageInit() {
		bindUIActions();
		bindServiceMessages();
	}
	
	function pageBeforeShow() {
	}
	
	function pageShow() {
	}
	
	function bindUIActions(){
		fbLoginClicked();
	}
	
	function bindServiceMessages() {
		loginResponseMsg();
	}
	
	function loginResponseMsg() {
		amplify.subscribe('ReadyForGameFacebook', function (message) {
			$.mobile.hidePageLoadingMsg();
			if (message.loginSuccess) { //fb login success
				//prepare select view
				$.mobile.changePage("#selectPage", { transition: "pop", changeHash: true });
			}else {
				//display loginFailure
				accountData.reset();
				$("#loginFailedPopup").popup("open");
			}
		});
	}
		
	fbLoginClicked = function(){
		$("#fbLoginButton").on("click", function(event, ui) {
			facebookHandler.login(); 
		});
		
		$("#standardLoginButton").on("click", function(event, ui) {
			$.mobile.changePage("#loginPage", { transition: "slide", changeHash: true }); 
		});
	}
	
	//public module functions (API)
	return {
		pageInit : pageInit,
		pageBeforeShow : pageBeforeShow,
		pageShow : pageShow
	}
})();