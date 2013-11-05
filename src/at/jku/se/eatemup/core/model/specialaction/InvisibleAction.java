package at.jku.se.eatemup.core.model.specialaction;

public class InvisibleAction implements SpecialAction {

	@Override
	public String getName() {
		return "InvisibleAction";
	}

	@Override
	public int getDuration() {
		return 30;
	}

}
