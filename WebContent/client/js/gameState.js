var gameState = (function() {
	var goodies = {};
	//{"specialAction":"NoAction","longitude":14.318532943725586,"points":25,"latitude":48.33748844154077}
	var playerInfos = {}; //"playerInfo":[{"position":{"longitude":14.28583,"latitude":48.30694},"username":"user2","facebookuser":false,"userid":"a76ef01c-f8bd-425f-9af2-19b4199d9884","facebookimage":[],"points":0},{"position":{"longitude":14.28583,"latitude":48.30694},"username":"user1","facebookuser":false,"userid":"76961368-f40d-4acb-9086-e325a32a2f47","facebookimage":[],"points":0}]
	var remainingTime; //in seconds
		
	function setTime(time) {
		remainingTime = time;
	}
	function getTime() {
		return remainingTime;
	}
	function getGoodies() {
		return goodies;
	}
	function setGoodies(goodiesArr) {
		goodies = goodiesArr;
	}
	function setPlayerInfos(playerInfoArr) {
		playerInfos = playerInfoArr;
	}
	function getPlayerInfos() {
		return playerInfos;
	}
	function getMMSSTime() { //converts seconds to MM:SS-format
		var minutes = Math.floor(remainingTime / 60);
		var seconds = remainingTime  - (minutes * 60);
		if (minutes < 10) {minutes = "0"+minutes;}
		if (seconds < 10) {seconds = "0"+seconds;}
		return minutes+':'+seconds;
	}
		// public module functions (API)
	return {
		getGoodies : getGoodies,
		setGoodies : setGoodies,
		setTime : setTime,
		getTime : getTime,
		setPlayerInfos : setPlayerInfos,
		getPlayerInfos : getPlayerInfos,
		getMMSSTime : getMMSSTime
	}

})();