package at.jku.se.eatemup.core.model.specialaction;

public class DoublePointsAction implements SpecialAction {

	@Override
	public String getName() {
		return "DoublePointsAction";
	}

	@Override
	public int getDuration() {
		return 60;
	}	
}
