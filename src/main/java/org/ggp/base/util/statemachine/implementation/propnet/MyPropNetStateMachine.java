package org.ggp.base.util.statemachine.implementation.propnet;


import java.util.ArrayList;
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



public class MyPropNetStateMachine extends StateMachine {
    /** The underlying proposition network  */
    private PropNet propNet;
    private PropNet factoredPropNet;
    /** The topological ordering of the propositions */
    private List<Proposition> ordering;
    /** The player roles */
    private List<Role> roles;
    private Set<Component> factors;
    private boolean isInitial = false;

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
	        ordering = getOrdering();

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
    }

    public void factor(){
        isInitial = true;
        propmarkp(this.propNet.getTerminalProposition());
        propmarkp(this.propNet.getInitProposition());
//        Map<GdlSentence, Proposition> baseProps = this.propNet.getBasePropositions();
//        for (Proposition p: baseProps.values()){
//        	propmarkp(p);
//        }
        for (Role role: this.propNet.getGoalPropositions().keySet()) {
        	Set<Proposition> props = this.propNet.getGoalPropositions().get(role);
        	for (Proposition prop: props) {
        		propmarkp(prop);
        	}
        }
        for (Role role: this.propNet.getLegalPropositions().keySet()) {
        	Set<Proposition> props = this.propNet.getLegalPropositions().get(role);
        	for (Proposition prop: props) {
        		propmarkp(this.propNet.getLegalInputMap().get(prop));
        		propmarkp(prop);
        	}
        }
//        propNet = new PropNet(this.roles, factors);
		this.propNet.renderToFile("tic_tac_toe_propnet.dot");
        isInitial = false;
        System.out.println("Number of factors: " + factors.size());
    }
	/**
	 * Computes if the state is terminal. Should return the value
	 * of the terminal proposition for the state.
	 */
	@Override
	public boolean isTerminal(MachineState state) {
		clearPropnet();
		markBases(state);
		return propmarkp(this.propNet.getTerminalProposition());
	}

	public void markBases(MachineState state) {
//		System.out.println("mark bases is called");
		for (GdlSentence sentence: state.getContents()) {
			Proposition p = this.propNet.getBasePropositions().get(sentence);
			if (p != null) {
				p.setValue(true);
				this.propNet.getBasePropositions().put(sentence, p);
			}
		}
	}

	public void markActions(List<Move> moves, MachineState state) {
		 List<GdlSentence> sentences = toDoes(moves);
		 for (GdlSentence sentence: sentences) {
			 Proposition inputP = this.propNet.getInputPropositions().get(sentence);
			 if (inputP != null){
				 inputP.setValue(true);
			 }
		 }
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

	public boolean propmarkp (Component c){
		if (isInitial){
			factors.add(c);
		}
		if (c.equals(this.propNet.getInitProposition())) {
			return false;
		}
		if (c instanceof Constant) {
//			System.out.println("Constant " + c.getValue());
			return c.getValue();
		}
		if (c instanceof Transition){
//			System.out.println("Transition" +  c.getSingleInput().getValue());
			return propmarkp(c.getSingleInput());
		}
		if (this.propNet.getBasePropositions().values().contains(c)) { // base

			return c.getValue();
		}
		if (this.propNet.getInputPropositions().values().contains(c)) { // input
			return c.getValue();
		}
		if (this.propNet.getPropositions().contains(c)) { // view - will always depend on one component
			boolean returnVal = propmarkp(c.getSingleInput());
//			System.out.println("View" + returnVal);
			return returnVal;
		}
		if (c instanceof Not) {
			return propmarknegation(c);
		}
		if (c instanceof And) {
			return propmarkconjunction(c);
		}
		if (c instanceof Or) {
			return propmarkdisjunction(c);
		}
		return false;
	}

	public boolean propmarknegation(Component c) {
		return !propmarkp(c.getSingleInput());
	}

	public boolean propmarkconjunction(Component c) {
		for (Component newC: c.getInputs()) {
			if (!propmarkp(newC)) {
				return false;
			}
		}
		return true;
	}

	public boolean propmarkdisjunction(Component c) {
		for (Component newC: c.getInputs()) {
			if (propmarkp(newC)) {
				return true;
			}
		}
		return false;
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
		markBases(state);
		Set<Proposition> props = new HashSet<Proposition>();
		for(Role thisRole: this.propNet.getRoles()) {
			if (thisRole.equals(role)) {
				props = this.propNet.getGoalPropositions().get(thisRole);
				break;
			}
		}
		for(Proposition p: props) {
			if(propmarkp(p)){
				return getGoalValue(p);
			}
		}
		return 0;
	}

	/**
	 * Returns the initial state. The initial state can be computed
	 * by only setting the truth value of the INIT proposition to true,
	 * and then computing the resulting state.
	 */
	@Override
	public MachineState getInitialState() {
		// TODO: Compute the initial state.
		this.propNet.getInitProposition().setValue(true);

		Set<GdlSentence> trueSentences = new HashSet<GdlSentence>();

		for(Proposition p: this.propNet.getBasePropositions().values()){
			if (p.getSingleInput().getSingleInput().getValue()){
				trueSentences.add(p.getName());
			}
		}


		MachineState newState = new MachineState(trueSentences);
		markBases(newState);
		for (Proposition p: this.propNet.getBasePropositions().values()) {
			if (propmarkp(p.getSingleInput())) {
				System.out.println("this base was true" + p);
			}
		}
		this.propNet.renderToFile("orig_tic_tac_toe_propnet.dot");
		return newState;
	}


	  @Override
	 public List<Move> findActions(Role role)
	 throws MoveDefinitionException {
	        // TODO: Compute legal moves.
		   System.out.println("find actions is called");
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
		clearPropnet();
		markBases(state);
		Set<Proposition> allLegalProps = new HashSet<Proposition>();
		List<Move> legalMoves = new ArrayList<Move>();
//		System.out.println("get roles: " + this.propNet.getRoles());

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
			if(propmarkp(lp))
			{
				Proposition actionProp = this.propNet.getLegalInputMap().get(lp);
				if(actionProp != null){
					legalMoves.add(getMoveFromProposition(actionProp));
				}
			} else{
				Proposition actionProp = this.propNet.getLegalInputMap().get(lp);
			}
		}
		if(legalMoves.size() == 0){
			List<Move> oneMove = new ArrayList<Move>();
			oneMove.add(Move.create("noop"));
			return oneMove;
		}
		return legalMoves;
	}

	/**
	 * Computes the next state given state and the list of moves.
	 */
	@Override
	public MachineState getNextState(MachineState state, List<Move> moves)
	throws TransitionDefinitionException {
		//Forward pseudo code
		//markActions + propagate their values forward until they hit base prop values
		// Add all true base props

		//mark all actions not played as false
		// Topological ordering of *changed* input propositions
		// Propagate until you reach transition
		// Then return all true base props



		//forward not necessary to do this
		clearPropnet();

		//set all input components
		markActions(moves, state);

		//Unnecessary in forward prop...
		markBases(state);

		Set<GdlSentence> trueSentences = new HashSet<GdlSentence>();

		//for all base components of a state
		// Forward prop will probably just need to go over the changed base values...
		for (Proposition p: this.propNet.getBasePropositions().values()) {
			if (propmarkp(p.getSingleInput())) {
//				System.out.println(p.toString() + p.getSingleInput());
				trueSentences.add(p.getName());
			}
		}
		MachineState newState = new MachineState(trueSentences);

		return newState;
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
	public List<Proposition> getOrdering()
	{
	    // List to contain the topological ordering.
	    List<Proposition> order = new LinkedList<Proposition>();

		// All of the components in the PropNet
		List<Component> components = new ArrayList<Component>(propNet.getComponents());

		// All of the propositions in the PropNet.
		List<Proposition> propositions = new ArrayList<Proposition>(propNet.getPropositions());

	    // TODO: Compute the topological ordering.

		return order;
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