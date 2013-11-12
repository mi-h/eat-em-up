package at.jku.se.eatemup.core.json;

public enum MessageType {
	Login, ReadyForGame, GameStandbyUpdate, RequestForGameStart, 
	GameStartSurvey, Position, GameState, PlayerMoved, PlayerHasEaten, 
	TimerUpdate, SpecialActionActivated, SpecialActionDeactivated, BattleStart, 
	BattleAnswer, BattleResult, GameEnd, Exit, Logout, Play, FollowGameRequest, 
	GoodieCreated, HighscoreRequest, Highscore, GameStateRequest
}
