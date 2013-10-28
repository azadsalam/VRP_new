public abstract class SelectionOperator 
{
	boolean survivalSelection;
	boolean mark[];
	
	public void initialise(Individual[] population,boolean survivalSelection)
	{
		
	}
	
	abstract public Individual getIndividual(Individual[] population);
	
}
