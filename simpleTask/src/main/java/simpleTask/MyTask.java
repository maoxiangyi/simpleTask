package simpleTask;

public class MyTask extends Thread {

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		System.out.println("task running!");
	}
	
}