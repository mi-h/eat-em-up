var accountData = (function() {
	// getter/setter
	function setIsFacebookAccount(loginType) {
		sessionStorage.setItem("facebookAccount", loginType);
	}

	function isFacebookAccount() {
		return sessionStorage.getItem("facebookAccount");
	}

	function setPoints(inputPoints) {
		sessionStorage.setItem("points", inputPoints);
	}

	function getPoints() {
		return sessionStorage.getItem("points");
	}

	function setCode(inputCode) {
		sessionStorage.setItem("code", inputCode);
	}

	function getCode() {
		return sessionStorage.getItem("code");
	}

	function setUserID(inputUserID) {
		sessionStorage.setItem("userID", inputUserID);
	}

	function getUserID() {
		return sessionStorage.getItem("userID");
	}

	function setUsername(inputUsername) {
		sessionStorage.setItem("username", inputUsername);
	}

	function getUsername() {
		return sessionStorage.getItem("username");
	}

	function setFacebookID(fbID) {
		sessionStorage.setItem("facebookID", fbID);
	}

	function getFacebookID() {
		return sessionStorage.getItem("facebookID");
		;
	}

	function reset() {
		sessionStorage.removeItem("facebookAccount");
		sessionStorage.removeItem("points");
		sessionStorage.removeItem("code");
		sessionStorage.removeItem("userID");
		sessionStorage.removeItem("username");
		sessionStorage.removeItem("facebookID");
	}

	// public module functions (API)
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
		reset : reset
	}
})();