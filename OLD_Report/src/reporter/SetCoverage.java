package reporter;

import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.point.Point;

public class SetCoverage {

	public static InmutablePair<Double, Double> evaluate(InmutablePair<Front, Front> pairOfSolutionLists) {
		Front front1 = pairOfSolutionLists.getFirst() ;
		Front front2 = pairOfSolutionLists.getSecond() ;

		if (front1 == null) {
			throw new JMetalException("The first front is null") ;
		} else if (front2 == null) {
			throw new JMetalException("The second front is null");
		}

		return new InmutablePair<>(evaluate(front1, front2), evaluate(front2, front1));
	}

	/**
	 * Calculates the set coverage of set1 over set2
	 * @param set1
	 * @param set2
	 * @return The value of the set coverage
	 */
	public static double evaluate(Front set1, Front set2) {
		double result ;
		int sum = 0 ;

		if (set2.getNumberOfPoints()==0) {
			if (set1.getNumberOfPoints()==0) {
				result = 0.0 ;
			} else {
				result = 1.0 ;
			}
		} else {
			for (int i = 0; i < set2.getNumberOfPoints(); i++) {
				Point solution = set2.getPoint(i);
				if (isSolutionDominatedBySolutionList(solution, set1)) {
					sum++;
				}
			}
			result = (double)sum/set2.getNumberOfPoints() ;
		}
		return result ;
	}
	public static boolean isSolutionDominatedBySolutionList(Point solution, Front solutionSet) {
		for(int i = 0; i < solutionSet.getNumberOfPoints(); i++)
		{
			Point point = solutionSet.getPoint(i);
			if(dominance(solution, point) == -1)
				return false;
		}

		return true;
	}
	private static int dominance(final Point pointA, final Point pointB)
	{
		boolean aDominatesB = true;
		for(int i = 0; i < pointA.getNumberOfDimensions(); i++)
			if(pointA.getDimensionValue(i) > pointB.getDimensionValue(i))
				aDominatesB = false;

		boolean bDominatesA = true;
		for(int i = 0; i < pointA.getNumberOfDimensions(); i++)
			if(pointB.getDimensionValue(i) > pointA.getDimensionValue(i))
				bDominatesA = false;

		if(aDominatesB) return -1;
		if(bDominatesA) return 1;
		else return 0;
	}

}