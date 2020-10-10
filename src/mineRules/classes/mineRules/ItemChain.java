package mineRules;

import java.util.HashSet;
import java.util.List;

public class ItemChain {
	
	private int chainID;
	private double Support;
	private String EndpointEntity;
	private HashSet<String> Entities_Var;
	//private RDFNode EntidadeEndpoint;
	private List<String> Relations_Parameter;
	
	public ItemChain(int chainID, HashSet<String> Entities_Var, String EndpointEntity, List<String> Relations_Parameter, double Support) {
		
		this.chainID = chainID;
		this.Support = Support;
		this.Entities_Var = Entities_Var;
		this.EndpointEntity = EndpointEntity;
		this.Relations_Parameter = Relations_Parameter;
	}
	
	HashSet<String> getEntities(){
		return this.Entities_Var;
	}
	
	double getSuport() {
		return this.Support;
	}
	
	int getChainID() {
		return this.chainID;
	}
	
	List<String> getRelations(){
		return this.Relations_Parameter;
	}
	
	String getEndpoint() {
		return this.EndpointEntity;
	}
	
	double getNumberOfEntities() {
		return this.Entities_Var.size();
	}
}


