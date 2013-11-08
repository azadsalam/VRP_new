import java.util.Random;
import java.util.Vector;

public class Utility 
{
	static Random randomGenerator = new Random(System.currentTimeMillis());
	//returns a random numbor between [m,n] 
	
	public static int randomIntInclusive(int m,int n)
	{
		int random = randomGenerator.nextInt(n-m+1);
		return m+random;
	}
	
	public static double randomDouble(double m,double n)
	{
		double random = randomGenerator.nextDouble();
		return m+random*(n-m);
	}
	
	//[0,n]
	public static int randomIntInclusive(int n)
	{
		int random = randomGenerator.nextInt(n+1);
		return random;
	}

	/**
	 * Sorts in increasing order of cost, 
	 * BETTER INDIVIDUALS HAVE LOWER INDEX, best individual at 0
	 * Assumption : All individuals cost+penalty is calculated
	 * 
	*/
	public static void sort(Individual[] array)
	{
		sort(array, array.length);
	}
	
	/**
	 * Sorts in increasing order of cost+penalty in rance [0, length)
	 * @param array
	 * @param length
	 */
	
	public static void sort(Individual[] array,int length)
	{
		Individual temp;
		//FOR NOW DONE SELECTION SORT
		//AFTERWARDS REPLACE IT WITH QUICK SORT OR SOME OTHER O(n logn) sort
		for(int i=0;i<length;i++)
		{
			for(int j=i+1;j<length;j++)
			{
				if(array[i].costWithPenalty > array[j].costWithPenalty)
				{
					temp = array[i];
					array[i] =array[j];
					array[j] = temp;
				}
			}
		}

	}
	
	public static void sort(Vector<Individual> vec)
	{
		Individual temp;
		//FOR NOW DONE SELECTION SORT
		//AFTERWARDS REPLACE IT WITH QUICK SORT OR SOME OTHER O(n logn) sort
		for(int i=0;i<vec.size();i++)
		{
			for(int j=i+1;j<vec.size();j++)
			{
				if(vec.get(i).costWithPenalty > vec.get(j).costWithPenalty)
				{
					temp = vec.get(i);
					vec.set(i, vec.get(j));
					vec.set(j, temp);

				}
			}
		}

	}

	
	public static void concatPopulation(Individual target[],Individual[] first,Individual[] second)
	{
		for(int i=0;i<first.length;i++) 
			target[i] = first[i];
		for(int i=0;i<second.length;i++)
			target[i+first.length] = second[i];
	}
}	
	