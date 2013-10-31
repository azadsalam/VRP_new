
public class FirstChoiceHillClimbing extends LocalSearch {

	
	@Override
	public void improve(Individual initialNode, double loadPenaltyFactor, double routeTimePenaltyFactor) 
	{
		// TODO Auto-generated method stub

		//Mutation mutation = new Mutation();
		int retry=0;
		
		Individual node,neighbour;
		node = new Individual(initialNode);
		
		while(retry<5)
		{			
			neighbour = new Individual(node);
			applyMutation(neighbour);
			TotalCostCalculator.calculateCost(neighbour, loadPenaltyFactor, routeTimePenaltyFactor);
			
			//better
			if(neighbour.costWithPenalty < node.costWithPenalty)
			{
				node = neighbour;
				retry=0;
			}
			else
			{
				retry++;
			}
		}
		
		initialNode.copyIndividual(node);
		
	}

	
	void applyMutation(Individual offspring)
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
}
