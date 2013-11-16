package at.jku.se.eatemup.core.model.specialaction;

public class DoublePointsAction implements SpecialAction {

	@Override
	public int getDuration() {
		return 60;
	}

	@Override
	public String getName() {
		return "DoublePointsAction";
	}	
}
