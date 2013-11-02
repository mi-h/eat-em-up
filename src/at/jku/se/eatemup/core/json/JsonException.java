package at.jku.se.eatemup.core.json;

public abstract class JsonException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4555510802776197164L;
	private String text = "";

	public String getLogText() {
		StringBuilder sb = new StringBuilder();
		if (this.text != null && !this.text.equals("")) {
			sb.append(this.text);
			sb.append("</br>");
		}
		int max = this.getStackTrace().length;
		int cnt = 0;
		for (StackTraceElement ste : this.getStackTrace()) {
			sb.append(ste.toString());
			cnt++;
			if (cnt < max) {
				sb.append("</br>");
			}
		}
		return sb.toString();
	}

	public void setText(String text) {
		this.text = text;
	}
}
