/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package testful.evolutionary;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import jmetal.base.Algorithm;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.TerminationCriterion;
import jmetal.base.Variable;
import jmetal.base.operator.crossover.Crossover;
import jmetal.base.operator.localSearch.LocalSearch;
import jmetal.base.operator.localSearch.LocalSearchPopulation;
import jmetal.base.operator.mutation.Mutation;
import jmetal.base.operator.selection.Selection;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.Ranking;
import testful.IUpdate;
import testful.evolutionary.IConfigEvolutionary.FitnessInheritance;
import testful.utils.StopWatch;

/**
 * This class implements the NSGA-II algorithm. Adapted from JMetal.
 */
public class NSGAII<V extends Variable>
extends Algorithm<V, Crossover<V>, Mutation<V>, Selection<V, Solution<V>>, LocalSearch<V>>
implements IUpdate {

	private static final Logger logger = Logger.getLogger("testful.evolutionary");

	private static final long serialVersionUID = 4970928169851043408L;

	private List<Callback> callbacks = new LinkedList<Callback>();

	@Override
	public void register(Callback c) {
		this.callbacks.add(c);
	}

	@Override
	public void unregister(Callback c) {
		this.callbacks.remove(c);
	}

	private void update(TerminationCriterion criterion) {
		for(Callback c : callbacks)
			c.update(criterion);
	}

	/** stores the problem  to solve */
	private Problem<V> problem_;

	/** probability to inherit the fitness */
	private final float INHERIT_PROBABILITY = 0.55f;

	/** is fitness inheritance enabled */
	private FitnessInheritance inherit = FitnessInheritance.DISABLED;

	/** period of the local search (in generations) */
	private int localSearchPeriod = 20;

	/** number of elements on which the local search is applied */
	private int localSearchNum = 0;

	/**
	 * Constructor
	 * @param problem Problem to solve
	 */
	public NSGAII(Problem<V> problem) {
		this.problem_ = problem;
	} // NSGAII

	public void setInherit(FitnessInheritance inherit) {
		this.inherit = inherit;
	}

	public FitnessInheritance getInherit() {
		return inherit;
	}

	public void setLocalSearchPeriod(int localSearchPeriod) {
		this.localSearchPeriod = localSearchPeriod;
	}

	public int getLocalSearchPeriod() {
		return localSearchPeriod;
	}

	public void setLocalSearchNum(int localSearchNum) {
		this.localSearchNum = localSearchNum;
	}

	public void setLocalSearchNum(float perc) {
		if(perc < 0) perc = 0;
		if(perc > 1) perc = 1;

		this.localSearchNum = (int) (getPopulationSize()*perc);
	}

	public int getLocalSearchNum() {
		return localSearchNum;
	}

	/**
	 * Runs the NSGA-II algorithm.
	 * @return a <code>SolutionSet</code> that is a set of non dominated solutions
	 * as a result of the algorithm execution
	 * @throws JMException if something goes wrong
	 */
	@Override
	public SolutionSet<V> execute() throws JMException {
		SolutionSet<V> population;
		SolutionSet<V> union;

		StopWatch timer = StopWatch.getTimer();
		timer.start("nsga.initialization");

		//Read the parameters
		final int populationSize = getPopulationSize();

		//Initialize the variables
		population = new SolutionSet<V>(populationSize);
		int evaluations = 0;

		int currentGeneration = 0;
		problem_.setCurrentGeneration(currentGeneration, 0);

		// Create the initial solutionSet
		logger.info(String.format("(%5.2f%%) Creating initial population - %s to go", getTerminationCriterion().getProgressPercent(), getTerminationCriterion().getRemaining()));
		for (int i = 0; i < populationSize; i++)
			population.add(new Solution<V>(problem_));

		timer.stop();

		// Evaluating initial population
		timer.start("nsga.execution");
		logger.info(String.format("(%5.2f%%) Generation 0 (initial population) - %s to go", getTerminationCriterion().getProgressPercent(), getTerminationCriterion().getRemaining()));
		evaluations += problem_.evaluate(population);

		for(Solution<V> solution : population)
			problem_.evaluateConstraints(solution);

		timer.stop();

		// Generations ...
		while (!getTerminationCriterion().isTerminated()) {
			problem_.setCurrentGeneration(++currentGeneration, getTerminationCriterion().getProgress());
			update(getTerminationCriterion());

			logger.info(String.format("(%5.2f%%) Generation %d - %s to go", getTerminationCriterion().getProgressPercent(), currentGeneration, getTerminationCriterion().getRemaining()));

			// perform the improvement
			if(improvement != null && currentGeneration % localSearchPeriod == 0) {

				timer.start("nsga.localSearch");
				if(localSearchNum == 0 && improvement instanceof LocalSearchPopulation<?>) {
					SolutionSet<V> front = new Ranking<V>(population).getSubfront(0);
					logger.info("Local search on fronteer (" + front.size() + ")");
					SolutionSet<V> mutated = ((LocalSearchPopulation<V>)improvement).execute(front);
					if(mutated != null) problem_.evaluate(mutated);
				} else {
					for (int i = 0; i < localSearchNum && !getTerminationCriterion().isTerminated(); i++) {
						final int randInt = PseudoRandom.getMersenneTwisterFast().nextInt(populationSize);
						logger.info("Local search " + i + "/" + localSearchNum + " on element " + randInt);
						Solution<V> solution = population.get(randInt);
						solution = improvement.execute(solution);
						if(solution != null) problem_.evaluate(solution);
					}
				}
				timer.stop();

				continue;
			}

			// Create the offSpring solutionSet
			timer.start("nsga.offSpring_creation");
			SolutionSet<V> offspringPopulation = new SolutionSet<V>(populationSize);
			for (int i = 0; i < (populationSize / 2); i++) {
				//obtain parents
				Solution<V> parent1 = selectionOperator.execute(population);
				Solution<V> parent2 = selectionOperator.execute(population);
				Solution<V>[] offSpring = crossoverOperator.execute(parent1, parent2);
				mutationOperator.execute(offSpring[0]);
				mutationOperator.execute(offSpring[1]);
				offspringPopulation.add(offSpring[0]);
				offspringPopulation.add(offSpring[1]);
				evaluations += 2;
			}
			timer.stop();

			// select individuals to evaluate
			Iterable<Solution<V>> toEval = offspringPopulation;

			switch (inherit) {
			case UNIFORM:
				timer.start("nsga.fitnessInheritance_uniform");
				List<Solution<V>> tmpu = new ArrayList<Solution<V>>();
				for(Solution<V> s : offspringPopulation)
					if(!PseudoRandom.getMersenneTwisterFast().nextBoolean(INHERIT_PROBABILITY))
						tmpu.add(s);

				toEval = tmpu;
				timer.stop();
				break;

			case FRONTEER:
				timer.start("nsga.fitnessInheritance_uniform");
				List<Solution<V>> tmpf = new ArrayList<Solution<V>>();

				final Ranking<V> ranking = new Ranking<V>(population);
				final SolutionSet<V> fronteer = ranking.getSubfront(0);
				final List<Solution<V>> others = new ArrayList<Solution<V>>();

				for(int i = 1; i < ranking.getNumberOfSubfronts(); i++)
					for(Solution<V> s : ranking.getSubfront(i))
						others.add(s);

				final int n = offspringPopulation.size();
				final int f = fronteer.size();

				final float k = 0.5f;
				final float pf = k * INHERIT_PROBABILITY * n / (n + f*(k - 1.0f));
				final float po = (pf / k) >= 1 ? 1 : pf / k;

				for(Solution<V> s : fronteer) {
					if(!PseudoRandom.getMersenneTwisterFast().nextBoolean(pf)) {
						tmpf.add(s);
					}
				}

				for(Solution<V> s : others) {
					if(!PseudoRandom.getMersenneTwisterFast().nextBoolean(po))  {
						tmpf.add(s);
					}
				}

				toEval = tmpf;
				timer.stop();
				break;
			}

			// evaluate individuals
			timer.start("nsga.execution");
			problem_.evaluate(toEval);
			for(Solution<V> solution : toEval) problem_.evaluateConstraints(solution);
			timer.stop();

			// Create the solutionSet union of solutionSet and offSpring
			timer.start("nsga.union");
			union = population.union(offspringPopulation);
			timer.stop();

			// Ranking the union
			timer.start("nsga.ranking");
			Ranking<V> ranking = new Ranking<V>(union);
			timer.stop();

			timer.start("nsga.selection");

			int remain = populationSize;
			int index = 0;
			SolutionSet<V> front = null;
			population.clear();

			// Obtain the next front
			front = ranking.getSubfront(0);

			while ((remain > 0) && (remain >= front.size())) {
				//Assign crowding distance to individuals
				Distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
				//Add the individuals of this front
				for(Solution<V> s : front)
					population.add(s);

				//Decrement remain
				remain = remain - front.size();

				//Obtain the next front
				if (remain > 0)
					front = ranking.getSubfront(++index);
			} // while

			// Remain is less than front(index).size, insert only the best one
			if (remain > 0) {  // front contains individuals to insert
				Distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
				front.sort(new jmetal.base.operator.comparator.CrowdingComparator<V>());
				for (int k = 0; k < remain; k++)
					population.add(front.get(k));

				remain = 0;
			} // if

			timer.stop(); // nsga2-selection

		} // while

		// Return the first non-dominated front
		Ranking<V> ranking = new Ranking<V>(population);
		return ranking.getSubfront(0);
	} // execute
} // NSGA-II
