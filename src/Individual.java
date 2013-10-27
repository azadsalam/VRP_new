import java.io.PrintWriter;
import java.util.Scanner;


public class Individual 
{
	//representation
	boolean periodAssignment[][];
	int permutation[][];
	int routePartition[][];
	
	
	double cost;
	
	double costWithPenalty;
//	Utility utility;
	boolean isFeasible;
	boolean feasibilitySet;

	double loadViolation[][];
	double totalLoadViolation;

	//double totalRouteTime;
	
	double totalRouteTimeViolation;
	
	ProblemInstance problemInstance;
	
	

	public Individual()
	{
		cost = -1;
		costWithPenalty = -1;
		feasibilitySet = false;
		isFeasible = false;	
	}
	
	
	public void initialise() 
	{
		// TODO Auto-generated method stub

		int i,j;
		
		for( i=0;i<problemInstance.periodCount;i++)
		{
			// initially every permutation is identity permutation
			for( j=0;j<problemInstance.customerCount;j++)
			{
				permutation[i][j] = j;
			}
		}
		
		// NOW INITIALISE WITH VALUES

		//initialize period assignment

		int freq,allocated,random,tmp;

		//Randomly allocate period to clients equal to their frequencies
		for(int client=0; client < problemInstance.customerCount; client++)
		{
			freq = problemInstance.frequencyAllocation[client];
			allocated=0;

			while(allocated!=freq)
			{
				random = Utility.randomIntInclusive(problemInstance.periodCount-1);
				
				if(periodAssignment[random][client]==false)
				{
					periodAssignment[random][client]=true;
					allocated++;
				}
			}
		}
		
		
		//initialize permutation map - KNUTH SHUFFLE
		for(int period=0; period < problemInstance.periodCount;period++)
		{
			//apply knuths shuffle
			for( i = problemInstance.customerCount -1 ;i>0 ;i-- )
			{
				j = Utility.randomIntInclusive(i);
				
				if(i==j)continue;

				tmp = permutation[period][i];
				permutation[period][i] = permutation[period][j];
				permutation[period][j] = tmp;
			}
		}
		
		//NEED TO GENERATE #vehicle-1 (not distinct - distinct) random numbers in increasing order from [0,#customer - 1]
		// DEVICE some faster and smarter algorithm

		// route for vehicle i is  [ routePartition[i-1]+1 , routePartition[i] ]
		// given that routePartition[i-1]+1 <= routePartition[i]

		//bool found;
		for(int period=0;period<problemInstance.periodCount;period++)
		{
			allocated = 0;
			while(allocated != problemInstance.vehicleCount-1)
			{
				random = Utility.randomIntInclusive(problemInstance.customerCount-1);
	
				routePartition[period][allocated]=random;
				sort(period,random,allocated);
				allocated++;
			}
			routePartition[period][problemInstance.vehicleCount-1] = problemInstance.customerCount-1;
		}
		
		calculateCostAndPenalty();

	}
	
	public Individual(ProblemInstance problemInstance)
	{
		this.problemInstance = problemInstance;
		
		// ALLOCATING periodCount * customerCount Matrix for Period Assignment
		periodAssignment = new boolean[problemInstance.periodCount][problemInstance.customerCount];
		
		//ALLOCATING permutation map matrix -> period * customer
		permutation = new int[problemInstance.periodCount][problemInstance.customerCount];
		
		
		//allocating routeAllocation
		routePartition = new int[problemInstance.periodCount][problemInstance.vehicleCount];

		loadViolation = new double[problemInstance.periodCount][problemInstance.vehicleCount];
	}
	
	
	// make a copy cat individual
		//copies problem instance, periodAssignment, permutation, routePartition
	public Individual(Individual original)
	{
	    int i,j;
		problemInstance = original.problemInstance;

		periodAssignment = new boolean[problemInstance.periodCount][problemInstance.customerCount];
		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				periodAssignment[i][j] = original.periodAssignment[i][j];
			}
		}



		permutation = new int[problemInstance.periodCount][problemInstance.customerCount];
		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				permutation[i][j] = original.permutation[i][j];
			}
		}


		routePartition = new int[problemInstance.periodCount][problemInstance.vehicleCount];
		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.vehicleCount;j++)
			{
				routePartition[i][j] = original.routePartition[i][j];
			}
		}
		

		cost = original.cost;
		costWithPenalty = original.costWithPenalty;

		//allocate demanViolationMatrix

        loadViolation = new double[problemInstance.periodCount][problemInstance.vehicleCount];

	}

	/**
	 * Calculates cost and penalty of every individual
	 * For route time violation travelling times are not considered
	 * route time violation = maximum duration of a route - Sum of service time
	 */
	void calculateCostAndPenalty()
	{
		double tempCost = 0;

		totalLoadViolation = 0;
		totalRouteTimeViolation = 0;
        
		//double temlLoad;
		for(int i=0;i<problemInstance.periodCount;i++)
		{
			for(int j=0;j<problemInstance.vehicleCount;j++)
			{
				tempCost += calculateCost(i,j);
                //calculate the total load violation
                //Add only when actually the load is violated i.e. violation is positive
                if(loadViolation[i][j]>0) totalLoadViolation += loadViolation[i][j];
			}
		}
		
		cost = tempCost;

		if(totalLoadViolation>0  || totalRouteTimeViolation > 0)
		{
			isFeasible = false;
		}
		else isFeasible = true;

		feasibilitySet = true;
		
	}

	//calcuate fitness for each period for each vehicle
	// route for vehicle i is  [ routePartition[i-1]+1 , routePartition[i] ]
	// given that routePartition[i-1]+1 <= routePartition[i]
	//ignoring travelling time for now - for cordeau MDVRP
	// only service time is considered


	double calculateCost(int period,int vehicle)
	{
		int assignedDepot;
		assignedDepot = problemInstance.depotAllocation[vehicle];
		double costForPV = 0;
		int start,end; // marks the first and last position of corresponding route for the array permutation

		if(vehicle == 0) start = 0;
		else start = routePartition[period][vehicle-1]+1;

		end = routePartition[period][vehicle];

		if(end<start) return 0;

		int activeStart=-1,activeEnd=-1,previous=-1,clientNode;

        double clientDemand=0;
		double totalRouteTime=0;
		for(int i=start;i<=end;i++)
		{
			clientNode = permutation[period][i];
			if(periodAssignment[period][clientNode]==false) continue;

			if(activeStart == -1) activeStart = clientNode;
			activeEnd = clientNode;

			totalRouteTime += problemInstance.serviceTime[clientNode]; //adding service time for that node

            //Caluculate total client demand for corresponding period,vehicle
            clientDemand += problemInstance.demand[clientNode];

			if(previous == -1)
			{
				previous = clientNode;
				continue;
			}

			costForPV +=	problemInstance.costMatrix[previous+problemInstance.depotCount][clientNode+problemInstance.depotCount];
			
			//ignoring travelling time for now - for cordeau MDVRP
			//totalRouteTime += problemInstance.travellingTimeMatrix[previous+problemInstance.depotCount][clientNode+problemInstance.depotCount];
			
			previous = clientNode;

		}

        if(activeStart!=-1 && activeEnd != -1)
        {
            costForPV += problemInstance.costMatrix[assignedDepot][activeStart+problemInstance.depotCount];
            costForPV += problemInstance.costMatrix[activeEnd+problemInstance.depotCount][assignedDepot];

//			totalRouteTime += problemInstance.travellingTimeMatrix[assignedDepot][activeStart+problemInstance.depotCount];
//            totalRouteTime += problemInstance.travellingTimeMatrix[activeEnd+problemInstance.depotCount][assignedDepot];
        }
        loadViolation[period][vehicle] = clientDemand - problemInstance.loadCapacity[vehicle];

		double routeTimeViolation = totalRouteTime - problemInstance.timeConstraintsOfVehicles[period][vehicle] ;
		if(routeTimeViolation>0) totalRouteTimeViolation += routeTimeViolation;

		return costForPV;

	}
	

	// sorts the array routePartition in increasing order
	// input -> routePartition array [0, upperbound ], with,n inserted at the last in the array
	// output -> sorted array [0, upperbound]
	void sort(int period,int n,int upperbound)
	{
		int tmp;
		for(int v = upperbound-1;v>=0;v--)
		{
			if(routePartition[period][v]>routePartition[period][v+1])
			{
				tmp = routePartition[period][v];
				routePartition[period][v] = routePartition[period][v+1];
				routePartition[period][v+1] = tmp;
			}
			else
				break;
		}
	}
	
	void print()
	{
		//if(problemInstance == null) System.out.println("OUT IS NULL");
		PrintWriter out = this.problemInstance.getPrintWriter();
		int i,j;
		
		out.println("PERIOD ASSIGMENT : ");
		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				if(periodAssignment[i][j])	out.print("1 ");
				else out.print("0 ");
				
			}
			out.println();
		}

		out.print("Permutation : \n");
		for( i=0; i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				out.print(permutation[i][j]+" ");
			}
			out.println();
		}

		out.print("Route partition : \n");
		
		for(i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.vehicleCount;j++)
				out.print(routePartition[i][j] +" ");
			out.println();
		}

        // print load violation

		out.print("LOAD VIOLATION MATRIX : \n");
        for( i=0;i<problemInstance.periodCount;i++)
        {
            for( j=0;j<problemInstance.vehicleCount;j++)
            {
            	out.print(loadViolation[i][j]+" ");
            }
            out.println();
        }

        out.println("Is Feasible : "+isFeasible);
        out.println("Total Load Violation : "+totalLoadViolation);        
        out.println("Total route time violation : "+totalRouteTimeViolation);		
		out.println("Cost : " + cost);
		out.println("Cost with penalty : "+costWithPenalty);
		out.println();
		
	}
	
	void miniPrint()
	{
		PrintWriter out = this.problemInstance.getPrintWriter();
		int i,j;
		
		out.println("PERIOD ASSIGMENT : ");
		for( i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				if(periodAssignment[i][j])	out.print("1 ");
				else out.print("0 ");
				
			}
			out.println();
		}

		out.print("Permutation : \n");
		for( i=0; i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.customerCount;j++)
			{
				out.print(permutation[i][j]+" ");
			}
			out.println();
		}

		out.print("Route partition : \n");
		
		for(i=0;i<problemInstance.periodCount;i++)
		{
			for( j=0;j<problemInstance.vehicleCount;j++)
				out.print(routePartition[i][j] +" ");
			out.println();
		}
		

        // print load violation
        out.println("Is Feasible : "+isFeasible);
        out.println("Total Load Violation : "+totalLoadViolation);        
        out.println("Total route time violation : "+totalRouteTimeViolation);		
		out.println("Cost : " + cost);
		out.println("Cost with penalty : "+costWithPenalty);
		out.println("\n");
		
	}
	
	// swaps even if neither of the customers get visited that day
	void mutatePermutation(int period)
	{
		int first = Utility.randomIntInclusive(problemInstance.customerCount-1);

		int second;
		int count=0;
		do
		{
			second = Utility.randomIntInclusive(problemInstance.customerCount-1);
			count++;
			if(count==problemInstance.customerCount)break;
		}
		while(second == first);

		int temp = permutation[period][first];
		permutation[period][first] = permutation[period][second];
		permutation[period][second] = temp;

		// FITNESS CAN BE UPDATED HERE
	}
	
	void mutatePermutationWithinSingleRoute()
	{
		boolean success = false;
		do
		{
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			int vehicle = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			success = mutatePermutationWithinSingleRoute(period, vehicle);
		}while(success==false);
		
	}
	

	//returns true if permutation successful
	boolean mutatePermutationWithinSingleRoute(int period,int vehicle)
	{
		int start,end;
		
		if(vehicle == 0) start = 0;
		else start = routePartition[period][vehicle-1]+1;

		end = routePartition[period][vehicle];

		if(end<=start) return false;
		
		int first = Utility.randomIntInclusive(start,end);

		int second;
		do
		{
			second = Utility.randomIntInclusive(start,end);
		}
		while(second == first);

		int temp = permutation[period][first];
		permutation[period][first] = permutation[period][second];
		permutation[period][second] = temp;

		
		return true;
		
	}
	void mutatePermutationOfDifferentRoute()
	{
		
		if(problemInstance.vehicleCount<2)return;
		
		boolean success = false;
		do
		{
			int period = Utility.randomIntInclusive(problemInstance.periodCount-1);
			int vehicle1 = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			int vehicle2 = Utility.randomIntInclusive(problemInstance.vehicleCount-1);
			if(vehicle1==vehicle2)continue;
			success = mutatePermutationOfDifferentRoute(period, vehicle1,vehicle2);
		}while(success==false);
		
	}
	

	//returns if permutation successful
	boolean mutatePermutationOfDifferentRoute(int period,int vehicle1,int vehicle2)
	{
		int start1,end1,start2,end2;
		
		if(vehicle1 == 0) start1 = 0;
		else start1 = routePartition[period][vehicle1-1]+1;

		end1 = routePartition[period][vehicle1];

		if(end1<start1) return false;
		
		
		if(vehicle2 == 0) start2 = 0;
		else start2 = routePartition[period][vehicle2-1]+1;

		end2 = routePartition[period][vehicle2];

		if(end2<start2) return false;
		
		int first = Utility.randomIntInclusive(start1,end1);
		int second = Utility.randomIntInclusive(start2,end2);
		

		
		int temp = permutation[period][first];
		permutation[period][first] = permutation[period][second];
		permutation[period][second] = temp;

		
		return true;
		
	}

	//moves some red line
	//no effect if only one vehicle
	void mutateRoutePartition(int period)
	{
		//nothing to do if only one vehicle
		if(problemInstance.vehicleCount == 1) return ;

		//pick a red line/seperator
		//generate random number in [0,vehicleCount-1)


		int distance,increment;

		while(true)
		{
			int seperatorIndex = Utility.randomIntInclusive(problemInstance.vehicleCount-2);
			int dir = Utility.randomIntInclusive(1); // 0-> left , 1-> right
			if(dir==0)//move the seperator left
			{
				if(seperatorIndex==0) distance = routePartition[period][0] ;
				else distance = routePartition[period][seperatorIndex] - routePartition[period][seperatorIndex-1];
				// if the line can not merge with the previous one ,
				// difference = routePartition[seperatorIndex] - 1 - routePartition[seperatorIndex-1]

				// increment should be in [1,distance]
				if(distance==0)continue;
				increment = 1 + Utility.randomIntInclusive(distance-1);
				routePartition[period][seperatorIndex] -= increment;
				return;
			}
			else	//move the seperator right
			{
				distance = routePartition[period][seperatorIndex+1] - routePartition[period][seperatorIndex] ;
				if(distance==0)continue;
				increment = 1 + Utility.randomIntInclusive(distance-1);
				routePartition[period][seperatorIndex] += increment;
				return;
			}
		}

	}


	//returns 0 if it couldnt mutate as period == freq
	int mutatePeriodAssignment(int clientNo)
	{
		//no way to mutate per. ass. as freq. == period
		if(problemInstance.frequencyAllocation[clientNo] == problemInstance.periodCount) return 0;
		if(problemInstance.frequencyAllocation[clientNo] == 0) return 0;		

		int previouslyAssigned; // one period that was assigned to client
		do
		{
			previouslyAssigned = Utility.randomIntInclusive(problemInstance.periodCount-1);
		} while (periodAssignment[previouslyAssigned][clientNo]==false);

		int previouslyUnassigned;//one period that was NOT assigned to client
		do
		{
			previouslyUnassigned = Utility.randomIntInclusive(problemInstance.periodCount-1);
		} while (periodAssignment[previouslyUnassigned][clientNo]==true);

		periodAssignment[previouslyAssigned][clientNo] = false;
		periodAssignment[previouslyUnassigned][clientNo]= true;

		return 1;
	}
	
	/** 
	 * 
	  */
	static void crossOver(ProblemInstance problemInstance,Individual parent1,Individual parent2,Individual child1,Individual child2)
	{
		//with 50% probability swap parents
		int ran = Utility.randomIntInclusive(1);
		if(ran ==1)
		{
			Individual temp = parent1;
			parent1 = parent2;
			parent2 = temp;
		}
		
		
		//child 1 gets first n customers assignment from parent 1 and rest from parent 2
		//child 2 gets first n customers assignment from parent 2 and rest from parent 1
		int n = Utility.randomIntInclusive(problemInstance.customerCount);
		
		
		copyPeriodAssignmentFromParents(child1, parent1, parent2, n ,problemInstance);
		copyPeriodAssignmentFromParents(child2, parent2, parent1, n ,problemInstance);
		
		//child 1 gets permutation of first n period from parent 1
		n = Utility.randomIntInclusive(problemInstance.periodCount);
		
		copyPermutation(child1, parent1, parent2, n, problemInstance);
		copyPermutation(child2, parent2, parent1, n, problemInstance);
		
		
		// crossover route partition
		
		for(int period=0;period<problemInstance.periodCount;period++)
		{
			int temp[] = new int[problemInstance.vehicleCount*2];
			int i;
			for(i=0;i<problemInstance.vehicleCount;i++) temp[i] = parent1.routePartition[period][i];
			for(i=0;i<problemInstance.vehicleCount;i++) temp[i+problemInstance.vehicleCount] = parent2.routePartition[period][i];
			
			
			for(i=0;i<problemInstance.vehicleCount*2;i++)
			{
				for(int j=i+1;j<problemInstance.vehicleCount*2;j++)
				{
					if(temp[i]>temp[j])
					{
						int tmp = temp[i];
						temp[i] = temp[j];
						temp[j]=tmp;
					}
				}
			}
			
			for( i=0;i<problemInstance.vehicleCount;i++) child1.routePartition[period][i] = temp[2*i];
			for( i=0;i<problemInstance.vehicleCount;i++) child2.routePartition[period][i] = temp[2*i+1];
			
		}
		//System.out.println(" "+n);
	}

	//copy first n row from parent 1's permutation
	private static void copyPermutation(Individual child, Individual parent1, Individual parent2,int n,ProblemInstance problemInstance) 
	{
		int i;
		
		for(i=0;i<problemInstance.customerCount;i++)
		{
			for(int period = 0;period<n;period++)
			{
				child.permutation[period][i] = parent1.permutation[period][i];
			}
		}
		
		for(i=0;i<problemInstance.customerCount;i++)
		{
			for(int period = n;period<problemInstance.periodCount;period++)
			{
				child.permutation[period][i] = parent2.permutation[period][i];
			}
		}
		
	}
	
	//copies first n columns from parent1 and rest of them from parent 2 
	private static  void copyPeriodAssignmentFromParents(Individual child, Individual parent1, Individual parent2,int n,ProblemInstance problemInstance)
	{
		int i;
		for(int period = 0; period<problemInstance.periodCount; period++)
		{
			for(i=0;i<n;i++)
			{
				//if(parent1==null)System.out.print("nul");
				child.periodAssignment[period][i] = parent1.periodAssignment[period][i];
			}
		}
		for(int period = 0; period<problemInstance.periodCount; period++)
		{
			for(i=n;i<problemInstance.customerCount;i++)
			{
				child.periodAssignment[period][i] = parent2.periodAssignment[period][i];
			}
		}
	}

	public static double distance(ProblemInstance problemInstance, Individual first,Individual second)
	{
		boolean print=true;

		if(print)
		{
			problemInstance.out.println("In distance function : ");
		}
		
		double distance=0;
		int distanceX=0;
		int distanceY=0;
		int distanceZ=0;
		
		double X,Y,Z;
		double tmp;
		for(int i=0;i<problemInstance.periodCount;i++)
		{
			for(int j=0;j<problemInstance.customerCount;j++)
			{
				//distance for periodAssigment
				if(first.periodAssignment[i][j] != second.periodAssignment[i][j])
					distanceX++;
			}
		}
		
		
		tmp = (double)problemInstance.periodCount*problemInstance.customerCount;
		X = distanceX / tmp;
		
		
		distanceY=0;
		for(int i=0;i<problemInstance.periodCount;i++)
		{
			for(int j=0;j<problemInstance.customerCount;j++)
			{
				//distance for permutation - A distance (Campos)
				//distanceY += Math.abs(first.permutation[i][j] - second.permutation[i][j]);
				
				//hamming distance
				if(first.permutation[i][j] != second.permutation[i][j])
					distanceY++;
			}
		}
		
		tmp = problemInstance.periodCount*problemInstance.customerCount;
		Y = distanceY/tmp;
		
			
		distanceZ=0;
		for(int i=0;i<problemInstance.periodCount;i++)
		{
			for(int j=0;j<problemInstance.vehicleCount;j++)
			{
				//distance for route partition - A distance
				distanceZ += Math.abs(first.routePartition[i][j] - second.routePartition[i][j]);
			}
		}
		
		//as the last element is always same
		tmp = problemInstance.periodCount * problemInstance.customerCount * (problemInstance.vehicleCount-1);
		if(tmp ==0)Z=0;
		else Z = (double)distanceZ/tmp; 
	
		distance = (X+Y+Z)/3;
		
		if(print)
		{
			problemInstance.out.println("distanceX : "+distanceX+" distanceY : "+distanceY+" distanceZ : " +distanceZ);
			problemInstance.out.println("maxX : "+(problemInstance.periodCount*problemInstance.customerCount)
								+" maxY : "+ (problemInstance.periodCount*problemInstance.customerCount)
								+" maxZ : " +( problemInstance.periodCount * problemInstance.customerCount * (problemInstance.vehicleCount-1)) + "\n");
			
			
		}
		
		return distance;
	}
	
}
