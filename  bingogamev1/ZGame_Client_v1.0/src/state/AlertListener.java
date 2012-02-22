package state;

public interface AlertListener {
	void alertEventPerform(int alertType, int eventType, int alertId);
}
