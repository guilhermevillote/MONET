package mineRules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.jena.query.ParameterizedSparqlString;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class SPARQL {
	
	public SPARQL() {
	}
	
	public HashMap<String, HashMap<String, HashSet<String>>> runQuery(String Endpoint, HashMap<String, String> LOE, List<String> Labels, List<String> Namespaces, List<String> QueryPropertiesLabel) {
		String DefaultQueryString = getDefaultQueryString(Labels, Namespaces);
		HashMap<String, HashMap<String, HashSet<String>>> newEntityInfo = getNewEntityInfo(Endpoint, LOE, DefaultQueryString, QueryPropertiesLabel);
		return newEntityInfo;
		
	}
	
	private String getDefaultQueryString(List<String> Labels, List<String> Namespaces) {
		String DefaultQueryString = "";
		for(int i = 0; i < Labels.size(); i++) {
			DefaultQueryString += "PREFIX ";
			DefaultQueryString += Labels.get(i);
			DefaultQueryString += ": <";
			DefaultQueryString += Namespaces.get(i);
			DefaultQueryString += ">\n";
		}
		DefaultQueryString += "SELECT DISTINCT ?o\n";
		DefaultQueryString += "WHERE {\n";
		DefaultQueryString += "?x ?y ?o.\n}";
		return DefaultQueryString;
	}

	private HashMap<String, HashMap<String, HashSet<String>>> getNewEntityInfo(String Endpoint, HashMap<String, String> LOE, String DefaultQueryString, List<String> QueryPropertiesLabel) {
		HashMap<String, HashMap<String, HashSet<String>>> newEntityInfo = new HashMap<String, HashMap<String, HashSet<String>>>();
		ParameterizedSparqlString qs = new ParameterizedSparqlString();
		for(String Predicate : QueryPropertiesLabel) {
			for(String Subject : LOE.keySet()) {
				String QueryString = DefaultQueryString.replace("?x", "<"+Subject+">").replace("?y", Predicate);
				//System.out.println(QueryString);
				qs.setCommandText(QueryString);
				QueryExecution exec = QueryExecutionFactory.sparqlService(Endpoint, qs.asQuery());
				try{
					ResultSet results = exec.execSelect();
				    while(results.hasNext()){
				    	QuerySolution soln = results.nextSolution() ;
				    	String Object = (soln.get("o")).toString();
				    	//System.out.println(Predicate+" "+Object);
				    	if(!newEntityInfo.containsKey(Object)) {
			            	HashMap<String, HashSet<String>> predicate_map = new HashMap<String, HashSet<String>>();
			            	HashSet<String> subject_list = new HashSet<String>();
		                	subject_list.add(LOE.get(Subject));
		                	predicate_map.put(Predicate, subject_list);
		                	//System.out.println(predicate_map);
		                	newEntityInfo.put(Object, predicate_map);
				    	}
			            else {
			            	if(!newEntityInfo.get(Object).containsKey(Predicate)) {
			            		HashMap<String, HashSet<String>> predicate_map = new HashMap<String, HashSet<String>>();
			                	HashSet<String> subject_list = new HashSet<String>();
			                	subject_list.add(LOE.get(Subject));
			                	predicate_map.put(Predicate, subject_list);
			                	newEntityInfo.get(Object).putAll(predicate_map);
			            	}
					    	else {
			            		if(!newEntityInfo.get(Object).get(Predicate).contains(LOE.get(Subject))) {
			            			newEntityInfo.get(Object).get(Predicate).add(LOE.get(Subject));
			            		}
			            		else
			            			continue;
					    	}
			            }
				    }
				}
				finally{
		            exec.close();
				}	
			}
		}
		//System.out.println(newEntityInfo);
		return newEntityInfo;
	}
}
