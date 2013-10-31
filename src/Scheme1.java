import java.io.PrintWriter;
import java.util.Scanner;


public class Scheme1 implements GeneticAlgorithm
{
	//Algorithm parameters
	int POPULATION_SIZE = 200; 
	int NUMBER_OF_OFFSPRING = 200;   
	int NUMBER_OF_GENERATION = 1000;	
	double loadPenaltyFactor = 10;
	double routeTimePenaltyFactor = 1;

	
	//Algorithm data structures
	Individual population[];
	Individual offspringPopulation[];
	Individual parentOffspringTotalPopulation[];

	//Operators
	Mutation mutation;
    SelectionOperator parentSelectionOperator;
    SelectionOperator survivalSelectionOperator;
    LocalImprovement localImprovement;
    LocalSearch localSearch;
	
	//Utility Functions	
	PrintWriter out; 
	ProblemInstance problemInstance;

	//Temprary Variables
	Individual parent1,parent2;
	

	
	public Scheme1(ProblemInstance problemInstance) 
	{
		// TODO Auto-generated constructor stub
		this.problemInstance = problemInstance;
		out = problemInstance.out;


		mutation = new Mutation();
		
		
		//Change here if needed
		population = new Individual[POPULATION_SIZE];
		offspringPopulation = new Individual[NUMBER_OF_OFFSPRING];		
		parentOffspringTotalPopulation = new Individual[POPULATION_SIZE + NUMBER_OF_OFFSPRING];
		
		//Add additional code here
		parentSelectionOperator = new RoutletteWheelSelection();
	    //survivalSelectionOperator = new FUSS(); 

		localSearch = new FirstChoiceHillClimbing();
		localImprovement = new LocalImprovementBasedOnFuss(loadPenaltyFactor, routeTimePenaltyFactor, localSearch, POPULATION_SIZE);	
	}

	public Individual run() 
	{
		int i,generation;
		
		Individual offspring1,offspring2;

		PopulationInitiator.initialisePopulation(population, POPULATION_SIZE, problemInstance);
		TotalCostCalculator.calculateCostofPopulation(population,0, POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor) ;
		
		for( generation=0;generation<NUMBER_OF_GENERATION;generation++)
		{
			//For collecting min,max,avg
			Solver.gatherExcelData(population, POPULATION_SIZE, generation);
			TotalCostCalculator.calculateCostofPopulation(population,0, POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor) ;
			
			parentSelectionOperator.initialise(population,false);
			
			i=0;
			while(i<NUMBER_OF_OFFSPRING)
			{
				parent1 = parentSelectionOperator.getIndividual(population);
				parent2 = parentSelectionOperator.getIndividual(population);
				
				offspring1 = new Individual(problemInstance);
				offspring2 = new Individual(problemInstance);
				
			
				Individual.crossOver(problemInstance, parent1, parent2, offspring1, offspring2);	
				
				mutation.applyMutation(offspring1);
				mutation.applyMutation(offspring2);
				
				offspringPopulation[i] = offspring1;
				i++;
				offspringPopulation[i] = offspring2;
				i++;
			}

			TotalCostCalculator.calculateCostofPopulation(offspringPopulation, 0,NUMBER_OF_OFFSPRING, loadPenaltyFactor, routeTimePenaltyFactor) ;
			Utility.concatPopulation(parentOffspringTotalPopulation, population, offspringPopulation);
			
			
			localImprovement.initialise(parentOffspringTotalPopulation);
			localImprovement.run(parentOffspringTotalPopulation);
			
			TotalCostCalculator.calculateCostofPopulation(population, 0, POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor);
			
			
			//semi elitist + Uniform approach, the n portion of best individuals always make to next generation
			int n =  30 ;
			int rand;
			
			Utility.sort(parentOffspringTotalPopulation);
			
			for(i=0;i<n;i++)
				population[i] = parentOffspringTotalPopulation[i];
			
			for(i=n;i<POPULATION_SIZE;i++)
			{
				rand  = Utility.randomIntInclusive(n, POPULATION_SIZE*2 - 1);
				population[i] = parentOffspringTotalPopulation[rand];
			}
			
			
			Utility.sort(population);
			
		}


		TotalCostCalculator.calculateCostofPopulation(population,0,POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor);
		Utility.sort(population);
		Solver.gatherExcelData(population, POPULATION_SIZE, generation);
		
		

		if(Solver.outputToFile)
		{
			out.print("\n\n\n\n\n--------------------------------------------------\n");
		//	calculateCostWithPenalty(0, POPULATION_SIZE, generation, true);
			out.print("\n\n\nFINAL POPULATION\n\n");
			for( i=0;i<POPULATION_SIZE;i++)
			{
				out.println("\n\nIndividual : "+i);
				population[i].print();
			}
		}
		
		return population[0];

	}
	
		
	public int getNumberOfGeeration()
	{
		return NUMBER_OF_GENERATION;
	}
}
