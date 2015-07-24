package org.evosuite.testcase.mutation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.evosuite.Properties;
import org.evosuite.setup.TestCluster;
import org.evosuite.testcase.ConstraintVerifier;
import org.evosuite.testcase.TestCase;
import org.evosuite.testcase.TestFactory;
import org.evosuite.testcase.statements.PrimitiveStatement;
import org.evosuite.testcase.variable.NullReference;
import org.evosuite.testcase.variable.VariableReference;
import org.evosuite.utils.Randomness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomInsertion implements InsertionStrategy {

	private static final Logger logger = LoggerFactory.getLogger(RandomInsertion.class);
	
	@Override
	public int insertStatement(TestCase test, int lastPosition) {
		double r = Randomness.nextDouble();
		int oldSize = test.size();

		/*
			TODO: if allow inserting a UUT method in the middle of a test,
			 we need to handle case of not breaking any initialzing bounded variable
		 */
		int max = lastPosition;
		if (max == test.size())
			max += 1;

		if (max <= 0)
			max = 1;

		int position = 0;

		assert Properties.INSERTION_UUT + Properties.INSERTION_ENVIRONMENT + Properties.INSERTION_PARAMETER == 1.0;

		boolean insertUUT = Properties.INSERTION_UUT > 0 &&
				r <= Properties.INSERTION_UUT && TestCluster.getInstance().getNumTestCalls() > 0 ;

		boolean insertEnv = !insertUUT && Properties.INSERTION_ENVIRONMENT > 0 &&
				r > Properties.INSERTION_UUT && r <= Properties.INSERTION_UUT+Properties.INSERTION_ENVIRONMENT &&
				TestCluster.getInstance().getNumOfEnvironmentCalls() > 0;

		boolean insertParam = !insertUUT && !insertEnv;


		boolean success = false;
		if (insertUUT) {
			// Insert a call to the UUT at the end
			position = test.size();
			success = TestFactory.getInstance().insertRandomCall(test, position);
		} else if (insertEnv) {
			/*
				Insert a call to the environment. As such call is likely to depend on many constraints,
				we do not specify here the position of where it ll happen.
			 */
			position = TestFactory.getInstance().insertRandomCallOnEnvironment(test,lastPosition);
			success = (position >= 0);
		} else if (insertParam){
			// Insert a call to a parameter

			VariableReference var = selectRandomVariableForCall(test, lastPosition);
			if (var != null) {
				int lastUsage = var.getStPosition();

				for (VariableReference usage : test.getReferences(var)) {
					if (usage.getStPosition() > lastUsage)
						lastUsage = usage.getStPosition();
				}

				if (lastUsage != var.getStPosition()) {
					position = Randomness.nextInt(var.getStPosition(), lastUsage);
				} else {
					position = lastUsage;
				}

				logger.debug("Inserting call at position " + position + ", chosen var: "
						+ var.getName() + ", distance: " + var.getDistance() + ", class: "
						+ var.getClassName());

				success = TestFactory.getInstance().insertRandomCallOnObjectAt(test, var, position);
			}

			if (!success && TestCluster.getInstance().getNumTestCalls() > 0) {
				logger.debug("Adding new call on UUT because var was null");
				//Why was it different fron UUT insertion? ie, in random position instead of last
				//position = Randomness.nextInt(max);
				position = test.size();
				success = TestFactory.getInstance().insertRandomCall(test, position);
			}
		}

		//this can happen if insertion had side effect of adding further previous statements in the test,
		//eg to handle input parameters
		if (test.size() - oldSize > 1) {
			position += (test.size() - oldSize - 1);
		}

		if (success) {
			assert ConstraintVerifier.verifyTest(test);;
			assert ! ConstraintVerifier.hasAnyOnlyForAssertionMethod(test);

			return position;
		} else {
			return -1;
		}
	}
	
	private VariableReference selectRandomVariableForCall(TestCase test, int position) {
		if (test.isEmpty() || position == 0)
			return null;

		List<VariableReference> allVariables = test.getObjects(position);
		Set<VariableReference> candidateVariables = new LinkedHashSet<VariableReference>();
		for(VariableReference var : allVariables) {
			if (!(var instanceof NullReference) &&
					!var.isVoid() &&
					!(test.getStatement(var.getStPosition()) instanceof PrimitiveStatement) &&
					!var.isPrimitive() &&
					test.hasReferences(var) &&
					/* Note: this check has been added only recently,
						to avoid having added calls to UUT in the middle of the test
					 */
					!var.getVariableClass().equals(Properties.getTargetClass())) {

				candidateVariables.add(var);
			}
		}
		if(candidateVariables.isEmpty()) {
			return null;
		} else {
			VariableReference choice = Randomness.choice(candidateVariables);
			return choice;
		}
	}

}