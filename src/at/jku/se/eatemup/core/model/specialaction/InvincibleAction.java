package at.jku.se.eatemup.core.model.specialaction;

public class InvincibleAction implements SpecialAction {

	@Override
	public int getDuration() {
		return 30;
	}

	@Override
	public String getName() {
		return "InvincibleAction";
	}

}
