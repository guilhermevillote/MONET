package mineRules;

import java.util.HashSet;

public class LargeItemChain {

	private HashSet<ItemChain> Set_ICs;
	
	public LargeItemChain(HashSet<ItemChain> Set_ICs, int LOClength, double support) {
		this.Set_ICs = Set_ICs;
	}
	
	public LargeItemChain(ItemChain IC1, ItemChain IC2) {
		this.Set_ICs = new HashSet<ItemChain>();
		this.Set_ICs.add(IC1);
		this.Set_ICs.add(IC2);
	}
	
	public void addItemChain(ItemChain IC) {
		this.Set_ICs.add(IC);
	}
	
	public HashSet<ItemChain> getSetICs() {
		return this.Set_ICs;
	}	
	
}
