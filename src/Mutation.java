
public class Mutation 
{	
	//ProblemInstance problemInstance;
	
	
	/*
	public Mutation(ProblemInstance problemInstance) 
	{
		// TODO Auto-generated constructor stub
		this.problemInstance = problemInstance;
	}
	*/
	
	
	void applyMutation(Individual offspring)
	{
		int selectedMutationOperator = Utility.randomIntInclusive(4);
		
		if(selectedMutationOperator==0)
		{
			offspring.mutateRoutePartition();
		}
		else if (selectedMutationOperator == 1)
		{
			offspring.mutatePermutationWithinSingleRoute();
		}
		else if (selectedMutationOperator == 2)
		{
			offspring.mutatePermutationOfDifferentRoute();
		}
		else if (selectedMutationOperator == 3)
		{
			offspring.mutatePeriodAssignment();
		}
		else
		{
			
		}
		
		
	}

}
