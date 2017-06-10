package org.ggp.base.player.gamer.statemachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ggp.base.player.gamer.exception.GamePreviewException;
import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.propnet.MyDifferentialPropNetStateMachine;

public class MyReadyPlayerOne extends StateMachineGamer {


	private Map< Role, Map< GdlSentence, Integer>> roleToPropositionCounts;
	private Map< Role, Map< GdlSentence, Integer>> roleToPropositionScores;
	private int totalTerminalStatesDiscovered;
	private boolean gameHasGoals;
	private int totalDepthChargesThisTurn = 0;
	public MyReadyPlayerOne() {
		// TODO Auto-generated constructor stub

	}

	@Override
	public StateMachine getInitialStateMachine() {
		// TODO Auto-generated method stub
//		return new CachedStateMachine(new ProverStateMachine());
//		return new MyPropNetStateMachine();
		return new MyDifferentialPropNetStateMachine();
	}

	@Override
	public void stateMachineMetaGame(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
//		System.out.println("VERIFYING STATE MACHINE");
//		StateMachine old = new CachedStateMachine(new ProverStateMachine());
//		StateMachine newMachine = new MyDifferentialPropNetStateMachine();
//		old.initialize(getMatch().getGame().getRules());
//		newMachine.initialize(getMatch().getGame().getRules());
//		StateMachineVerifier.checkMachineConsistency(old, newMachine, 20000);

				//		((MyPropNetStateMachine) getStateMachine()).factor();
				//		System.out.println("Meta game is called");
				//		roleToPropositionScores = new HashMap< Role, Map<GdlSentence, Integer>>();
				//		roleToPropositionCounts= new HashMap< Role, Map<GdlSentence, Integer>>();
				//		totalTerminalStatesDiscovered = 0;
				//		gameHasGoals = true;
				////
		StateMachine stateMachine = getStateMachine();
		MachineState currentState = getCurrentState();
		MachineState next = currentState;

		//finding out if the game has non terminal reward states
		int moves = 0;
		while(moves < 200 & stateMachine.isTerminal(next) == false){
			try {
				List<Integer> allRewards = stateMachine.getGoals(next);
				for( Integer reward: allRewards){
					if(reward > 0){
						gameHasGoals = true;
					}
				}
				next = stateMachine.getRandomNextState(next);
			}
			catch( GoalDefinitionException e){
				System.out.println(e);
				break;
			}
		}
//			//
//			//		for(Role r: stateMachine.getRoles()){
//			//			roleToPropositionScores.put(r, new HashMap<GdlSentence, Integer>());
//			//			roleToPropositionCounts.put(r, new HashMap<GdlSentence, Integer>());
//			//		}
//
//
//		int theDepth[] = new int[1];
//		theDepth[0] = 0;
			// Get a bunch of terminal states, but you aren't likely to win in each of them...
			// So collect terminal states for each state add it's value to score
			// 100 = 1.0 similar * 100 score...
			// how similar is intersection over terminal state... score is your own score... then average all
		//	while(System.currentTimeMillis() < timeout - 3000){
		//			MachineState terminalState = stateMachine.performDepthCharge(currentState, theDepth);
		//
		//			totalTerminalStatesDiscovered += 1;
		//			//for a given terminal state, go through each role and update the scores...
		//			for(Role r: stateMachine.getRoles()){
		//				int roleIndex = stateMachine.getRoleIndices().get(r);
		//				int roleReward = stateMachine.getGoals(terminalState).get(roleIndex);
		//				Map< GdlSentence, Integer> currentPropositionScores = roleToPropositionScores.get(r);
		//				Map< GdlSentence, Integer> currentPropositionCounts = roleToPropositionCounts.get(r);
		//				for (GdlSentence prop : terminalState.getContents()){
		//					if (currentPropositionScores.containsKey(prop) ){
		//						currentPropositionScores.put(prop, currentPropositionScores.get(prop) + roleReward);
		//						currentPropositionCounts.put(prop, currentPropositionScores.get(prop) + 1);
		//					}
		//					else{
		//						currentPropositionScores.put(prop, roleReward);
		//						currentPropositionCounts.put(prop, 1);
		//					}
		//				}
		//			}
		//	}
	System.out.println("Does this game have goals? " + gameHasGoals);
	}

	@Override
	public Move stateMachineSelectMove(long timeout)
			throws TransitionDefinitionException, MoveDefinitionException, GoalDefinitionException {
		// TODO Auto-generated method stub
		System.out.println("We were asked for a move");
		// Get current state
		long bufferTime = 3000;
		long finishBy = timeout - bufferTime;

		StateMachine stateMachine = getStateMachine();
		MachineState currentState = getCurrentState();
	    Role role = getRole();
	    System.out.println("state machine class: " + stateMachine.getClass());
		List<Move> allMoves = stateMachine.getLegalMoves(currentState, role);
		System.out.println("num moves: " + allMoves.size());
		if( allMoves.size() == 1){
			System.out.println("One move");
			return allMoves.get(0);
		}
		// LEGAL
		System.out.println("All legal moves are" + allMoves);
//		return allMoves.get(0);
		// RANDOM
//		Random rand = new Random();
//		return allMoves.get(rand.nextInt(allMoves.size()));

		// COMPULSIVE DELIBERATION
//		return bestCompulsiveMove(role, currentState, stateMachine);

		 //MINIMAX
		//return bestMinimaxMove(role, currentState, stateMachine);


//		 ALPHA BETA ITERATIVE DEEPENING
//		 Do iterative deepening on alpha beta move... keeping track of best action after each search
//		 Time out around 6 seconds...?
//		 ALPHA-BETA
//		while we have more than 4 seconds left...
//		int maxDepth = 1;
//		Move action = allMoves.get(0);
//		int bestScore = 0;
//		totalDepthChargesThisTurn = 0;
//		//Keep track of time of number of depth charges per second...
////		System.out.println(stateMachine.getLegalJointMoves(currentState) + "Are the legal moves.");
//		while(System.currentTimeMillis() < finishBy){
//			CombinedMove moveAndScore = bestAlphaBetaMove(role, currentState, stateMachine, maxDepth, finishBy);
//			if(moveAndScore.score >= bestScore){
//				bestScore = moveAndScore.score;
//				action = moveAndScore.move;
////				if(maxDepth % 3 == 0){
////					System.out.println("current best action chosen is" + action.toString() + "max depth" + maxDepth + "score" + bestScore);
////				}
//			}
//			maxDepth += 1;
//		}
////////
//		System.out.println("total depth charges" + totalDepthChargesThisTurn + "timeout is" + timeout);
//////		System.out.println("Number Depth Charges per second: " + (1000.0*totalDepthChargesThisTurn)/timeout);
//		System.out.println("Final action chosen is" + action.toString() + "max depth" + maxDepth + "score" + bestScore);
//		return action;

//		 MCTS
		Move action = allMoves.get(0);
		Move MCTS_move = MCTS(stateMachine, currentState, role, finishBy);
		if (MCTS_move != null) {
			System.out.println("returning move "+ MCTS_move );
			return MCTS_move;
		}
		return action;

	}




	public Move MCTS(StateMachine stateMachine, MachineState currentState, Role role, long finishBy) {

		Node root = new Node(currentState, null, null);
		int cycle = 1;
		while(System.currentTimeMillis() < finishBy){
			Node leafNode = selectNode(root, stateMachine);
			//System.out.println("MCTS leaf node: " + leafNode);
			if (leafNode == null) {
				continue;
			}
			expandOnNode(leafNode, stateMachine, role);
			int score = simulateOnNode(leafNode, stateMachine, role);
			leafNode.totalVisits += 1;
			backpropogateOnNode(leafNode, score);
			//System.out.println("cycle finished: " + cycle);
			cycle += 1;
		}
		return pickBestRootMove(root);
	}

	public Move pickBestRootMove(Node node) {

		//System.out.println("Node move utilities: " + node.moveUtility);
		//System.out.println("Node move visits: " + node.moveVisits);

		int totalMoveVisits = 0;
		for (Move move: node.moveVisits.keySet()) {
			totalMoveVisits += node.moveVisits.get(move);
		}
		System.out.println("Total Move visits: " + totalMoveVisits);

		Move bestMove = null;
		int bestScore = 0;

		for (Move move: node.moveUtility.keySet()) {
			int moveScore = 0;
			if (node.moveVisits.get(move) != 0) {
				moveScore = node.moveUtility.get(move) / node.moveVisits.get(move);
			}
			if (moveScore > bestScore) {
				bestMove = move;
				bestScore = moveScore;
			}
		}
		return bestMove;

	}

	public void backpropogateOnNode(Node leafNode, int score) {

		if (leafNode.parentNode != null) {

			int lastScore = leafNode.parentNode.moveUtility.get(leafNode.lastParentMove);
			leafNode.parentNode.moveUtility.put(leafNode.lastParentMove, lastScore + score);

			int lastMoveVisits = leafNode.parentNode.moveVisits.get(leafNode.lastParentMove);
			leafNode.parentNode.moveVisits.put(leafNode.lastParentMove, lastMoveVisits + 1);

			leafNode.parentNode.totalVisits += 1;

			int lastNumVisits = leafNode.parentNode.childNodesVisits.get(leafNode);
			leafNode.parentNode.childNodesVisits.put(leafNode, lastNumVisits + 1);

			//System.out.println("BACKPROP: node visits: " + lastNumVisits);

			int lastUtility = leafNode.parentNode.childNodesUtilities.get(leafNode);
			leafNode.parentNode.childNodesUtilities.put(leafNode, lastUtility + score);

			backpropogateOnNode(leafNode.parentNode, score);
		}
	}

	public int simulateOnNode(Node leafNode, StateMachine stateMachine, Role role) {

		int total = 0;
		int depths[] = new int[1];
		int roleIndex = stateMachine.getRoleIndices().get(role);
		int numProbes = 3;
		for (int i = 0; i < numProbes; i ++) {
			MachineState terminalState = null;
			try {
				terminalState = stateMachine.performDepthCharge(leafNode.state, depths);
				int roleReward = stateMachine.getGoals(terminalState).get(roleIndex);
				total += roleReward;
			} catch (TransitionDefinitionException e) {
				// TODO Auto-generated catch block

				e.printStackTrace();
			} catch (MoveDefinitionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (GoalDefinitionException ge) {
				System.out.println("terminal State: " + terminalState);
				ge.printStackTrace();
			}
		}

		return total/numProbes;
	}


	public void expandOnNode(Node leafNode, StateMachine stateMachine, Role role) {

		if (leafNode.childNodesUtilities.keySet().size() > 0) {
			//System.out.println("returning bc this node already has child nodes");
			return;
		}

		try {
			List <Move> actions = stateMachine.getLegalMoves(leafNode.state, role);
			for (Move action: actions) {
				leafNode.moveUtility.put(action, 0);
				leafNode.moveVisits.put(action, 0);
				List<List<Move>> jointActions = stateMachine.getLegalJointMoves(leafNode.state, role, action);
				for (List<Move> jointAction: jointActions) {
					try {
						MachineState nextState = stateMachine.getNextState(leafNode.state, jointAction);
						Node newNode = new Node(nextState, leafNode, action);
						leafNode.childNodesUtilities.put(newNode, 0);
						leafNode.childNodesVisits.put(newNode, 0);
					} catch (TransitionDefinitionException e) {
						System.out.println(e);
					}
				}
			}
		}
		catch ( MoveDefinitionException e) {
			System.out.println(e);
		}
	}


	public Node selectNode(Node node, StateMachine stateMachine){

		if (stateMachine.isTerminal(node.state)) {
			//System.out.println("returning a terminal node");
			return node;
		}

		if (node.moveVisits.keySet().size() == 0) {
			//System.out.println("returning a move visits 0 node");
			return node;
		}

		for (Node child: node.childNodesVisits.keySet()) {
			if (child.moveVisits.keySet().size() == 0) {
				//System.out.println("returning an empty child move visits node");
				return child;
			}
		}
		double score = 0.0;
		Node result = node;
		ArrayList<Double> results = new ArrayList<Double>();
		for (Node child: node.childNodesVisits.keySet()) {
			double newScore = selectFn(child);
			results.add(newScore);
			if (newScore > score) {
				score = newScore;
				result = child;
			}
		}
		//System.out.println("all scores are" + results);
		if (result == node) {
			//System.out.println("returning a result = node");
			return node;
		}

		return selectNode(result, stateMachine);
	}

	public double selectFn(Node node) {

		double c = 50.0;
		int nodeUtilityRatio = utilityRatioFromNode(node);

//		System.out.println("SELECT FN STATS");
//		System.out.println("parent node total visits: " + node.parentNode.totalVisits);
//		System.out.println(" node total visits: " + node.totalVisits);
//		System.out.println("2nd term value: " + Math.sqrt(2*Math.log(node.parentNode.totalVisits)/node.totalVisits));

		double secondTerm = 1;
		if (node.totalVisits > 0) {
//			System.out.println("node total visits is greater than 0!!!!!");
//			System.out.println("parent visits: " + node.parentNode.totalVisits);
//			System.out.println("node visits: " + node.totalVisits);
			secondTerm = Math.sqrt(2*Math.log(node.parentNode.totalVisits)/node.totalVisits);
		}
		if (node.parentNode.totalVisits < node.totalVisits) {
			//System.out.println("parent node less than child, something is wrong!!!");
		}
		return (double) (nodeUtilityRatio + c*secondTerm);

	}

	public int utilityRatioFromNode(Node node) {

		int totalUtility = 0;
		for (Move move: node.moveUtility.keySet()) {
			int denom = 1;
			if (node.moveVisits.get(move) > 0) {
				denom = node.moveVisits.get(move);
			}
			totalUtility += node.moveUtility.get(move) / denom;
		}
		return (int) totalUtility / node.moveUtility.keySet().size();

	}

	public class Node {
		Map <Move, Integer> moveVisits = new HashMap <Move, Integer>();
		Map <Move, Integer> moveUtility = new HashMap <Move, Integer>();

		Map <Node, Integer> childNodesUtilities = new HashMap <Node, Integer>();
		Map <Node, Integer> childNodesVisits = new HashMap <Node, Integer>();

		Move lastParentMove;
		Node parentNode;
		MachineState state;
		int totalVisits = 0;

		public Node(MachineState state, Node parentNode, Move lastParentMove) {
			this.state = state;
			this.parentNode = parentNode;
			this.lastParentMove = lastParentMove;
		}
	}






























	public class CombinedMove{
		Move move;
		int score;
		public CombinedMove(Move m, int score){
			this.move = m;
			this.score = score;
		}
	}

	public CombinedMove bestAlphaBetaMove(Role role, MachineState currentState, StateMachine stateMachine, int level, long finishBy) {

		CombinedMove moveAction = new CombinedMove(null, 0);
		Move action = null;
		try {
			List<Move> actions = stateMachine.getLegalMoves(currentState, role);
			action = actions.get(0);
			int score = 0;
			for (int i=0; i<actions.size(); i++){
				int result = minScoreAlphaBeta(role, actions.get(i), currentState, stateMachine, 0, 100, level, finishBy);
			    if (result >= score) {
			    	score = result;
			    	action = actions.get(i);
			    }
			}
			moveAction.move = action;
			moveAction.score = score;

		}catch (MoveDefinitionException e) {
			System.out.println(e);
		}
		if (action == null) {
			  try {
				  moveAction.move = stateMachine.getLegalMoves(currentState, role).get(0);
				  moveAction.score = 0;
				  return moveAction;
//				  return stateMachine.getLegalMoves(currentState, role).get(0);
			  } catch (MoveDefinitionException e) {
				  System.out.println(e);
			  }
		 }
		return moveAction;
//		return action;
	}

	////////////////////////////////////////////////////////////////////////////

	public int minScoreAlphaBeta(Role role, Move action, MachineState state, StateMachine stateMachine, int alpha, int beta, int level, long finishBy) {
		try {
			List<List<Move>> allGroupMoves = stateMachine.getLegalJointMoves(state, role, action);
			if (System.currentTimeMillis() > finishBy){
				return alpha;
			}
			for (int i = 0; i < allGroupMoves.size(); i++) {
				try {
					MachineState nextState = stateMachine.getNextState(state, allGroupMoves.get(i));
					int result = maxScoreAlphaBeta(role, nextState, stateMachine, alpha, beta, level, finishBy);
					beta = Math.min(result, beta);
					if (beta <= alpha) {
						return alpha;
					}

				} catch (TransitionDefinitionException e) {
					System.out.println(e);
				}
			}

		} catch (MoveDefinitionException e) {
			System.out.println(e);
		}
		return beta;
	}
	//Return the number of legal actions -> thinking here is more legal actions is normally better
	public int mobilityHeuristic(Role role, MachineState state, StateMachine stateMachine){
		 try{
			  List<Move> legalActions = stateMachine.getLegalMoves(state, role);
			  List<Move> allActions = stateMachine.findActions(role);

			  double percent = 100 * ( (double) legalActions.size() )/allActions.size();
			  return (int) percent;

		  }
		  catch (MoveDefinitionException e) {
				System.out.println(e);
				return 0;
		  }
	}
	//Returns the average intersection between a given state's properties and all terminal states

	public int goalProxHelper(Role role, MachineState state, StateMachine stateMachine){
		Map<GdlSentence, Integer>propScores = roleToPropositionScores.get(role);
		Map<GdlSentence, Integer>propCounts = roleToPropositionCounts.get(role);
		double score = 0;
		for(GdlSentence p: state.getContents()){
			if (propScores.containsKey(p)) {
				double averagePropositionScore = propScores.get(p)/ ((double) propCounts.get(p));
				double propMeansTerminalState = ((double) propCounts.get(p)) / totalTerminalStatesDiscovered;
				score += averagePropositionScore*propMeansTerminalState;
			}
		}
		return (int) score;
	}

	public int simpleGoalHeuristic(Role role, MachineState state, StateMachine stateMachine){
		if(gameHasGoals) {
			try {
				return stateMachine.getGoal(state, role);
			} catch (GoalDefinitionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return 0;
			}
		}
		return 0;
	}

	public int goalProximityHeuristic(Role role, MachineState state, StateMachine stateMachine){
			// Get a bunch of terminal states, but you aren't likely to win in each of them...
			// So collect terminal states for each state add it's value to score
			// 100 = 1.0 similar * 100 score...
			// how similar is intersection over terminalstate... score is your own score... then average all

			//Average score of propositions
			//Look at difference between average score for you and opponent...
			int ourScore = 0;
			int bestOpponent = 0;
			for(Role r: stateMachine.getRoles()){
				int score = goalProxHelper(r, state,stateMachine);
				if(!r.equals(role)) {
					bestOpponent = Math.max(bestOpponent, score);
				} else{
					ourScore = score;
				}
			}
			if(ourScore > bestOpponent){
				return ourScore - bestOpponent;
			}
			return 0;

	}
	public int evalfn(Role role, MachineState state, StateMachine stateMachine){
		return 0;
	}
	public int weightedAverageHeuristic(Role role, MachineState state, StateMachine stateMachine){
		double gph = 0;
		//(int) (.5* goalProximityHeuristic(role,state,stateMachine));

		if(gameHasGoals){
			gph = (.5* simpleGoalHeuristic(role,state,stateMachine));
//			gph = (gph + (int)(.5* simpleGoalHeuristic(role,state,stateMachine))) /2;
		}

//		int mh = (int) .2* mobilityHeuristic(role, state, stateMachine);
//		int opf = (int) .2* opponentMobilityHeuristic(role,  state,  stateMachine);
		double jointMobility = .5 * jointMobilityHeuristic(role,state,stateMachine);
//		System.out.println("Mobility score is " + jointMobility + "goals score is " + gph);

		return (int) (jointMobility + gph);
	}


	public boolean opponentCanMove(Role role, MachineState state, StateMachine stateMachine){
		List<Role> allRoles = stateMachine.getRoles();
		if(allRoles.size() == 1){
			return false;
		}
		boolean theirTurn = false;
		for (Role other_role: allRoles) {
			if (!other_role.equals(role)){
				try {
					List<Move> otherPlayersMoves = stateMachine.getLegalMoves(state, other_role);
					if(otherPlayersMoves.size() > 1){
						theirTurn = true;
					}
				}
				catch(MoveDefinitionException e ){
					System.out.println(e);
					return false;
				}

			}
		}
		return theirTurn;
	}

	public int jointMobilityHeuristic(Role role, MachineState state, StateMachine stateMachine){
		//Just my turn return just my mobility
		// Just opponents turn return just their focus
		// Both, i.e. a simultaneous game, then return average...

		try {
			boolean myTurn = stateMachine.getLegalMoves(state, role).size() > 1;
			boolean theirTurn = opponentCanMove( role,  state,  stateMachine);
			if(myTurn && !theirTurn){
				return mobilityHeuristic(role,state, stateMachine);
			}
			if(theirTurn && !myTurn ){
				return opponentMobilityHeuristic(role,state, stateMachine);
			}
			return ( mobilityHeuristic(role,state, stateMachine) + opponentMobilityHeuristic(role,state, stateMachine))/2;
			//if at least one opponent has more than just noop look at their mobility
		}
		catch (MoveDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}

	}
	//Right now it sees anytime where it is my turn as my opponent's move's being limited...


	//How to evaluate the opponent's mobility in two person game...
	public int opponentMobilityHeuristic(Role role, MachineState state, StateMachine stateMachine){
//		return 0;
		try {
			//If it ends on my turn use my mobility options... ends on opponents use their focus... on both use average...
			List<Role> allRoles = stateMachine.getRoles();
			if(allRoles.size() == 1){
				return 0;
			}

			List<Move>allMoves = stateMachine.findActions(role);
			int allMovesSize = allMoves.size();
			double opponentsTotalMobility = 0;
			for (Role other_role: allRoles) {
				if (!other_role.equals(role)){
					List<Move> otherPlayersMoves = stateMachine.getLegalMoves(state, other_role);
					double percent = ((double) otherPlayersMoves.size()) / allMovesSize;
					opponentsTotalMobility += percent;
				}
			}
			double averageMobility = opponentsTotalMobility/(allRoles.size()-1);
			return (int)(100 - averageMobility*100);
		}
		catch (MoveDefinitionException e){
			System.out.println(e);
			return 0;
		}
	}

	public int monteCarloScore(Role role, MachineState state, StateMachine stateMachine, int numProbes) {
		int depths[] = new int[1];
		int total = 0;
		int roleIndex = stateMachine.getRoleIndices().get(role);
		for( int i = 0; i < numProbes; i ++){
			try {
				MachineState terminalState = stateMachine.performDepthCharge(state, depths);
				int roleReward = stateMachine.getGoals(terminalState).get(roleIndex);
				total += roleReward;
				totalDepthChargesThisTurn += 1;
			}
			catch(TransitionDefinitionException te){
				System.out.println(te);
			}
			catch(MoveDefinitionException me) {
				System.out.println(me);
			}
			catch(GoalDefinitionException me) {
				System.out.println(me);
			}
		}
		return total/numProbes;
	}

	public int maxScoreAlphaBeta(Role role, MachineState state, StateMachine stateMachine, int alpha, int beta, int level,long finishBy) {
		if (stateMachine.isTerminal(state)) {
			try {
				return stateMachine.getGoal(state, role);
			} catch (GoalDefinitionException e) {
				System.out.println(e);
			}
		}
		if (level <= 0) {
			if(gameHasGoals){
				return weightedAverageHeuristic(role, state, stateMachine);
			}
//			return mobilityHeuristic(role, state, stateMachine);
//			return evalfn(role, state, stateMachine);
//			return opponentMobilityHeuristic(role,state, stateMachine);
//			return jointMobilityHeuristic(role,state,stateMachine);
			return monteCarloScore(role, state, stateMachine, 5);
//			return weightedAverageHeuristic(role, state, stateMachine);
//			return goalProximityHeuristic(role,state, stateMachine);
		}

		try {
			if (System.currentTimeMillis() > finishBy){
				return beta;
			}
			List<Move> actions = stateMachine.getLegalMoves(state, role);
			for (int i=0; i<actions.size(); i++){
				int result = minScoreAlphaBeta(role, actions.get(i), state, stateMachine, alpha, beta, level - 1, finishBy);
			    alpha = Math.max(alpha, result);
			    if (alpha >= beta) {
			    	return beta;
			    }
			}
		} catch (MoveDefinitionException e) {
			System.out.println(e);
		}
		return alpha;
	}




























	////////////////////////////////////////////////////////////////////////////

	public Move bestMinimaxMove(Role role, MachineState currentState, StateMachine stateMachine) {
		Move action = null;
		try {
			List<Move> actions = stateMachine.getLegalMoves(currentState, role);
			action = actions.get(0);
			int score = 0;
			for (int i=0; i<actions.size(); i++){
				int result = minScoreMinimax(role, actions.get(i), currentState, stateMachine);
			    if (result > score) {
			    	score = result;
			    	action = actions.get(i);
			    }
			}

		}catch (MoveDefinitionException e) {
			System.out.println(e);
		}
		if (action == null) {
			  try {
				  return stateMachine.getLegalMoves(currentState, role).get(0);
			  } catch (MoveDefinitionException e) {
				  System.out.println(e);
			  }
		 }
		return action;
	}

	public int minScoreMinimax(Role role, Move action, MachineState state, StateMachine stateMachine) {
		int score = 100;
		try {
			List<List<Move>> allGroupMoves = stateMachine.getLegalJointMoves(state, role, action);

			for (int i = 0; i < allGroupMoves.size(); i++) {
				try {
					MachineState nextState = stateMachine.getNextState(state, allGroupMoves.get(i));
					int result = maxScoreMinimax(role, nextState, stateMachine);
					if (result < score) {
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

	public int maxScoreMinimax(Role role, MachineState state, StateMachine stateMachine) {
		if (stateMachine.isTerminal(state)) {
			try {
				return stateMachine.getGoal(state, role);
			} catch (GoalDefinitionException e) {
				System.out.println(e);
			}
		}

		int score = 0;
		try {
			List<Move> actions = stateMachine.getLegalMoves(state, role);
			for (int i=0; i<actions.size(); i++){
				int result = minScoreMinimax(role, actions.get(i), state, stateMachine);
			    if (result > score) {
			    	score = result;
			    }
			}
		} catch (MoveDefinitionException e) {
			System.out.println(e);
		}
		return score;

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