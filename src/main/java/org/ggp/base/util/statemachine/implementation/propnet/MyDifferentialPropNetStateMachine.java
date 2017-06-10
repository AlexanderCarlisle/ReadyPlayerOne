package org.ggp.base.util.statemachine.implementation.propnet;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ggp.base.util.gdl.grammar.Gdl;
import org.ggp.base.util.gdl.grammar.GdlConstant;
import org.ggp.base.util.gdl.grammar.GdlRelation;
import org.ggp.base.util.gdl.grammar.GdlSentence;
import org.ggp.base.util.propnet.architecture.Component;
import org.ggp.base.util.propnet.architecture.PropNet;
import org.ggp.base.util.propnet.architecture.components.And;
import org.ggp.base.util.propnet.architecture.components.Constant;
import org.ggp.base.util.propnet.architecture.components.Not;
import org.ggp.base.util.propnet.architecture.components.Or;
import org.ggp.base.util.propnet.architecture.components.Proposition;
import org.ggp.base.util.propnet.architecture.components.Transition;
import org.ggp.base.util.propnet.factory.OptimizingPropNetFactory;
import org.ggp.base.util.statemachine.MachineState;
import org.ggp.base.util.statemachine.Move;
import org.ggp.base.util.statemachine.Role;
import org.ggp.base.util.statemachine.StateMachine;
import org.ggp.base.util.statemachine.exceptions.GoalDefinitionException;
import org.ggp.base.util.statemachine.exceptions.MoveDefinitionException;
import org.ggp.base.util.statemachine.exceptions.TransitionDefinitionException;
import org.ggp.base.util.statemachine.implementation.prover.query.ProverQueryBuilder;


public class MyDifferentialPropNetStateMachine extends StateMachine {
    /** The underlying proposition network  */
    private PropNet propNet;
    private PropNet factoredPropNet;
    /** The topological ordering of the propositions */
    private List<Component> ordering;
    /** The player roles */
    private List<Role> roles;
    private Set<Component> factors;
    private boolean firstTimePropogating = false;
    private MachineState lastCalculatedState;
    private Map<Component, Boolean> allComponentValues;
    private Map<Component, Boolean> nextComponentValues;

    private Set<Component> hasBeenSeen;
    private Map<Component, Integer> orderMap;

    /**
     * Initializes the PropNetStateMachine. You should compute the topological
     * ordering here. Additionally you may compute the initial state here, at
     * your discretion.
     */
    @Override
    public void initialize(List<Gdl> description) {
        try {
        	factors = new HashSet<Component>();
			propNet = OptimizingPropNetFactory.create(description);
	        roles = propNet.getRoles();

	        System.out.println("starting up diff prop" + " there are "  + propNet.getNumNots() + "nots");
	        allComponentValues = new HashMap<Component, Boolean>();

	        orderMap = new HashMap<Component, Integer>();
	        for( Component c: this.propNet.getComponents()){
	        	allComponentValues.put(c, false);
	        }
	        ordering = getOrdering();

	        for(int i =0; i < ordering.size(); i ++){
	        	orderMap.put(ordering.get(i), i);
	        }

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
    }

//    public void factor(){
//        firstTimePropogating = true;
//        propmarkp(this.propNet.getTerminalProposition());
//        propmarkp(this.propNet.getInitProposition());
////        Map<GdlSentence, Proposition> baseProps = this.propNet.getBasePropositions();
////        for (Proposition p: baseProps.values()){
////        	propmarkp(p);
////        }
//        for (Role role: this.propNet.getGoalPropositions().keySet()) {
//        	Set<Proposition> props = this.propNet.getGoalPropositions().get(role);
//        	for (Proposition prop: props) {
//        		propmarkp(prop);
//        	}
//        }
//        for (Role role: this.propNet.getLegalPropositions().keySet()) {
//        	Set<Proposition> props = this.propNet.getLegalPropositions().get(role);
//        	for (Proposition prop: props) {
//        		propmarkp(this.propNet.getLegalInputMap().get(prop));
//        		propmarkp(prop);
//        	}
//        }
////        propNet = new PropNet(this.roles, factors);
//		this.propNet.renderToFile("tic_tac_toe_propnet.dot");
//        firstTimePropogating = false;
//        System.out.println("Number of factors: " + factors.size());
//    }
	/**
	 * Computes if the state is terminal. Should return the value
	 * of the terminal proposition for the state.
	 */
	@Override
	public boolean isTerminal(MachineState state) {
//		if(state.equals(this.lastCalculatedState)){
//			return allComponentValues.get(this.propNet.getTerminalProposition());
//		}
		this.updatePropNet(state, new ArrayList<Move>());
		return allComponentValues.get(this.propNet.getTerminalProposition());
	}

	public List<Component> markBases(MachineState state) {
//		System.out.println("mark bases is called");
		//Goes over all true base propositions
		Set<Component> changedBaseProps = new HashSet<Component>();
		for( GdlSentence baseSentence : this.propNet.getBasePropositions().keySet()){
			Proposition baseP = this.propNet.getBasePropositions().get(baseSentence);
			if(firstTimePropogating){
				changedBaseProps.add(baseP);
			}

			if (state.getContents().contains(baseSentence)){
				//The corresponding base prop should be true
//				System.out.println("setting this bp to true: " + baseP);
				baseP.setValue(true);

				if( allComponentValues.get(baseP) == false){
					changedBaseProps.add(baseP);
				}
			}
			else {
				baseP.setValue(false);
				if (allComponentValues.get(baseP) == true){
//					System.out.println("setting this bp to be false because it was true before: " + baseP);
					changedBaseProps.add(baseP);
				}
			}
		}
		return new ArrayList<Component>(changedBaseProps);
	}

	public List<Component> markActions(List<Move> moves, MachineState state) {
		List<Component> changedInputProps = new ArrayList<Component>();
		if (moves.size() == 0){ return changedInputProps; }
		Set<GdlSentence> sentences = new HashSet<GdlSentence>(toDoes(moves));
		for(GdlSentence inputSentence : this.propNet.getInputPropositions().keySet()){
			Proposition inputP = this.propNet.getInputPropositions().get(inputSentence);

			if (sentences.contains(inputSentence)){
				//The corresponding base prop should be true
				inputP.setValue(true);
				if (allComponentValues.get(inputP) == false){
					changedInputProps.add(inputP);
				}
			}
			else {
				inputP.setValue(false);
				if (allComponentValues.get(inputP) == true){
					changedInputProps.add(inputP);
				}
			}
		}
		return changedInputProps;
	}

	public boolean clearPropnet() {
		for (Proposition p: this.propNet.getBasePropositions().values()) {
			p.setValue(false);
		}
		for (Proposition p: this.propNet.getInputPropositions().values()) {
			p.setValue(false);
		}

		return true;
	}

	/**
	 * Computes the goal for a role in the current state.
	 * Should return the value of the goal proposition that
	 * is true for that role. If there is not exactly one goal
	 * proposition true for that role, then you should throw a
	 * GoalDefinitionException because the goal is ill-defined.
	 */
	@Override
	public int getGoal(MachineState state, Role role)
	throws GoalDefinitionException {
//		clearPropnet();
		Set<Proposition> props = new HashSet<Proposition>();
		for(Role thisRole: this.propNet.getRoles()) {
			if (thisRole.equals(role)) {
				props = this.propNet.getGoalPropositions().get(thisRole);
				break;
			}
		}
//		if(this.lastCalculatedState.equals(state)){
//			for(Proposition p: props) {
//				if(allComponentValues.get(p)){
//					return getGoalValue(p);
//				}
//			}
//		}
		updatePropNet(state, new ArrayList<Move>());
		for(Proposition p: props) {
			if(allComponentValues.get(p)){
				return getGoalValue(p);
			}
		}
		return 0;
	}

	/**
	 * Returns the initial state. The initial state can be computed
	 * by only setting the truth value of the INIT proposition to true,
	 * and then computing the resulting state.
	 * Need to propagate once through everything...
	 */
	@Override
	public MachineState getInitialState() {
//		System.out.println("Calling get initial state");
		//Initialize all values of the prop net

		// TODO: Compute the initial state.
		this.propNet.getInitProposition().setValue(true);
		// Diff prop on initial one
		diffForwardProp(this.propNet.getInitProposition());
		//collect all base states create new state
		Set<GdlSentence> trueSentences = new HashSet<GdlSentence>();
		for(Proposition p: this.propNet.getBasePropositions().values()){
			if (p.getSingleInput().getSingleInput().getValue()){
				trueSentences.add(p.getName());
//				System.out.println("IS THE TRUTH" + p);
			}
//			System.out.println("IS A BASE PROP" + p);
		}

		MachineState newState = new MachineState(trueSentences);
		this.propNet.getInitProposition().setValue(false);
		diffForwardProp(this.propNet.getInitProposition());
		this.propNet.renderToFile("orig_tic_tac_toe_propnet.dot");
		firstTimePropogating = true;

		return newState;
	}


	 @Override
	 public List<Move> findActions(Role role)
	 throws MoveDefinitionException {
	        // TODO: Compute legal moves.
			List<Move> allMoves = new ArrayList<Move>();

			Set<Proposition> legalProps = new HashSet<Proposition>();
			for (Role thisRole: this.propNet.getRoles()) {
				if (thisRole.equals(role)) {
					legalProps = this.propNet.getLegalPropositions().get(thisRole);
					break;
				}
			}
			for (Proposition p: legalProps) {
				allMoves.add(getMoveFromProposition(this.propNet.getLegalInputMap().get(p)));
			}
	        return allMoves;
	   }

	/**
	 * Computes the legal moves for role in state.
	 */
	@Override
	public List<Move> getLegalMoves(MachineState state, Role role)
	throws MoveDefinitionException {
//		System.out.println("getting legal moves for this state");

//		for(GdlSentence s : state.getContents()){
//			this.propNet.getBasePropositions().get(s).setValue(true);
////			System.out.println(this.propNet.getBasePropositions().get(s));
//		}

//		if(this.lastCalculatedState == null || !this.lastCalculatedState.equals(state)){
//			System.out.println("recalculating propnet to get legal moves");
			this.updatePropNet(state, new ArrayList<Move>());
			firstTimePropogating = false;
//		}

		Set<Proposition> allLegalProps = new HashSet<Proposition>();
		List<Move> legalMoves = new ArrayList<Move>();

		for (Role thisRole: this.propNet.getRoles()) {
			if (thisRole.equals(role)) {
				allLegalProps = this.propNet.getLegalPropositions().get(thisRole);//these are legal action props
				if (allLegalProps == null){
					allLegalProps = new HashSet<Proposition>();
				}
				break;
			}
		}

		for (Proposition lp: allLegalProps) {
			if(this.allComponentValues.get(lp))
			{
				Proposition actionProp = this.propNet.getLegalInputMap().get(lp);
				if(actionProp != null){
					legalMoves.add(getMoveFromProposition(actionProp));
				}
			}
		}
		if(legalMoves.size() == 0){
			List<Move> oneMove = new ArrayList<Move>();
			oneMove.add(Move.create("noop"));
			return oneMove;
		}
		return legalMoves;
	}

	private void updatePropNet(MachineState state, List<Move> moves){
		clearPropnet();
		List<Component> changedProps = markActions(moves, state);
		changedProps.addAll(markBases(state));

//		System.out.println("Observe. There are " + changedProps.size() + " changed propositions. They are ->" + changedProps);
		for (Component c: changedProps){
//			System.out.println("next recursive run on" + c);
			diffForwardProp(c);
		}
	}
	/**
	 * Computes the next state given state and the list of moves.
	 */
	private boolean calculateValue(Component c){
		boolean newCalculatedValue = false;
		if(c instanceof And){
			newCalculatedValue = true;
			for (Component newC: c.getInputs()) {
//				System.out.println("my input is" + newC + " has value " + allComponentValues.get(newC));
				if (!allComponentValues.get(newC)) {
					newCalculatedValue = false;
					break;
				}
			}
		}
		else if( c instanceof Constant){
			newCalculatedValue = c.getValue();
		}
		else if(c.equals(this.propNet.getInitProposition())) {
//			System.out.println("Init " + c + c.getValue());
			newCalculatedValue = c.getValue();
		}
		else if(c instanceof Or){
			newCalculatedValue = false;
			//Switch to get old version working
			for (Component newC: c.getInputs()) {
				if (allComponentValues.get(newC)) {
					newCalculatedValue = true;
					break;
				}
			}
		}
		else if(c instanceof Not){
			//we know that the previous value has been recalculated and updated
			newCalculatedValue = !allComponentValues.get(c.getSingleInput());
		}
		else if (this.propNet.getBasePropositions().values().contains(c)){
			newCalculatedValue = c.getValue();
		}
		else if (this.propNet.getInputPropositions().values().contains(c)){
			newCalculatedValue = c.getValue();
		}
		//If it is a view proposition
		else if( this.propNet.getPropositions().contains(c)){
			//we know that the previous value has been recalculated and updated
			newCalculatedValue = allComponentValues.get(c.getSingleInput());
		}
		return newCalculatedValue;
	}

	@Override
	public MachineState getNextState(MachineState state, List<Move> moves)
	throws TransitionDefinitionException {
//		System.out.println("Get next state");

		//set all input components
		updatePropNet(state, moves);
		Set<GdlSentence> trueSentences = new HashSet<GdlSentence>();

		//Now all the ones that were changed
//		System.out.println("Next state facts");
		for (Proposition p: this.propNet.getBasePropositions().values()) {
//			allComponentValues.put(p, allComponentValues.get(p.getSingleInput()));
			if(allComponentValues.get(p.getSingleInput())){
//				System.out.println("The next state has -> " + p);
				trueSentences.add(p.getName());
			}
		}
		for(Proposition p : this.propNet.getInputPropositions().values()){
			p.setValue(false);
			allComponentValues.put(p, false);
		}
		//clear all action all component values?

		MachineState newState = new MachineState(trueSentences);
		this.lastCalculatedState = newState;
		firstTimePropogating = false;
		return newState;
	}

	private void diffForwardProp(Component c){
//		System.out.println("\t\tcalling on"  + c);

		if (c instanceof Transition){
//			System.out.println("calling on"  + c);
			allComponentValues.put(c, allComponentValues.get(c.getSingleInput()));
//			System.out.println("Stopped on " + c + " next output is " + c.getSingleOutput() + allComponentValues.get(c.getSingleInput()));
			return;
		}
		boolean newCalculatedValue = false;
		//recalculate...

		newCalculatedValue = calculateValue(c);
//		System.out.println("calling on"  + c + " value: " + newCalculatedValue);

		if(allComponentValues.get(c) != newCalculatedValue || firstTimePropogating){
			allComponentValues.put(c, newCalculatedValue);
			for ( Component nextOutput : c.getOutputs()){
				diffForwardProp(nextOutput);
			}
		}
	}

	/**
	 * This should compute the topological ordering of propositions.
	 * Each component is either a proposition, logical gate, or transition.
	 * Logical gates and transitions only have propositions as inputs.
	 *
	 * The base propositions and input propositions should always be exempt
	 * from this ordering.
	 *
	 * The base propositions values are set from the MachineState that
	 * operations are performed on and the input propositions are set from
	 * the Moves that operations are performed on as well (if any).
	 *
	 * @return The order in which the truth values of propositions need to be set.
	 */
	public List<Component> getOrdering()
	{
	    // List to contain the topological ordering.
	    List<Component> order = new LinkedList<Component>();

		// All of the components in the PropNet

		// All of the propositions in the PropNet.
		List<Proposition> propositions = new ArrayList<Proposition>(propNet.getPropositions());
		List<Component> components = new ArrayList<Component>(this.propNet.getComponents());


	    // TODO: Compute the topological ordering of all propositions that are not base or inputs...
		Set<Component> marked = new HashSet<Component>();
		Set<Component> unmarked = new HashSet<Component>(components);
		Set<Component> temporary = new HashSet<Component>();

		while(unmarked.size() > 0){
			visit(unmarked.iterator().next(), marked, unmarked, temporary, order);
		}
		return order;
	}

	private void visit(Component p, Set<Component>  marked, Set<Component>  unmarked, Set<Component>  temporary,  List<Component> order) {
		if(temporary.contains(p)){
			return;
		}
		if(!marked.contains(p)){
			temporary.add(p);
			for(Component nextP : p.getOutputs()){
				visit(nextP, marked, unmarked, temporary, order);
			}
			marked.add(p);
			unmarked.remove(p);
			temporary.remove(p);
			order.add(0, p);
		}
	}
	/* Already implemented for you */
	@Override
	public List<Role> getRoles() {
		return roles;
	}

	/* Helper methods */

	/**
	 * The Input propositions are indexed by (does ?player ?action).
	 *
	 * This translates a list of Moves (backed by a sentence that is simply ?action)
	 * into GdlSentences that can be used to get Propositions from inputPropositions.
	 * and accordingly set their values etc.  This is a naive implementation when coupled with
	 * setting input values, feel free to change this for a more efficient implementation.
	 *
	 * @param moves
	 * @return
	 */
	private List<GdlSentence> toDoes(List<Move> moves)
	{
		List<GdlSentence> doeses = new ArrayList<GdlSentence>(moves.size());
		Map<Role, Integer> roleIndices = getRoleIndices();

		for (int i = 0; i < roles.size(); i++)
		{
			int index = roleIndices.get(roles.get(i));
			doeses.add(ProverQueryBuilder.toDoes(roles.get(i), moves.get(index)));
		}
		return doeses;
	}

	/**
	 * Takes in a Legal Proposition and returns the appropriate corresponding Move
	 * @param p
	 * @return a PropNetMove
	 */
	public static Move getMoveFromProposition(Proposition p)
	{
		return new Move(p.getName().get(1));
	}

	/**
	 * Helper method for parsing the value of a goal proposition
	 * @param goalProposition
	 * @return the integer value of the goal proposition
	 */
    private int getGoalValue(Proposition goalProposition)
	{
		GdlRelation relation = (GdlRelation) goalProposition.getName();
		GdlConstant constant = (GdlConstant) relation.get(1);
		return Integer.parseInt(constant.toString());
	}

	/**
	 * A Naive implementation that computes a PropNetMachineState
	 * from the true BasePropositions.  This is correct but slower than more advanced implementations
	 * You need not use this method!
	 * @return PropNetMachineState
	 */
	public MachineState getStateFromBase()
	{
		Set<GdlSentence> contents = new HashSet<GdlSentence>();
		for (Proposition p : propNet.getBasePropositions().values())
		{
			p.setValue(p.getSingleInput().getValue());
			if (p.getValue())
			{
				contents.add(p.getName());
			}

		}
		return new MachineState(contents);
	}
}