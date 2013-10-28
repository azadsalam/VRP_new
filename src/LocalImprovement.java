public abstract class LocalImprovement 
{
	LocalSearch localSearch;
	
	public abstract void initialise(Individual[] population,LocalSearch localSearch); 

	public void run(Individual[] population)
	{		
		Individual selected = selectIndividualForImprovement(population);
		localSearch.improve(selected);
	}
	
	public abstract Individual selectIndividualForImprovement(Individual[] population);
}
