package mineRules;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;

public class Functions {
	
    HashSet<String> GraphNodes;
	
	public Functions() {
		GraphNodes = new HashSet<String>();
	}
	
	public int getGraphSize() {
		return GraphNodes.size();
	}
	
	
	public HashMap<String, HashMap<String, HashSet<String>>> constructEntityInfo(String DS) {
		Model model = ModelFactory.createDefaultModel();
		InputStream in = FileManager.get().open( DS );
	    if (in == null) {
	    	throw new IllegalArgumentException("File: " + DS + " not found");
	    }
	    model.read(in, null, "N-TRIPLES");

	    HashMap<String, HashMap<String, HashSet<String>>> EntityInfo = new HashMap<String, HashMap<String, HashSet<String>>>(); 
		StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();         // get next sStatement
            String subject = stmt.getSubject().toString();
            String predicate = stmt.getPredicate().toString(); // get the predicate
            String object = stmt.getObject().toString();    // get the object
            if(!EntityInfo.containsKey(object)) {
            	this.GraphNodes.add(subject);
            	this.GraphNodes.add(object);
            	HashMap<String, HashSet<String>> predicate_map = new HashMap<String, HashSet<String>>();
            	HashSet<String> subject_list = new HashSet<String>();
            	subject_list.add(subject);
            	predicate_map.put(predicate, subject_list);
            	EntityInfo.put(object, predicate_map);
            }
            else {
            	if(!EntityInfo.get(object).containsKey(predicate)) {
            		HashMap<String, HashSet<String>> predicate_map = new HashMap<String, HashSet<String>>();
                	HashSet<String> subject_list = new HashSet<String>();
                	subject_list.add(subject);
                	predicate_map.put(predicate, subject_list);
                	EntityInfo.get(object).putAll(predicate_map);
                	this.GraphNodes.add(subject);
            	}
            	else {
            		if(!EntityInfo.get(object).get(predicate).contains(subject)) {
            			EntityInfo.get(object).get(predicate).add(subject);
            			this.GraphNodes.add(subject);
            		}
            		else
            			continue;
            	}
            }
            
        };
        model.close();
        try {
        	in.close();
        } catch (Exception ex) {
            System.out.println("Error closing the file: "+DS);
        }
        return EntityInfo;
	}
	
	public HashMap<String, HashMap<String, HashSet<String>>> mergeEntityInfo(
				HashMap<String, HashMap<String, HashSet<String>>> oldEntityInfo, 
				HashMap<String, HashMap<String, HashSet<String>>> newEntityInfo){
		HashMap<String, HashMap<String, HashSet<String>>> extendedEntityInfo = new HashMap<String, HashMap<String, HashSet<String>>>();
		extendedEntityInfo.putAll(oldEntityInfo);
		//System.out.println(newEntityInfo);
		for(String Object : newEntityInfo.keySet()) {
			if(!extendedEntityInfo.containsKey(Object)) {
				for(String Predicate : newEntityInfo.get(Object).keySet()) {
					HashMap<String, HashSet<String>> predicate_map = new HashMap<String, HashSet<String>>();
	            	HashSet<String> subject_list = newEntityInfo.get(Object).get(Predicate);
	            	predicate_map.put(Predicate, subject_list);
	            	extendedEntityInfo.put(Object, predicate_map);
	            	this.GraphNodes.add(Object);
	            	this.GraphNodes.addAll(subject_list);
				}
			}
			else {
				for(String Predicate : newEntityInfo.get(Object).keySet()) {
	            	if(!extendedEntityInfo.get(Object).containsKey(Predicate)) {
	            		HashMap<String, HashSet<String>> predicate_map = new HashMap<String, HashSet<String>>();
		            	HashSet<String> subject_list = newEntityInfo.get(Object).get(Predicate);
		            	predicate_map.put(Predicate, subject_list);
		            	extendedEntityInfo.get(Object).putAll(predicate_map);
		            	this.GraphNodes.addAll(subject_list);

	            	}
	            	else {       		
	            		if( !extendedEntityInfo.get(Object).get(Predicate).containsAll(newEntityInfo.get(Object).get(Predicate)) ) {
	            			extendedEntityInfo.get(Object).get(Predicate).addAll(newEntityInfo.get(Object).get(Predicate));
	            			this.GraphNodes.addAll(newEntityInfo.get(Object).get(Predicate));
	            		}
	            		else
	            			continue;
	            	}
				}
            }
		}
		return extendedEntityInfo;
	}
	
	public List<Rule> findFreshRules(List<Rule> oldRules, List<Rule> newRules){
		List<Rule> freshRules = new ArrayList<>();
		boolean stats = false;
		for(Rule newRule : newRules) {
			stats = false;
			for(Rule oldRule : oldRules) 
				if(equalRules(newRule, oldRule)) {
					stats = true;
					break;
				}
			if(!stats)
				freshRules.add(newRule);
		}
		return freshRules;
	}
	
	private boolean equalRules(Rule Rule1, Rule Rule2) {
		boolean stats;
		
		if(Rule1.getLOE().size() != Rule2.getLOE().size())
			return false;
		else if(!Rule1.getLOE().containsAll(Rule2.getLOE()) || !Rule2.getLOE().containsAll(Rule1.getLOE()) )
			return false;
		
		for(ItemChain IC1 : Rule1.getAntecedent()) {
			stats = false;
			for(ItemChain IC2 : Rule2.getAntecedent())
				if(equalICs(IC1, IC2)) {
					stats = true;
					break;
				}
			if(!stats)
				return false;
		}
		for(ItemChain IC1 : Rule1.getConsequent()) {
			stats = false;
			for(ItemChain IC2 : Rule2.getConsequent())
				if(equalICs(IC1, IC2)) {
					stats = true;
					break;
				}
			if(!stats)
				return false;
		}
	
		return true;
	}
	
	private boolean equalICs(ItemChain IC1, ItemChain IC2) {
		if(IC1.getEndpoint().equals(IC2.getEndpoint()))
			if(IC1.getRelations().containsAll(IC2.getRelations()) && IC2.getRelations().containsAll(IC1.getRelations()))	
				return true;

		return false;
	}
	
	public void writeRules(String fileName, List<Rule> Rules) throws IOException {
		int i = 0;
		BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
		for(Rule R : Rules) {
			writer.write("Rule "+(++i)+": "+"\n"+R.escreverRegra()+"\n");
		}
		writer.close();
	}
	
	public boolean readInput(Scanner scan) {
			String s = scan.next();
			if(s.equals("Y") || s.equals("y"))
				return true;
			else if(s.equals("N") || s.equals("n"))
				return false;
			else {
				System.out.println("Please, insert Y/N");
				return readInput(scan);
			}
		
	}
	
	/*public HashSet<String> getLOEEndpoint(HashSet<String> LOE, HashMap<String, HashMap<String, HashSet<String>>> EntityInfo){
		HashSet<String> LOEEndpoint = new HashSet<>();
		for(String EndpointEntity : EntityInfo.keySet()) {
			for(String relation : EntityInfo.get(EndpointEntity).keySet()) {
				//System.out.println(relation);
				if(relation.equals("http://www.w3.org/2002/07/owl#sameAs")) {
					//System.out.println("Endpoint Entity: "+EndpointEntity);
					HashSet<String> Entity_Var = EntityInfo.get(EndpointEntity).get(relation);
					//System.out.println("Entity_Var: "+Entity_Var);
					if(LOE.containsAll(Entity_Var))
						LOEEndpoint.add(EndpointEntity);
				}
			}
		}
		return LOEEndpoint;
	}*/
	
	public HashMap<String, String> getLOEMap(HashSet<String> LOE, HashMap<String, HashMap<String, HashSet<String>>> EntityInfo){
		HashMap<String, String> LOEMap = new HashMap<>();
		for(String EndpointEntity : EntityInfo.keySet()) {
			for(String relation : EntityInfo.get(EndpointEntity).keySet()) {
				if(relation.equals("http://www.w3.org/2002/07/owl#sameAs")) {
					String Entity_Var = EntityInfo.get(EndpointEntity).get(relation).iterator().next();
					for(String LOEEntity : LOE) {
						if(LOEEntity.equals(Entity_Var)) {
							LOEMap.put(EndpointEntity, Entity_Var);
						}
					}
				}
			}
		}
		return LOEMap;
	}
	
	public HashSet<String> getLinkedEntities(HashMap<String, HashMap<String, HashSet<String>>> EntityInfo){
		HashSet<String> LOE = new HashSet<>();
		for(String EndpointEntity : EntityInfo.keySet()) {
			for(String relation : EntityInfo.get(EndpointEntity).keySet()) {
				//System.out.println(relation);
				if(relation.equals("http://www.w3.org/2002/07/owl#sameAs")) {
					LOE.add(EndpointEntity);
				}
			}
		}
		return LOE;
	}
	
	public void previewLOE(HashMap<String, String> LOE){
		Scanner scan = new Scanner(System.in);
		System.out.println("Number of LOE: "+LOE.size());
		System.out.println("Do you want to see the entities? (Y/N)");
		if(readInput(scan)) {
			System.out.println("LOE: "+LOE.keySet());
		}
		System.out.println("Do you want to continue? (Y/N)");
		if(!readInput(scan)) {
			System.out.println("Process Ended!");
			System.exit(0);
		}
		scan.close();
	}
}
