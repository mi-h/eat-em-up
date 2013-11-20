var gameData = (function() {
	var users = {} //Key - Name; Team - Value

	function getUsers() {
		return users;
	}
	function setUsers(userArr) {
		users = userArr;
	}
	function isTeamRed(playerName) {
		var isTeamRed = false;
		$.each( users, function( index, player ) {
			if(player.username==playerName) {
				isTeamRed = player.teamRed;
			}
		});
		return isTeamRed;
	}
	
	//public module functions (API)
	return {
		getUsers : getUsers,
		setUsers : setUsers,
		isTeamRed : isTeamRed
	}
})();