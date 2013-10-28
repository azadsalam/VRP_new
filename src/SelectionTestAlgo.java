import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Scanner;


public class SelectionTestAlgo  implements GeneticAlgorithm
{
	PrintWriter out; 
	
	int POPULATION_SIZE = 10;
	int NUMBER_OF_OFFSPRING = 10;
	int NUMBER_OF_GENERATION = 1;
	
	ProblemInstance problemInstance;
	Individual population[];

	//for storing new generated offsprings
	Individual offspringPopulation[];

	//for temporary storing
	Individual temporaryPopulation[];

	// for selection - roulette wheel
	double fitness[];
	double cdf[];

	double loadPenaltyFactor;
	double routeTimePenaltyFactor;
	
	
	public SelectionTestAlgo(ProblemInstance problemInstance) 
	{
		// TODO Auto-generated constructor stub
		this.problemInstance = problemInstance;
		out = problemInstance.out;
		
		population = new Individual[POPULATION_SIZE];
		offspringPopulation = new Individual[NUMBER_OF_OFFSPRING];
		temporaryPopulation = new Individual[NUMBER_OF_GENERATION];
		
		fitness = new double[POPULATION_SIZE];
		cdf = new double[POPULATION_SIZE];
		
		loadPenaltyFactor = 10;
		routeTimePenaltyFactor = 0.6;
		
	}

	public Individual run() 
	{
		
		int selectedParent1,selectedParent2;
		int i;
		
		Individual parent1,parent2,offspring;

		// INITIALISE POPULATION
		initialisePopulation();
		TotalCostCalculator.calculateCostofPopulation(population,0,POPULATION_SIZE, loadPenaltyFactor, routeTimePenaltyFactor);
		

		Utility.sort(population);
		for( i=0;i<POPULATION_SIZE;i++) System.out.print(population[i].costWithPenalty + " ");
		System.out.print("\n");

		
		SelectionOperator so = new RoutletteWheelSelection();
		for(int generation=0;generation<1;generation++)
		{
			//sort function uses selection sort, replace with some O(n lg n) sort algthm

			double[] cst = new double[NUMBER_OF_OFFSPRING];
			
			//initialiseRouletteWheelSelection(generation);
			so.initialise(population,true);
			

			//Select a parent and apply genetic operator
			for( i=0;i<NUMBER_OF_OFFSPRING;i++)
			{
					cst[i]= so.getIndividual(population).costWithPenalty;
			}
			
			Arrays.sort(cst);

			for( i=0;i<NUMBER_OF_OFFSPRING;i++) System.out.print(cst[i] + " ");
			System.out.print("\n");

			
			so.initialise(population,false);
			

			//Select a parent and apply genetic operator
			for( i=0;i<NUMBER_OF_OFFSPRING;i++)
			{
					cst[i]= so.getIndividual(population).costWithPenalty;
			}
			
			Arrays.sort(cst);

			for( i=0;i<NUMBER_OF_OFFSPRING;i++) System.out.print(cst[i] + " ");
			System.out.print("\n");

			
		}

		return population[0];

	}
	
	
	
	void initialiseRouletteWheelSelection(int generation)
	{
		int i,j;
		//SELECTION -> Roulette wheel
		double sumOfFitness = 0,sumOfCost=0;
		double sumOfProability = 0;

		//cout<< "SELECTION\nCost : ";
		for( i=0;i<POPULATION_SIZE;i++)
		{
			population[i].calculateCostAndPenalty();
			fitness[i] = population[i].cost;
			// incorporate penalty
	
			double penalty = loadPenaltyFactor  * population[i].totalLoadViolation;
			if(penalty>0)fitness[i] += penalty;
			
			penalty = routeTimePenaltyFactor * (generation+1) * population[i].totalRouteTimeViolation;
			if(penalty>0)fitness[i] += penalty;
		
			
			//store the cost with penalty in the individual
			population[i].costWithPenalty = fitness[i];
			sumOfCost += fitness[i];
			
		}

		for( i=0;i<POPULATION_SIZE;i++)
		{
			fitness[i] = sumOfCost / fitness[i]; // the original fitness			
			sumOfFitness += fitness[i];
		}
		
		
		for( i=0;i<POPULATION_SIZE;i++)
		{
			sumOfProability = cdf[i] = sumOfProability + ((double)fitness[i]/sumOfFitness);
		}


	}
	// it also calculates cost of every individual
	int rouletteWheelSelection()
	{
		double num = Utility.randomIntInclusive(100); // generate random number from [0,100]
		double indicator = num/100;

		//find the smallest index i, with cdf[i] greater than indicator
		int par =  findParent(indicator);
		return par;

	}

	//binary search for smallest index i, having cdf[i] greater than indicator
	int findParent(double indicator)
	{
		//for now linear search, do binary search later
		for(int i=0;i<POPULATION_SIZE;i++)
			if(cdf[i]>=indicator)
				return i;
		return POPULATION_SIZE-1;
	}

	// for now not applying periodAssignment Mutation operator
	// for now working with only MDVRP ->  period = 1
	void applyMutation(Individual offspring)
	{
		int selectedMutationOperator = selectMutationOperator();
		
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


	//0 -> route partition
	//1 ->	permutation
	//2 -> route partition + permutation
	int selectMutationOperator()
	{
		return Utility.randomIntInclusive(2);
	}

	void initialisePopulation()
	{
		//out.print("Initial population : \n");
		for(int i=0; i<POPULATION_SIZE; i++)
		{
			population[i] = new Individual(problemInstance);
			population[i].initialise();
			//out.println("Printing individual "+ i +" : \n");
			//population[i].print();
		}
	}

	@Override
	public int getNumberOfGeeration() {
		// TODO Auto-generated method stub
		return NUMBER_OF_GENERATION;
	}

}
