package com.bj58.lbg.zk_task.task;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

import com.bj58.lbg.zk_task.core.entity.TaskData;
import com.bj58.lbg.zk_task.core.util.ByteUtil;
import com.bj58.lbg.zk_task.core.util.Constant;
import com.bj58.lbg.zk_task.core.util.PropertiesUtil;
import com.bj58.lbg.zk_task.task.service.TaskService;
import com.bj58.lbg.zk_task.task.util.ZookeeperTaskUtil;
import com.bj58.lbg.zk_task.task.watcher.TaskWatcher;

public class TaskExecute {
	
	/**
	 * 默认zk路径启动
	 * @param taskService
	 */
	public static void startup(TaskService taskService) {
		String rootPath = Constant.DEFAULT_ROOT_PATH;
		String taskPath = rootPath+Constant.DEFAULT_TASK_PATH;
		String schedulePath = rootPath+Constant.DEFAULT_SCHEDULE_PATH;
		startup(rootPath, taskPath, schedulePath, taskService);
	}
	
	/**
	 * 自定义zk路径启动
	 * @param pathIndex
	 * @param taskService
	 */
	public static void startup(int pathIndex, TaskService taskService) {
		String rootPath = PropertiesUtil.rootPath;
		String taskPath = PropertiesUtil.taskPaths.get(pathIndex);
		String schedulePath = PropertiesUtil.schedulePaths.get(pathIndex);
		startup(rootPath, taskPath, schedulePath, taskService);
	}
	
	/**
	 * 任务节点启动
	 * @param rootPath	根路径
	 * @param taskPath	任务路径
	 * @param taskPath	调度路径
	 * @param taskService 业务service
	 */
	public static void startup(String rootPath, String taskPath, String schedulePath, TaskService taskService) {
		try {
			CountDownLatch countDownLatch = new CountDownLatch(1);
			TaskWatcher watcher = new TaskWatcher(countDownLatch, taskService, taskPath, schedulePath);
			ZookeeperTaskUtil.init(watcher);
			ZooKeeper zk = ZookeeperTaskUtil.getZookeeper();
			if(zk.exists(rootPath, watcher) == null) {
				zk.create(rootPath, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			if(zk.exists(taskPath , watcher) == null) {
				zk.create(taskPath, ByteUtil.objectToByte(new ArrayList<TaskData>()), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				zk.exists(taskPath, watcher);
			}
			String nodeName = zk.create(taskPath+"/task_node", null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			watcher.setNodeName(nodeName.substring(nodeName.lastIndexOf("/")+1, nodeName.length()));
			countDownLatch.countDown();
			System.out.println("执行节点"+nodeName+"启动完成");
			while(true) {
				Thread.sleep(Integer.MAX_VALUE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("任务节点启动失败");
		} finally {
		}
		
	}
	
}