package at.jku.se.eatemup.core.model.specialaction;

public class NoAction implements SpecialAction {

	@Override
	public String getName() {
		return "NoAction";
	}

	@Override
	public int getDuration() {
		return 0;
	}

}
