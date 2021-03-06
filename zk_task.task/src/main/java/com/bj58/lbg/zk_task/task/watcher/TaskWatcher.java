package com.bj58.lbg.zk_task.task.watcher;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;

import com.bj58.lbg.zk_task.task.service.TaskService;
import com.bj58.lbg.zk_task.task.util.ZookeeperTaskUtil;

/**
 * 任务执行监听器
 * @author 常博
 *
 */
public class TaskWatcher implements Watcher {

	
	private ExecutorService pool = Executors.newCachedThreadPool();
	private String nodeName;
	private CountDownLatch countDownLatch;
	private TaskService taskService;
	private String taskPath;
	private String schedulePath;
	
	public TaskWatcher(CountDownLatch countDownLatch, TaskService taskService, String taskPath, String schedulePath) {
		this.countDownLatch = countDownLatch;
		this.taskService = taskService;
		this.taskPath = taskPath;
		this.schedulePath = schedulePath;
	}
	
	/**
	 * process是阻塞方法
	 * 这里要注意,业务处理是开辟线程处理的，所以List<TaskData>里的任务会并发执行，这里要注意同时放到List<TaskData>的任务不能有执行顺序的要求
	 * 未来如果支持了任务的优先级，那么也要注意，同一优先级的任务也不能有执行顺序的要求
	 */
	public void process(WatchedEvent event) {
		System.out.println("TaskWatcher event " + event.getPath() + " " + event.getType() + " " + event.getState());
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(event.getType() == EventType.NodeDataChanged) {
			if (event.getPath().equals(taskPath)) {
				if(taskService != null) {
					taskService.initProperties(ZookeeperTaskUtil.getZookeeper(), this, nodeName, taskPath, schedulePath);
				}
				pool.execute(taskService);
			}
		}
	}

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

}
