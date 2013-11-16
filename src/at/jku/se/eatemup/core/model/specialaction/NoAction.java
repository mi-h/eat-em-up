package at.jku.se.eatemup.core.model.specialaction;

public class NoAction implements SpecialAction {

	@Override
	public int getDuration() {
		return 0;
	}

	@Override
	public String getName() {
		return "NoAction";
	}

}
