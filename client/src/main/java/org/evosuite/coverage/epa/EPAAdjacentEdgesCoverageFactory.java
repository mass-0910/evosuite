package org.evosuite.coverage.epa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.evosuite.coverage.TestFitnessFactory;
import org.evosuite.testcase.TestChromosome;
import org.evosuite.testcase.execution.ExecutionResult;
import org.evosuite.testcase.execution.ExecutionTracer;
import org.evosuite.testsuite.TestSuiteChromosome;

public class EPAAdjacentEdgesCoverageFactory implements TestFitnessFactory<EPAAdjacentEdgesCoverageTestFitness> {

	// private final EPA epa;

	public EPAAdjacentEdgesCoverageFactory(EPA epaAutomata) {
		// this.epa = epaAutomata;
		// this.goals = buildCoverageGoals();
	}

	private static Map<String, EPAAdjacentEdgesCoverageTestFitness> goals = new LinkedHashMap<>();

	public static Map<String, EPAAdjacentEdgesCoverageTestFitness> getGoals() {
		return goals;
	}

	// private List<EPAAdjacentEdgesCoverageTestFitness> buildCoverageGoals() {
	// List<EPAAdjacentEdgesCoverageTestFitness> goals = new
	// LinkedList<EPAAdjacentEdgesCoverageTestFitness>();
	// Set<EPAState> states = new HashSet<EPAState>(this.epa.getStates());
	//
	// /*
	// * fromState -> firstActionId -> middleState -> secondActionId -> toState
	// */
	// for (EPAState fromState : states)
	// for (String firstActionId : this.epa.getActions())
	// for (EPAState middleState : states)
	// for (String secondActionId : this.epa.getActions())
	// for (EPAState toState : states) {
	// EPATransition firstTransition = new EPANormalTransition(fromState,
	// firstActionId, middleState);
	// EPATransition secondNormalTransition = new EPANormalTransition(middleState,
	// secondActionId, toState);
	// EPAAdjacentEdgesCoverageGoal goal = new
	// EPAAdjacentEdgesCoverageGoal(Properties.TARGET_CLASS, firstTransition,
	// secondNormalTransition);
	// EPAAdjacentEdgesCoverageTestFitness testFitness = new
	// EPAAdjacentEdgesCoverageTestFitness(goal);
	// goals.add(testFitness);
	// EPATransition secondExceptionalTransition = new
	// EPAExceptionalTransition(middleState, secondActionId, toState, "");
	// goal = new EPAAdjacentEdgesCoverageGoal(Properties.TARGET_CLASS,
	// firstTransition, secondExceptionalTransition);
	// testFitness = new EPAAdjacentEdgesCoverageTestFitness(goal);
	// goals.add(testFitness);
	// }
	// return goals;
	// }

	@Override
	public List<EPAAdjacentEdgesCoverageTestFitness> getCoverageGoals() {
		return new ArrayList<EPAAdjacentEdgesCoverageTestFitness>(goals.values());
	}

	@Override
	public double getFitness(TestSuiteChromosome suite) {

		ExecutionTracer.enableTraceCalls();

		int coveredGoals = 0;
		List<ExecutionResult> executionResults = new ArrayList<>(suite.getTestChromosomes().size());
		for (TestChromosome test : suite.getTestChromosomes()) {
			executionResults.add(test.getLastExecutionResult());
		}
		coveredGoals = EPAAdjacentEdgesPair.getAdjacentEdgesPairsExecuted(executionResults).size();
		ExecutionTracer.disableTraceCalls();

		return getCoverageGoals().size() - coveredGoals;

	}

}
