
public class MutationWithVariedStepSize 
{	
	int initialCount=210;
	
	void applyMutation(Individual offspring,int generation)
	{
		int max = initialCount - generation/12;		
		int count = Utility.randomIntInclusive(max);
		
		System.out.println("gen : " + generation + " max : " + max+" count : "+count);		

		
		for(int i=0;i<count;i++)
		{
			int selectedMutationOperator = Utility.randomIntInclusive(3);
			
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
		}
		
		offspring.calculateCostAndPenalty();
		
	}

}
