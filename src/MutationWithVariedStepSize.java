
public class MutationWithVariedStepSize 
{	
	
	void applyMutation(Individual offspring,int generation)
	{
//		int max = initialCount - generation / 50;		
		int max = 15;		

		int count = Utility.randomIntInclusive(max);
		
		//System.out.println("gen : " + generation + " max : " + max+" count : "+count);		

		int rand = 4;
		if(offspring.problemInstance.periodCount==1)rand=3;
		
		for(int i=0;i<count;i++)
		{
			int selectedMutationOperator = Utility.randomIntInclusive(rand);
			
			if(selectedMutationOperator==0)
			{
				offspring.mutateRoutePartition();
			}
			else if (selectedMutationOperator == 1)
			{
				offspring.mutatePermutation();
			}
			else if (selectedMutationOperator == 2)
			{
				offspring.mutatePermutationWithInsertion();
			}
			else if (selectedMutationOperator == 3)
			{
				offspring.mutateRoutePartitionWithRandomStepSize();
			}
			else if (selectedMutationOperator == 4)
			{
				offspring.mutatePeriodAssignment();
			}
		}
		
		offspring.calculateCostAndPenalty();
		
	}

}
