package mineRules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MRAR {
	
	
	int GraphSize, chainID;
	int minLevel, maxLevel;
	double minSup, minConf;
	HashSet<String> AllLOE;
	HashSet<LargeItemChain> AllLICs;
	List<Rule> Rules;
	List<ItemChain> List_ItemChains;
	HashMap<String, HashMap<String, HashSet<String>>> EntityInfo;
	

	public MRAR() {
		
	}
	
	public void mine(HashMap<String, HashMap<String, HashSet<String>>> EntityInfo, int GraphSize, double minSup, double minConf, int minLevel, int maxLevel) {
		this.AllLOE = null;
		this.AllLOE = new HashSet<>();
		this.Rules = null;
		this.Rules = new ArrayList<>();
		this.List_ItemChains = null;
		this.List_ItemChains = new ArrayList<>();
		this.chainID = 0;
		this.minSup = minSup;
		this.minConf= minConf;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		this.GraphSize = GraphSize;
		this.EntityInfo = EntityInfo;
		
		System.out.println("MRAR: Generating Itemchains..");
		for(String EndpointEntity : this.EntityInfo.keySet()) {
			for(String relation : this.EntityInfo.get(EndpointEntity).keySet()) {
				List<String> relations = new ArrayList<>();
				relations.add(relation);
				generateItemChains(EndpointEntity, relations, (this.EntityInfo.get(EndpointEntity)).get(relation), 1);
			}
		}
		
		System.out.println("MRAR: Generating 2 Large ItemChains..");
		this.AllLICs = generate2LargeItemChains();
		
		List<LargeItemChain> LLICs = new ArrayList<>();
		LLICs.addAll(this.AllLICs);
		int L = 1;
		HashSet<HashSet<ItemChain>> Candidates = new HashSet<>();
		
		System.out.println("MRAR: Generating L Large ItemChains..");
		do {		
			L = L + 1;
			Candidates.clear();
			for(int i = 0; i < LLICs.size()-1; i++) {
				for(int j = i+1; j < LLICs.size(); j++) {
					HashSet<ItemChain> Set_ICs = new HashSet<>();	
					Set_ICs.addAll(LLICs.get(i).getSetICs());
					Set_ICs.retainAll(LLICs.get(j).getSetICs());
					if( Set_ICs.size() == L-1) {
						HashSet<ItemChain> aux = new HashSet<>();
						aux.addAll(LLICs.get(j).getSetICs());
						aux.removeAll(Set_ICs);
						Set_ICs.clear();
						Set_ICs.addAll(LLICs.get(i).getSetICs());
						//SIZE == 1
						Set_ICs.addAll(aux);
						Candidates.add(Set_ICs);
					}
				}
			}
			LLICs.clear();
			for(HashSet<ItemChain> Set_ICs : Candidates) {
				HashSet<String> LOE = new HashSet<>();
				for(ItemChain IC : Set_ICs) {
					LOE.addAll(IC.getEntities());
					break;
				}
				for(ItemChain IC : Set_ICs) {
					LOE.retainAll(IC.getEntities());
				}
				double Support = ((double)LOE.size())/((double)this.GraphSize);
				if(Support >= this.minSup) {
					LLICs.add(new LargeItemChain(Set_ICs, LOE.size(), Support));
				}
			}
			this.AllLICs.addAll(LLICs);
		}while(Candidates.size() != 0);
		
		System.out.println("MRAR: Generating Rules..");
		generateRules();
		
		System.out.println("MRAR: Mining Operation Ended!");
	}
	
	void generateItemChains(String EndpointEntity, List<String> Relations_Parameter, HashSet<String> Entities_Parameter, int Level) {
		if(Level >= this.minLevel && Level <=this.maxLevel && Entities_Parameter.size() > 1) { //aqui
			double Support = ((double)Entities_Parameter.size())/((double)this.GraphSize);
			if(Support >= this.minSup) {
				this.chainID++;
				this.List_ItemChains.add(new ItemChain(this.chainID, Entities_Parameter, EndpointEntity, Relations_Parameter, Support));
			};
		};
		if(Level < this.maxLevel) {
			List<String> Relations_Var = new ArrayList<>();
			for(String entity : Entities_Parameter){
				   if(this.EntityInfo.containsKey(entity)) {
					   for(String relation : this.EntityInfo.get(entity).keySet()) {
						   Relations_Var.add(relation);
					   }
				   }
			}
			Set<String> Relations_Var_Set = new HashSet<>(Relations_Var);
			for(String relation : Relations_Var_Set) {
				List<String> Relations = new ArrayList<>();
				Relations.addAll(Relations_Parameter);
				Relations.add(relation);
				HashSet<String> Entities_Var = new HashSet<String>();
				for(String entity : Entities_Parameter) {
					if(this.EntityInfo.containsKey(entity)) {
						if(this.EntityInfo.get(entity).containsKey(relation)) {
							Entities_Var.addAll(this.EntityInfo.get(entity).get(relation));
						}
					}
				}
				if(!Entities_Var.isEmpty()) {
					generateItemChains(EndpointEntity, Relations, Entities_Var, Level + 1);
				}
			}
		};
	}
	
	HashSet<LargeItemChain> generate2LargeItemChains(){
		HashSet<LargeItemChain> AllLICs2 = new HashSet<>();
		for(int i = 0; i < this.List_ItemChains.size() - 1; i++) {
			for (int j = i+1; j < this.List_ItemChains.size(); j++) {
				ItemChain IC1 = this.List_ItemChains.get(i);
				ItemChain IC2 = this.List_ItemChains.get(j);
				HashSet<String> LOE = new HashSet<>();
				LOE.addAll(IC1.getEntities());
				LOE.retainAll(IC2.getEntities());
				double Suporte = ((double)LOE.size())/((double)this.GraphSize);
				if( Suporte >= this.minSup) {
					AllLICs2.add(new LargeItemChain(IC1, IC2));
				}
			}
		}
		return AllLICs2;
	}
	
	void generateRules() {
		for(LargeItemChain LIC : this.AllLICs) {
			for(ItemChain IC : LIC.getSetICs()) {
				HashSet<ItemChain> Consequent = new HashSet<>();
				Consequent.add(IC);
				HashSet<ItemChain> Antecedent = new HashSet<>();
				Antecedent.addAll(LIC.getSetICs());
				Antecedent.remove(IC);
				HashSet<String> LOE = new HashSet<>();
				LOE.addAll(IC.getEntities());
				for(ItemChain IC2 : Antecedent) {
					LOE.retainAll(IC2.getEntities());
				}
				double Support_LIC = ((double)LOE.size())/((double)this.GraphSize);
				LOE.clear();
				for(ItemChain IC2 : Antecedent) {
					LOE.addAll(IC2.getEntities());
					break;
				}
				for(ItemChain IC2 : Antecedent) {
					LOE.retainAll(IC2.getEntities());
				}
				double Support_Antecedent = ((double)LOE.size())/((double)this.GraphSize);
				double Confidence = Support_LIC / Support_Antecedent;
				if(Confidence >= this.minConf)
					this.Rules.add(new Rule(Antecedent, Consequent, Confidence, Support_LIC, LOE));
					this.AllLOE.addAll(LOE);
			};	
		};
	}
	
	public void printRules() {
		int i = 0;
		for(Rule R : this.Rules) {
			System.out.println((++i)+": "+R.escreverRegra());
		}
	}
	
	public HashSet<String> getLOE() {
		return this.AllLOE;
	}
	
	public List<Rule> getRules(){
		return this.Rules;
	}
}