var accountData = (function() {
	var facebookAccount = false;
	var userID = "";
	var username = "";
	var facebookID = "";
	var points = 0;
	var code = "";
	
	
	//getter/setter
	function setIsFacebookAccount(loginType) {
		facebookAccount = loginType;
	}
	
	function isFacebookAccount() {
		return facebookAccount;
	}
	
	function setPoints(inputPoints) {
		points = inputPoints;
	}
	
	function getPoints() {
		return points;
	}
	
	function setCode(inputCode) {
		code = inputCode;
	}
	
	function getCode() {
		return code;
	}
	
	function setUserID(inputUserID) {
		userID = inputUserID;
	}
	
	function getUserID() {
		return userID;
	}
	
	function setUsername(inputUsername) {
		username = inputUsername;
	}
	
	function getUsername() {
		return username;
	}
	
	function setFacebookID(fbID) {
		facebookID = fbID;
	}
	
	function getFacebookID() {
		return facebookID;
	}
	
	function resetUserData() {
		console.log("reset");
		facebookAccount = false;
		userID = "";
		username = "";
		facebookID = "";
		points = 0;
		code = "";
	}
	
	
	//public module functions (API)
	return {
		setIsFacebookAccount : setIsFacebookAccount,
		isFacebookAccount : isFacebookAccount,
		setUserID : setUserID,
		getUserID : getUserID,
		setUsername : setUsername,
		getUsername : getUsername,
		setFacebookID : setFacebookID,
		getFacebookID : getFacebookID,
		setPoints : setPoints,
		getPoints : getPoints,
		setCode : setCode,
		getCode : getCode,
		resetUserData : resetUserData
	}
})();