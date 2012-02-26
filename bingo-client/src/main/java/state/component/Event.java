package state.component;

public class Event {
	protected Object source;
	protected String action = "";

	public Event(Object source, String action) {
		this.source = source;
		this.action = action;
	}

	public Object getSource() {
		return source;
	}

	public void setSource(Object source) {
		this.source = source;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
