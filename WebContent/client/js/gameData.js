var gameData = (function() {
	var users = {} //Key - Name; Team - Value

	function getUsers() {
		return users;
	}
	function setUsers(userArr) {
		users = userArr;
	}
	function isTeamRead(playerName) {
		$.each( players, function( index, player ) {
			if(player.username==playerName) {
				return player.teamRed;
			}
		});
		return false;
	}
	
	//public module functions (API)
	return {
		getUsers : getUsers,
		setUsers : setUsers,
		isTeamRead : isTeamRead
	}
})();