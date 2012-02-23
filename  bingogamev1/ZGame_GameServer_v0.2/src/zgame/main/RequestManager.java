package zgame.main;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import zgame.socket.handle.SocketServerHandle;

public class RequestManager {
  private BlockingQueue<Runnable> requestHandlers;
  
  private ThreadPoolExecutor executor;
  
  private static RequestManager singleton;
  
  public static RequestManager instance() {
    if (singleton == null) {
      singleton = new RequestManager();
    }
    return singleton;
  }
  
  private RequestManager() {
    requestHandlers = new ArrayBlockingQueue<Runnable>(100);
    
    executor = new ThreadPoolExecutor(Global.REQUEST_MANAGER_CORE_POLL_SIZE, Global.REQUEST_MANAGER_MAX_POLL_SIZE, 
          Global.REQUEST_MANAGER_KEEP_ALIVE_TIME, TimeUnit.MINUTES, requestHandlers);
  }
  
  public void addRequest(SocketServerHandle handle) {
    executor.execute(handle);
  }
}
