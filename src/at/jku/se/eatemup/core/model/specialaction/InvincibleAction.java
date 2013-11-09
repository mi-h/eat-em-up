package at.jku.se.eatemup.core.model.specialaction;

public class InvincibleAction implements SpecialAction {

	@Override
	public String getName() {
		return "InvincibleAction";
	}

	@Override
	public int getDuration() {
		return 30;
	}

}
