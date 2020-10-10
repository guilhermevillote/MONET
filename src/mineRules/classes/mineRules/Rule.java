package mineRules;

import java.util.HashSet;
import java.util.List;

public class Rule {
	private double Support;
	private double Confidence;
	private HashSet<ItemChain> Antecedent;
	private HashSet<ItemChain> Consequent;
	private HashSet<String> LOE;
	
	public Rule(HashSet<ItemChain> Antecedente, HashSet<ItemChain> Consequente, double Confianca, double Suporte, HashSet<String> LOE) {
		this.Antecedent = Antecedente;
		this.Consequent = Consequente;
		this.Support = Suporte;
		this.Confidence = Confianca;
		this.LOE = LOE;
	}
	
	public String escreverRegra() {
		String EscritaTotal = ""+"\t";
		
		EscritaTotal += "Antecedent: ";
		EscritaTotal += escreverPrimaria(this.Antecedent);
		EscritaTotal = EscritaTotal+"\n\t";
		EscritaTotal += "Consequent: ";
		EscritaTotal += escreverPrimaria(this.Consequent);
		EscritaTotal += "\n\t"+"LOE: "+this.LOE;
		EscritaTotal += "\n\t#"+"Supp:"+this.Support;
		EscritaTotal += "\n\t#"+"Conf:"+this.Confidence;
		EscritaTotal += "\n\t#"+"LIC:"+(this.Antecedent.size()+this.Consequent.size());
		EscritaTotal +="\n";
		return EscritaTotal;
	}
	
	private String escreverPrimaria(HashSet<ItemChain> Set_ICs) {
		String EscritaParcial ="";
		List<String> Relacoes;
		int tam = 0, cont = 0, cont2 = 0, cont3 = 1;
		//For each ItemChain of the list
		for(ItemChain IC : Set_ICs) {
			EscritaParcial += "\n\t\t"+(cont3++)+": ";
			cont2++;
			Relacoes = IC.getRelations();
			tam = Relacoes.size();
			EscritaParcial=EscritaParcial+Relacoes.get(tam-1);
			for(int j = 1; j < tam; j++) {
				EscritaParcial=EscritaParcial+" ("+Relacoes.get(tam-1-j);
				cont++;
			}
			EscritaParcial = EscritaParcial+" ("+IC.getEndpoint()+")";
			for(int j = 0; j < cont; cont--)
				EscritaParcial=EscritaParcial+")";
			if(cont2 != Set_ICs.size()) {
				EscritaParcial=EscritaParcial+",";
			}
		}
		return EscritaParcial;
	}
	
	public HashSet<ItemChain> getAntecedent(){
		return this.Antecedent;
	}
	
	public HashSet<ItemChain> getConsequent(){
		return this.Consequent;
	}
	
	public HashSet<String> getLOE(){
		return this.LOE;
	}
	
}
