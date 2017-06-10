package org.ggp.base.player.gamer.statemachine;

import java.util.ArrayList;
import java.util.List;

import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.cache.CachedStateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.ProverStateMachine;

public class MyReadyPlayerOne extends StateMachineGamer {


	public MyReadyPlayerOne() {
		// TODO Auto-generated constructor stub



	}

	@Override
	public StateMachine getInitialStateMachine() {
		// TODO Auto-generated method stub
		return new CachedStateMachine(new ProverStateMachine());
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {

	}

	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO Auto-generated method stub

		// Get current state
		StateMachine stateMachine = getStateMachine();
		MachineState currentState = getCurrentState();
	    Role role = getRole();
		List<Move> allMoves = stateMachine.getLegalMoves(currentState, role);



		// LEGAL
		//return allMoves.get(0);

		// RANDOM
		//Random rand = new Random();
		//return allMoves.get(rand.nextInt(allMoves.size()));

		// COMPULSIVE DELIBERATION
		return bestCompulsiveMove(role, currentState, stateMachine);

		// MINIMAX


		// ALPHA-BETA

	}

	public Move bestCompulsiveMove (Role role, MachineState state, StateMachine stateMachine) {

		int roleIndex = stateMachine.getRoles().indexOf(role);
		Move action = null;

		try {
			List<List<Move>> allGroupMoves = stateMachine.getLegalJointMoves(state); //getAllGroupMoves(state, stateMachine);
			int score = 0;

			for (int i=0; i<allGroupMoves.size(); i++) {
				try {
					int result = maxScore(role, stateMachine.getNextState(state, allGroupMoves.get(i)), stateMachine);

					System.out.println("Score:" + result);

				    if (result == 100) {
				    	return allGroupMoves.get(i).get(roleIndex);
				    }
				    if (result > score) {
				    	score = result;
			    	 	action = allGroupMoves.get(i).get(roleIndex);
				    }
				} catch(TransitionDefinitionException e) {
					System.out.println(e);
				}
		  }
		} catch (MoveDefinitionException e) {
			System.out.println(e);
		}

		System.out.println("action:" + action);
		if (action == null) {
			  try {
				  return stateMachine.getLegalMoves(state, role).get(0);
			  } catch (MoveDefinitionException e) {
				  System.out.println(e);
			  }
		 }
		return action;
	}

	public int maxScore(Role role, MachineState state, StateMachine stateMachine) {
		if (stateMachine.isTerminal(state)) {
			try {
				return stateMachine.getGoal(state, role);
			} catch (GoalDefinitionException e) {
				System.out.println(e);
			}
		}
		int score = 0;
		try {
			List<List<Move>> allGroupMoves = stateMachine.getLegalJointMoves(state);
			for (int i = 0; i < allGroupMoves.size(); i++) {
				try {
					List<Move> currentGroupMoves = allGroupMoves.get(i);
					int result = maxScore(role, stateMachine.getNextState(state, currentGroupMoves), stateMachine);
					if (result > score) {
						score = result;
					}
				} catch (TransitionDefinitionException e) {
					System.out.println(e);
				}
			}
		} catch (MoveDefinitionException e) {
			System.out.println(e);
		}
		return score;
	}

	public List<List<Move>> getAllGroupMoves(MachineState state, StateMachine stateMachine) {
		List<Role> allRoles = stateMachine.getRoles();
		List<List<Move>> allPlayerActions = new ArrayList<List<Move>>();
		for (int i = 0; i < allRoles.size(); i++) {
			try {
				List<Move> actions = stateMachine.getLegalMoves(state, allRoles.get(i));
				allPlayerActions.add(actions);
			} catch (MoveDefinitionException e) {
				System.out.println(e);
			}
		}
		List<List<Move>> allGroupMoves = new ArrayList<List<Move>>();
		generatePermutations(allPlayerActions, allGroupMoves, 0, new ArrayList<Move>());
		return allGroupMoves;
	}

	public void generatePermutations(List<List<Move>> lists, List<List<Move>> allGroupMoves, int depth, List<Move> current) {
	    if(depth == lists.size()){
	    allGroupMoves.add(current);
	       return;
	     }

	    for(int i = 0; i < lists.get(depth).size(); ++i) {
	    	List<Move> newList = current;
	    	newList.add(lists.get(depth).get(i));
	        generatePermutations(lists, allGroupMoves, depth + 1, newList);
	    }
	}

	@Override
	public void stateMachineStop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stateMachineAbort() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preview(Game g, long timeout) throws GamePreviewException {
		// TODO Auto-generated method stub

	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "Ready Player One";
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
