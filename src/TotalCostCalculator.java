public class TotalCostCalculator 
{	
	/**
	 * Enumerates each individual and calculate the cost with penalty for each individual in population
	 * @param population
	 * @param loadPenaltyFactor
	 * @param routeTimePenaltyFactor
	 */
	public static void calculateCostofPopulation(Individual[] population, double loadPenaltyFactor, double routeTimePenaltyFactor)
	{
		double penalty;
		for(int i=0; i<population.length; i++)
		{
			population[i].calculateCostAndPenalty();
			
			penalty = 0;
			penalty += population[i].totalLoadViolation * loadPenaltyFactor;
			penalty += population[i].totalRouteTimeViolation * routeTimePenaltyFactor;
			//penalty *= (generation+1);
			
			population[i].costWithPenalty = population[i].cost + penalty;
			
		}
	}

}
