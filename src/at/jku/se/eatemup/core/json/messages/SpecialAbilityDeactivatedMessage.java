package at.jku.se.eatemup.core.json.messages;

import at.jku.se.eatemup.core.json.CastType;
import at.jku.se.eatemup.core.json.MessageType;
import at.jku.se.eatemup.core.json.OutgoingMessage;

public class SpecialAbilityDeactivatedMessage extends OutgoingMessage {
	public String specialAbility;

	@Override
	public MessageType getType() {
		return MessageType.SpecialAbilityDeactivated;
	}

	@Override
	public CastType getCastType() {
		return CastType.Unicast;
	}
}
