private void crowdingDistanceAssignment(List<IIndividual> front)
	{
		List<IIndividual> sortedFront;
		double auxDouble, fMax, fMin;
		int frontSize = front.size();
		int numberObjectives = ((CompositeFitness) (front.get(0)).getFitness()).getComponents().length;

		//The parameter that represents the perimeter of the
		//bucket is initialized to 0.
		for(int i=0; i<frontSize; i++)
			individualProperty.get(front.get(i)).setCuboPerimeter(0);

		
		//For each objetive 'm' 
		for(int objective=0; objective<numberObjectives; objective++)
		{
			// Sort the individuals of this front by the objective
			sortedFront = sortFronts(front, objective);

			// The last and first individual have a infinite value
			individualProperty.get(sortedFront.get(0)).setCuboPerimeter(Double.MAX_VALUE);	
			individualProperty.get(sortedFront.get(frontSize-1)).setCuboPerimeter(Double.MAX_VALUE);

			// Maximum and minimum of objective function
			fMin = ((IValueFitness) ((CompositeFitness) (sortedFront.get(0)).getFitness()).getComponent(objective)).getValue();
			fMax = ((IValueFitness) ((CompositeFitness) (sortedFront.get(frontSize - 1)).getFitness()).getComponent(objective)).getValue();

			if(fMin!=fMax){
				for(int i=1; i<frontSize-1; i++)
				{
					auxDouble = Math.abs( ((IValueFitness) ((CompositeFitness) (sortedFront.get(i+1)).getFitness()).getComponent(objective)).getValue() - ((IValueFitness) ((CompositeFitness) (sortedFront.get(i-1)).getFitness()).getComponent(objective)).getValue());
					auxDouble = auxDouble / Math.abs(fMax - fMin);
					individualProperty.get(sortedFront.get(i)).setCuboPerimeter(individualProperty.get(front.get(i)).getCubePerimeter() + auxDouble);
				}
			}
		}
	}