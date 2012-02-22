package zgame.bean;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class Entity {
  private static final Logger log = Logger.getLogger(Entity.class);
  
	protected String id;
	protected String name;
	protected int maxUser = Integer.MAX_VALUE;
	
	protected Entity parent = null;
	protected Map<String, Entity> childs = new HashMap<String, Entity>();
	protected Map<String, User> users = new HashMap<String, User>();
	
	public Entity(String id) {
		this.id = id;
	}
	
	public Entity getParent() {
		return parent;
	}
	
	public void addChild(Entity entity) {
		childs.put(entity.getId(), entity);
	}
	
	public Entity getChild(String id) {
		return childs.get(id);
	}
	
	public Collection<Entity> getChilds() {
		return childs.values();
	}
	
	public Map<String, Entity> getChildsMap() {
		return childs;
	}
	
	public void addUser(User user) {
		if (user == null) {
			log.warn("ERROR: Entity : addUser : user is NULL");
			return;
		}
		users.put(user.getName(), user);
		user.entity = this;
	}
	
	public User getUser(String userName) {
		return users.get(userName);
	}
	
	public void removeUser(User user) {
		if (user == null) {
			log.warn("ERROR: Entity : removeUser : user is NULL");
			return;
		}
		
		users.remove(user.getName());
		user.entity = parent;
	}
	
	public int getConcurrentUser() {
		return users.size();
	}
	
	public Collection<User> getUsers() {
		return users.values();
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
}
