var gameData = (function() {
	var users = {} //Key - Name; Team - Value

	function getUsers() {
		return users;
	}
	function setUsers(userArr) {
		users = userArr;
	}
	
	//public module functions (API)
	return {
		getUsers : getUsers,
		setUsers : setUsers
	}
})();