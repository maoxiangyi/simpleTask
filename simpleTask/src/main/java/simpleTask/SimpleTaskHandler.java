package simpleTask;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * 模块名称： 一个简单的定时器 功能点：读取项目根目录下的simpleTask.properties文件，并运行文件中配置的任务信息 </br>
 * 版本：v0.02 </br> 
 * 		|--支持一次性任务，不支持周期性任务 </br> 
 * 		|--支持重新加载任务文件</br> 
 * 
 * @author www.maoxiangyi.cn
 * 
 */
public class SimpleTaskHandler {
	/* 任务类路径 */
	private static String className;
	/* 定时器休眠时间 */
	private static long sleepTime;
	/* 任务信息 */
	private static Map<Long, String> taskMap;
	/* 是否重新加载配置文件 */
	private static boolean isReload = false;
	
	static {
		load();// 加载任务配置文件
	}
	
	/**
	 * 启动定时器
	 */
	public static void start() {
		try {
			doTask();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 配置下一个将要执行的任务
	 */
	private static void getNextTask() {
		long time = nextTaskTime();// 获得将要执行任务的时间，即taskMap中的KEY
		if (time != 0) {// 如果时间符合要求，开始执行任务
			configTaskInfo(time, taskMap.get(nextTaskTime()));// 使用time作为Key，获得任务Map中的任务类路径
		} else {
			System.out.println("当前没有任务....");
		}
	}

	/**
	 * 配置当前要执行的任务信息
	 * 
	 * @param workTime
	 *            任务执行的时间戳
	 * @param classDir
	 *            任务类路径
	 */
	private static void configTaskInfo(long workTime, String classDir) {

		sleepTime = workTime - System.currentTimeMillis();// 计算线程休眠的时间
		className = classDir;// 将任务类路径赋值给成员变量
		taskMap.remove(workTime);// 删除执行过的任务
	}

	/**
	 * 定时器
	 * 
	 * @throws InterruptedException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws ClassNotFoundException
	 * @throws NoSuchMethodException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	private static void doTask() throws InterruptedException,
			SecurityException, IllegalArgumentException,
			ClassNotFoundException, NoSuchMethodException,
			IllegalAccessException, InvocationTargetException,
			InstantiationException {
		while (true) {
			// 线程开始休眠，休眠时间由即将要执行的任务时间-当前系统时间
			Thread.sleep(sleepTime);
			// 如果要执行的任务的类路径不为空，开始执行任务
			if (className != null) {
				run();
			}
			// 任务执行完毕之后，将任务类路径置为空，避免任务列表中的所有任务执行完毕之后，重复执行最后一个任务。
			className = null;
			// 任务执行完毕之后，判断是否需要重新加载任务配置文件
			if (isReload) {
				load();
			}
			// 执行下一个任务
			getNextTask();
		}
	}

	/**
	 * 获得下一个任务执行时间 
	 * 1：获得当前系统时间 
	 * 2：迭代任务列表中的所有任务，如果有任务大于当前时间，将任务时间记住。
	 * 迭代的过程中，保证被记住的任务时间永远是最小的。也就是即将要运行的任务时间。
	 * 
	 * @return
	 */
	private static Long nextTaskTime() {
		long currentTime = System.currentTimeMillis();
		long nextTaskTime = 0;
		Set<Long> keySet = taskMap.keySet();
		for (Long taskTime : keySet) {
			if (taskTime > currentTime) {
				//当nextTaskTime等于0时，将taskTime赋值给nextTaskTime，通常运行在定时器启动的第一次
				//当taskTime小于已经存在的nextTaskTime时，将taskTime赋值给nextTaskTime
				if (taskTime < nextTaskTime || nextTaskTime == 0) {
					nextTaskTime = taskTime;
				}
			}

		}
		return nextTaskTime;
	}

	/**
	 * 转换时间 转换simpleTask.properties文件中的KEY，Returns the number of milliseconds
	 * 
	 * @param key
	 * @return
	 * @throws ParseException
	 */
	private static Long fomartTime(String key) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd-HH-mm-ss");
		Date date = null;
		try {
			date = simpleDateFormat.parse(key);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date.getTime();
	}

	/**
	 * 执行任务 1：使用反射技术获得任务的Class，该类必须要集成Thread线程类
	 * 2：获得要任务类的start()方法，创建一个任务类的实例，并运行
	 * 
	 * @throws ClassNotFoundException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("unchecked")
	private static void run() {
		Class<? extends Thread> clazz;
		try {
			clazz = (Class<? extends Thread>) Class
					.forName(className);
			Method method = clazz.getMethod("start");
			method.invoke(clazz.newInstance());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 避免立即加载配置文件引发的错误，在上一个任务执行完毕之后重新加载配置文件
	 */
	public static void reload() {
		isReload = true;
	}

	/**
	 * 读取任务配置文件 
	 * 1：使用Properties对象加载项目根目录下的simpleTask.properties配置文件
	 * 2：转换加载后的键值对信息，将其中的Key转换成毫秒格式，并存放在任务map中。
	 * 3：加载完配置文件之后，将是否重新加载配置文件的标示标量置为false
	 * 
	 * 
	 * simpleTask.properties的文件格式 
	 * #key 格式：yyyy-MM-dd-HH-mm-ss 
	 * #value 任务类，任务类必须继承Thread 
	 * 列如：2013-07-17-10-06-00=cn.maoxiangyi.task.MyTask
	 */
	private static void load() {
		BufferedInputStream bif = new BufferedInputStream(Thread
				.currentThread().getContextClassLoader()
				.getResourceAsStream("simpleTask.properties"));
		Properties p = new Properties();
		try {
			p.load(bif);
			Set<Entry<Object, Object>> entrySet = p.entrySet();
			taskMap = new HashMap<Long, String>();
			for (Entry<Object, Object> entry : entrySet) {
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				taskMap.put(fomartTime(key), value);
			}
			p = null;
			bif.close();
			isReload = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws Exception {

		SimpleTaskHandler.start();// 启动任务管理器

	}

}
