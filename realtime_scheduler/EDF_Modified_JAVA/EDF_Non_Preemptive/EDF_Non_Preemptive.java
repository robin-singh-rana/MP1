/*
@author - robin
*/
import java.util.*;
import java.io.*;

class Task
{
	public String name;
	public int priority;
	public double arrival_time;
	public double exec_time;
	public double deadline;
	public double remaining_time;
	public double exec_cost;
	public double scheduling_parameter;

	public Task(String name, int priority, double arrival_time, double exec_time, double deadline)
	{
		this.name = name;
		this.priority = priority;
		this.arrival_time = arrival_time;
		this.exec_time = exec_time;
		this.deadline = deadline;
		this.remaining_time = exec_time;
	}
}

class TaskComparator implements Comparator<Task>
{
	@Override
	public int compare(Task t1, Task t2)
	{
		if(t1.scheduling_parameter > t2.scheduling_parameter)
			return 1;
		else if(t1.scheduling_parameter < t2.scheduling_parameter)
			return -1;

		return 0;
	}
}

class EDF_Non_Preemptive
{
	public static final double cloud_instance_cost = 0.0010083333333333;						// $0.049468/h E2 high-CPU machine types
	public static int deadline_miss_count = 0;
	public static int preemption_count = 0;
	public static List<Task> executed_tasks = new ArrayList<Task>();
	public static List<Task> deadline_miss_tasks = new ArrayList<Task>();
	public static List<Task> gantt_chart = new ArrayList<Task>();

	public static void calculateExecCost(List<Task> list_of_tasks)
	{
		for(Task t: list_of_tasks)
		{
			double cost = cloud_instance_cost/t.exec_time;
			t.exec_cost = cost;
		}
	}

	public static void calculateSchedulingParameter(List<Task> list_of_tasks)					// sum of deadline + exec_cost
	{
		calculateExecCost(list_of_tasks);

		for(Task t: list_of_tasks)
		{
			double parameter = t.deadline + t.exec_cost;
			t.scheduling_parameter = parameter;

			if(t.priority > 0)
				t.scheduling_parameter = 0;
		}
	}

	public static double edfnp(List<Task> list_of_tasks)
	{
		calculateSchedulingParameter(list_of_tasks);

		double current_time = 0;
		double total_burst_time = 0;
		double limit = 0;

		for(Task t:list_of_tasks)
			limit+=t.deadline;

		PriorityQueue<Task> min_heap = new PriorityQueue<Task>(new TaskComparator());

		while(current_time<(2*limit))
		{
			for(Task t:list_of_tasks)
			{
				if(t.arrival_time == current_time)
					min_heap.add(t);
			}

			if(min_heap.isEmpty() == false)
			{
				Task top_task = min_heap.poll();
				total_burst_time += top_task.exec_time;						// execute(top_task);

				double if_run = current_time+top_task.exec_time;
				for(double i=current_time+1; i<if_run; i++)
					for(Task t:list_of_tasks)
						if(t.arrival_time >= i && t.arrival_time <= i)
							min_heap.add(t);

				current_time += top_task.exec_time;

				gantt_chart.add(top_task);
				executed_tasks.add(top_task);

				if(current_time > top_task.deadline)
				{
					deadline_miss_count++;
					deadline_miss_tasks.add(top_task);
				}
			}
			else
				current_time+=1;											// check at every interval
		}

		return total_burst_time;
	}

	public static void main(String args[]) throws IOException
	{
		/*Task t1 = new Task("t1", 0, 0, 1, 3);
		Task t2 = new Task("t2", 0, 0, 1, 3);
		Task t3 = new Task("t3", 1, 0, 2, 3);

		List<Task> list_of_tasks = new ArrayList<Task>();
		list_of_tasks.add(t1);
		list_of_tasks.add(t2);
		list_of_tasks.add(t3);*/

		int prior[] = 		new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
		double arriv[] = new double[]{0,1,3,4,5,6,7,9,10,11,12,13,15,16,17,18,19,21,22,23,24,25,27,28,29};
		double exect[] = new double[]{2,1,3,2,5,3,2,5,3,2,2,1,3,2,5,3,2,5,3,2,2,1,3,2,5};
		double deadl[] = new double[]{3,2,8,7,13,18,16,21,23,25,28,27,33,32,38,43,41,46,48,50,53,52,58,57,62};

		List<Task> list_of_tasks = new ArrayList<Task>();

		int task_count = 1;

		for(int i=0; i<prior.length; i++)
		{
			String name = "t" + task_count++;
			list_of_tasks.add(new Task(name, prior[i], arriv[i], exect[i], deadl[i]));
		}

		System.out.println("Time Taken Overall = \t\t"+edfnp(list_of_tasks));

		System.out.print("Task Executed as: \t\t");
		for(Task t: executed_tasks)
			System.out.print(t.name+"->");

		System.out.println("\nTotal Deadlines Missed = \t"+deadline_miss_count);

		System.out.print("Task Which Missed Deadline: \t");
		for(Task t: deadline_miss_tasks)
			System.out.print(t.name+" ");

		System.out.println("\nNo. of preemptions = \t\t"+preemption_count);

		System.out.print("Gantt Chart as: \t\t");
		for(Task t: gantt_chart)
			System.out.print(t.name+"->");
	}
}