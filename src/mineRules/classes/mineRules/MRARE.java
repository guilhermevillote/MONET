package mineRules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


//Mining Strong Rare Rules
public class MRARE {
	
	int GraphSize, chainID;
	int minLevel, maxLevel;
	double minSup;
	HashSet<String> AllLOE;
	List<Rule> Rules;
	HashMap<String, HashMap<String, HashSet<String>>> EntityInfo;
    List<ItemChain> RareItemChain;
    List<ItemChain> FrequentItemChain;
    HashMap<ItemChain, LargeItemChain> AllLICs;

	public MRARE() {
	}
	
	void mine(HashMap<String, HashMap<String, HashSet<String>>> EntityInfo, int GraphSize, double minSup, int minLevel, int maxLevel) {
		this.AllLOE = null;
		this.AllLOE = new HashSet<>();
		this.Rules = null;
		this.RareItemChain = null;
		this.RareItemChain = new ArrayList<>();
		this.FrequentItemChain = null;
		this.FrequentItemChain = new ArrayList<>();
		this.chainID = 0;
		this.minSup = minSup;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		this.GraphSize = GraphSize;
		this.EntityInfo = EntityInfo;
		
		System.out.println("MRARE: Generating Itemchains..");
		for(String EndpointEntity : this.EntityInfo.keySet()) {
			for(String relation : this.EntityInfo.get(EndpointEntity).keySet()) {
				List<String> relations = new ArrayList<>();
				relations.add(relation);
				generateItemChains(EndpointEntity, relations, (this.EntityInfo.get(EndpointEntity)).get(relation), 1);
			}
		}
	
		//Gerar ItemChains Largos
		System.out.println("MRARE: Generating L Large ItemChains..");
		this.AllLICs = generateAllLargeRareItemChains(this.RareItemChain, this.FrequentItemChain);
		
		//Gerar Regras Raras
		System.out.println("MRARE: Generating Rare Rules..");
		this.Rules = generateRareRules();
		
		System.out.println("MRARE: Mining Operation Ended!");
		
	}
	
	private void generateItemChains(String EndpointEntity, List<String> Relations_Parameter, HashSet<String> Entities_Parameter, int Level) {
		if(Level >= this.minLevel && Level <=this.maxLevel && Entities_Parameter.size() > 1) { //MAIS QUE UMA ENTIDADE
			double Support = ((double)Entities_Parameter.size())/((double)this.GraphSize);
			if(Support >= this.minSup) {
				this.chainID++;
				this.FrequentItemChain.add(new ItemChain(this.chainID, Entities_Parameter, EndpointEntity, Relations_Parameter, Support));
			}
			else {
				this.chainID++;
				this.RareItemChain.add(new ItemChain(this.chainID, Entities_Parameter, EndpointEntity, Relations_Parameter, Support));
			}
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
	
	
	private HashMap<ItemChain, LargeItemChain> generateAllLargeRareItemChains(List<ItemChain> RareLIC, List<ItemChain> FrequentLIC) {
		HashMap<ItemChain, LargeItemChain> AllLICs = new HashMap<ItemChain, LargeItemChain>();
		FrequentLIC.addAll(RareLIC);
		HashSet<String> LOE = new HashSet<String>();
		for(ItemChain RareIC : RareLIC) {
			for(ItemChain IC: FrequentLIC) {
				if(RareIC != IC) {
					LOE.clear();
					LOE.addAll(RareIC.getEntities());
					LOE.retainAll(IC.getEntities());
					if(LOE.size() == RareIC.getNumberOfEntities()) {
					//double Support = ((double)LOE.size())/((double)this.GraphSize);
					//if( Support == RareIC.getSuport()) {
						if(!AllLICs.containsKey(RareIC)) {
							AllLICs.put(RareIC, new LargeItemChain(RareIC, IC));
						}
						else {
							AllLICs.get(RareIC).addItemChain(IC);
						}
					}
				}

			}
		}
		return AllLICs;
	}

	private List<Rule> generateRareRules() {
		Rules = new ArrayList<Rule>();
		for(ItemChain GenIC : this.AllLICs.keySet()) {
			HashSet<ItemChain> Antecedent = new HashSet<>();
			Antecedent.add(GenIC);
			HashSet<ItemChain> Consequent = new HashSet<>();
			Consequent.addAll(this.AllLICs.get(GenIC).getSetICs());
			Consequent.removeAll(Antecedent);
			this.Rules.add(new Rule(Antecedent, Consequent, 1.0, GenIC.getSuport(), GenIC.getEntities()));
			this.AllLOE.addAll(GenIC.getEntities());
		}
	    return Rules;
	}
	
	public HashSet<String> getLOE() {
		return this.AllLOE;
	}
	
	public List<Rule> getRules(){
		return this.Rules;
	}
	
}
