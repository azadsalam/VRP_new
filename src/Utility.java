import java.util.Random;

public class Utility 
{
	static Random randomGenerator = new Random(System.currentTimeMillis());
	//returns a random numbor between [m,n] 
	
	public static int randomIntInclusive(int m,int n)
	{
		int random = randomGenerator.nextInt(n-m+1);
		return m+random;
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
		Individual temp;
		//FOR NOW DONE SELECTION SORT
		//AFTERWARDS REPLACE IT WITH QUICK SORT OR SOME OTHER O(n logn) sort
		for(int i=0;i<array.length;i++)
		{
			for(int j=i+1;j<array.length;j++)
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

}
