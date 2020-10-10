package mineRules;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class WorkFlow {
	
	private String Dataset, Endpoint, NameOutputMRAR, PathOutputMRAR, NameOutputMRARE, PathOutputMRARE;
	private double Support, Confidence;
	private int MinLevel, MaxLevel;
	private boolean MRAR, MRARE, LOD, PREVIEW;
	private List<String> Labels, Namespaces, QueryPropertiesLabel;
	
	public WorkFlow(String config) throws Exception {
		Parameter param = new Parameter(config);
		this.Dataset = param.getDataset();
		this.Support = param.getSupport();
		this.Confidence = param.getConfidence();
		this.MinLevel = param.getMinLevel();
		this.MaxLevel = param.getMaxLevel();
		this.MRAR = param.isMRAR();
		this.MRARE = param.isMRARE();
		this.LOD = param.isLOD();
		this.PREVIEW = param.isPREVIEW();
		this.Endpoint = param.getEndpoint();
		this.NameOutputMRAR = param.getNameOutputMRAR();
		this.PathOutputMRAR = param.getPathOutputMRAR();
		this.NameOutputMRARE = param.getNameOutputMRARE();
		this.PathOutputMRARE = param.getPathOutputMRARE();
		this.QueryPropertiesLabel = param.getQueryPropertiesLabel();
		this.Labels = param.getLabels();
		this.Namespaces = param.getNamespaces();
	}
	
	public void start() throws IOException {
		if(MRAR || MRARE) {
			if(LOD == false)
				mine();
			else if(LOD == true)
				mine_lod();
		}
	}
	
	private void mine() throws IOException {
		HashMap<String, HashMap<String, HashSet<String>>> EntityInfo;
		Functions fx = new Functions();
		int GraphSize = 0;
		
		//Generating EntityInfo and GraphSize
		System.out.println("Serealizing Data..");
		EntityInfo = fx.constructEntityInfo(Dataset);
		GraphSize = fx.getGraphSize();
		System.out.println("Number of Nodes: "+GraphSize+"\n");
		
		if(MRAR && !MRARE) {
			//Unique STEP of MRAR
			System.out.println("MRAR: Initializing..");
			MRAR miner = new MRAR();
			miner.mine(EntityInfo, GraphSize, Support, Confidence, MinLevel, MaxLevel);
			List<Rule> Rules = miner.getRules();
			System.out.println("MRAR: "+Rules.size()+" Rules Found!\n");
			System.out.println("MRAR: Saving Rules..");
			fx.writeRules(this.PathOutputMRAR+"/"+this.NameOutputMRAR+".txt", Rules);
			System.out.println("MRAR: Rules Saved!");
		}
		else if(!MRAR && MRARE){
			//Unique STEP of MRARE
			System.out.println("MRARE: Initializing..");
			MRARE miner = new MRARE();
			miner.mine(EntityInfo, GraphSize, Support, MinLevel, MaxLevel);
			List<Rule> Rules = miner.getRules();
			System.out.println("MRARE: "+Rules.size()+" Rules Found!\n");
			System.out.println("MRARE: Saving Rules..");
			fx.writeRules(this.PathOutputMRARE+"/"+this.NameOutputMRARE+".txt", Rules);
			System.out.println("MRARE: Rules Saved!");
		}
		else if(MRAR && MRARE) {
			System.out.println("MRAR: Initializing..");
			MRAR miner = new MRAR();
			miner.mine(EntityInfo, GraphSize, Support, Confidence, MinLevel, MaxLevel);
			List<Rule> Rules = miner.getRules();
			System.out.println("MRAR: "+Rules.size()+" Rules Found!\n");
			System.out.println("MRAR: Saving Rules..");
			fx.writeRules(this.PathOutputMRAR+"/"+this.NameOutputMRAR+".txt", Rules);
			System.out.println("MRAR: Rules Saved!\n");
			
			//Unique STEP of MRARE
			System.out.println("MRARE: Initializing..");
			MRARE miner2 = new MRARE();
			miner2.mine(EntityInfo, GraphSize, Support, MinLevel, MaxLevel);
			Rules = miner2.getRules();
			System.out.println("MRARE: "+Rules.size()+" Rules Found!\n");
			System.out.println("MRARE: Saving Rules..");
			fx.writeRules(this.PathOutputMRARE+"/"+this.NameOutputMRARE+".txt", Rules);
			System.out.println("MRARE: Rules Saved!");
		};
	}
	
	private void mine_lod() throws IOException {
		Functions fx = new Functions();
		HashMap<String, HashMap<String, HashSet<String>>> oldEntityInfo;
		HashMap<String, HashMap<String, HashSet<String>>> externalEntityInfo;
		HashMap<String, HashMap<String, HashSet<String>>> newEntityInfo;
		int oldGraphSize, newGraphSize;
		double newSupport;
		
		//Generating EntityInfo and GraphSize
		System.out.println("Serealizing Data..");
		oldEntityInfo = fx.constructEntityInfo(Dataset);
		oldGraphSize = fx.getGraphSize();
		System.out.println("Number of Nodes: "+oldGraphSize+"\n");
		
		if(MRAR && !MRARE) {
			MRAR miner = new MRAR();

			//First Step of MRAR
			System.out.println("MRAR: First Step");
			System.out.println("MRAR: Initializing..");
			miner.mine(oldEntityInfo, oldGraphSize, Support, Confidence, MinLevel, MaxLevel);
			List<Rule> oldRules = miner.getRules();
			HashSet<String> LOE = miner.getLOE();
			HashMap<String, String> LOEMap = fx.getLOEMap(LOE, oldEntityInfo);
			System.out.println("MRAR: "+oldRules.size()+" Rules Found!\n");
			
			//PREVIEW
			if(this.PREVIEW) {
				fx.previewLOE(LOEMap);
			}
			
			//Querying at External Database
			System.out.println("Querying at External Database..");
			SPARQL query = new SPARQL();
			externalEntityInfo = query.runQuery(Endpoint, LOEMap, Labels, Namespaces, QueryPropertiesLabel);
			System.out.println("Done!\n");
			
			//Generating new EntityInfo and GraphSize and Support Value
			System.out.println("Extending Local Database...");
			newEntityInfo = fx.mergeEntityInfo(oldEntityInfo, externalEntityInfo);
			newGraphSize = fx.getGraphSize();
			System.out.println("New Number of Nodes: "+newGraphSize+"\n");
			System.out.println("Done!\n");
			
			//Second Step of MRAR
			System.out.println("MRAR: Second Step");
			System.out.println("MRAR: Initializing..");
			newSupport = (double) (oldGraphSize*Support/newGraphSize);
			miner.mine(newEntityInfo, newGraphSize, newSupport, Confidence, MinLevel, MaxLevel);
			List<Rule> newRules = miner.getRules();
			System.out.println("MRAR: "+newRules.size()+" Rules Found!\n");
			
			//Finding freshRules
			System.out.println("MRAR: Finding New Rules..");
			List<Rule> freshRules = fx.findFreshRules(oldRules, newRules);
			System.out.println("MRAR: "+freshRules.size()+" New Rules Found!\n");
			
			//Saving freshRules
			System.out.println("MRAR: Saving New Rules..");
			fx.writeRules(this.PathOutputMRAR+"/"+this.NameOutputMRAR+".txt", freshRules);
			System.out.println("MRAR: Rules Saved!");
		}
		else if(!MRAR && MRARE){
			MRARE miner = new MRARE();

			//First Step of MRAR
			System.out.println("MRARE: First Step");
			System.out.println("MRARE: Initializing..");
			miner.mine(oldEntityInfo, oldGraphSize, Support, MinLevel, MaxLevel);
			List<Rule> oldRules = miner.getRules();
			HashSet<String> LOE = miner.getLOE();
			HashMap<String, String> LOEMap = fx.getLOEMap(LOE, oldEntityInfo);
			System.out.println("MRARE: "+oldRules.size()+" Rules Found!\n");
			
			//PREVIEW
			if(this.PREVIEW) {
				fx.previewLOE(LOEMap);
			}
			
			//Querying at External Database
			System.out.println("Querying at External Database..");
			SPARQL query = new SPARQL();
			externalEntityInfo = query.runQuery(Endpoint, LOEMap, Labels, Namespaces, QueryPropertiesLabel);
			System.out.println("Done!\n");
			
			//Generating new EntityInfo and GraphSize and Support Value
			System.out.println("Extending Local Database...");
			newEntityInfo = fx.mergeEntityInfo(oldEntityInfo, externalEntityInfo);
			newGraphSize = fx.getGraphSize();
			System.out.println("New Number of Nodes: "+newGraphSize+"\n");
			System.out.println("Done!\n");
			
			//Second Step of MRAR
			System.out.println("MRARE: Second Step");
			System.out.println("MRARE: Initializing..");
			newSupport = (float) (oldGraphSize*Support/newGraphSize);
			miner.mine(newEntityInfo, newGraphSize, newSupport, MinLevel, MaxLevel);
			List<Rule> newRules = miner.getRules();
			System.out.println("MRARE: "+newRules.size()+" Rules Found!\n");
			
			//Finding freshRules
			System.out.println("MRARE: Finding New Rules..");
			List<Rule> freshRules = fx.findFreshRules(oldRules, newRules);
			System.out.println("MRARE: "+freshRules.size()+" New Rules Found!\n");
			
			//Saving freshRules
			System.out.println("MRARE: Saving Rules..");
			fx.writeRules(this.PathOutputMRARE+"/"+this.NameOutputMRARE+".txt", freshRules);
			System.out.println("MRARE: Rules Saved!");
		}
		else if(MRAR && MRARE) {
			MRAR miner = new MRAR();
			MRARE miner2 = new MRARE();

			//First Step of MRAR
			System.out.println("MRAR: First Step");
			System.out.println("MRAR: Initializing..");
			miner.mine(oldEntityInfo, oldGraphSize, Support, Confidence, MinLevel, MaxLevel);
			List<Rule> oldRules = miner.getRules();
			HashSet<String> LOE = miner.getLOE();
			System.out.println("MRAR: "+oldRules.size()+" Rules Found!\n");
			
			//First Step of MRARARE
			System.out.println("MRARE: First Step");
			System.out.println("MRARE: Initializing..");
			miner2.mine(oldEntityInfo, oldGraphSize, Support, MinLevel, MaxLevel);
			List<Rule> oldRules2 = miner2.getRules();
			HashSet<String> LOE2 = miner2.getLOE();
			System.out.println("MRARE: "+oldRules2.size()+" Rules Found!\n");
			
			HashSet<String> LOE3 = new HashSet<String>();
			LOE3.addAll(LOE);
			LOE3.addAll(LOE2);
			HashMap<String, String> LOEMap = fx.getLOEMap(LOE3, oldEntityInfo);
			
			//PREVIEW
			if(this.PREVIEW) {
				fx.previewLOE(LOEMap);
			}
			
			//Querying at External Database
			System.out.println("Querying at External Database..");
			
			SPARQL query = new SPARQL();

			externalEntityInfo = query.runQuery(Endpoint, LOEMap, Labels, Namespaces, QueryPropertiesLabel);
			System.out.println("Done!\n");
			 
			//Generating new EntityInfo and GraphSize and Support Value
			System.out.println("Extending Local Database...");
			newEntityInfo = fx.mergeEntityInfo(oldEntityInfo, externalEntityInfo);
			newGraphSize = fx.getGraphSize();
			System.out.println("New Number of Nodes: "+newGraphSize+"\n");
			System.out.println("Done!\n");
			
			//New Support
			newSupport = (float) (oldGraphSize*Support/newGraphSize);
			
			//Second Step of MRAR
			System.out.println("MRAR: Second Step");
			System.out.println("MRAR: Initializing..");
			miner.mine(newEntityInfo, newGraphSize, newSupport, Confidence, MinLevel, MaxLevel);
			List<Rule> newRules = miner.getRules();
			System.out.println("MRAR: "+newRules.size()+" Rules Found!\n");
			
			//Finding freshRules
			System.out.println("MRAR: Finding New Rules..");
			List<Rule> freshRules = fx.findFreshRules(oldRules, newRules);
			System.out.println("MRAR: "+freshRules.size()+" New Rules Found!\n");
			
			//Saving freshRules MRAR
			System.out.println("MRAR: Saving Rules..");
			fx.writeRules(this.PathOutputMRAR+"/"+this.NameOutputMRAR+".txt", freshRules);
			System.out.println("MRAR: Rules Saved!");
			
			//Second Step of MRARE
			System.out.println("MRARE: Second Step");
			System.out.println("MRARE: Initializing..");
			miner2.mine(newEntityInfo, newGraphSize, newSupport, MinLevel, MaxLevel);
			List<Rule> newRules2 = miner2.getRules();
			System.out.println("MRARE: "+newRules2.size()+" Rules Found!\n");
			
			System.out.println("MRARE: Finding New Rules..");
			List<Rule> freshRules2 = fx.findFreshRules(oldRules2, newRules2);
			System.out.println("MRARE: "+freshRules2.size()+" New Rules Found!\n");
			
			//Saving freshRules
			System.out.println("MRARE: Saving Rules..");
			fx.writeRules(this.PathOutputMRARE+"/"+this.NameOutputMRARE+".txt", freshRules2);
			System.out.println("MRARE: Rules Saved!");
		};	
	}
}
