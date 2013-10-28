
public class Mutation 
{	
	ProblemInstance problemInstance;
	
	public Mutation(ProblemInstance problemInstance) 
	{
		// TODO Auto-generated constructor stub
		this.problemInstance = problemInstance;
	}
	
	// for now not applying periodAssignment Mutation operator
	// for now working with only MDVRP ->  period = 1
	//0 -> route partition
	//1 ->	permutation
	//2 -> route partition + permutation
	//3 -> none
	void applyMutation(Individual offspring)
	{
		int selectedMutationOperator = Utility.randomIntInclusive(3);
		
		if(selectedMutationOperator==0)
		{
			int ran = Utility.randomIntInclusive(problemInstance.periodCount-1);
			offspring.mutateRoutePartition(ran);
		}
		else if (selectedMutationOperator == 1)
		{
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			offspring.mutatePermutation(period);//for now single period
		}
		else if (selectedMutationOperator == 2)
		{
			//int client = Utility.randomIntInclusive(problemInstance.customerCount-1);
			//offspring.mutatePeriodAssignment(client);
			
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			offspring.mutateRoutePartition(period);
			offspring.mutatePermutation(period);//for now single period			
		}
		else if (selectedMutationOperator == 3){}
		
	}

}
