package development.bean;

import java.util.Vector;

public class Entity {
	protected String id;
	protected String name;
	protected int maxUser = Integer.MAX_VALUE;
	protected int concurrentUser;

	protected Entity parent = null;
	protected Vector childs = new Vector();

	public Entity(String id) {
		this.id = id;
	}

	public Entity getPrent() {
		return parent;
	}

	public void putChild(Entity entity) {
		childs.addElement(entity);
	}

	public Entity getChild(int index) {
		return (Entity) childs.elementAt(index);
	}

	public int numberOfChilds() {
		return childs.size();
	}

	public int getConcurrentUser() {
		return concurrentUser;
	}

	public void setConcurrentUser(int concurrentUser) {
		this.concurrentUser = concurrentUser;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getMaxUser() {
		return maxUser;
	}

	public void setMaxUser(int maxUser) {
		this.maxUser = maxUser;
	}

	public void clearChildList() {
		childs.removeAllElements();
	}
}
