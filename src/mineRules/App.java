package mineRules;

public class App {
	
    //static String file =  "C:\\PFC\\config_futebol_monet_mrar.xml";
    
	public static void main (String args[]) throws Exception{
		
		//DURANTE A ESCRITA DO CODIGO
		//WorkFlow workFlow = new WorkFlow(file);
		//workFlow.start();
			
		//APOS COMPILADO EM ARQUIVO JAR
		//PARA DAR COMO ENTRADA O PATH DO ARQUIVO POR LINHA DE COMANDO
		String config = args[0];
		WorkFlow workFlow = new WorkFlow(config);
		workFlow.start();
	}
}
	//COMANDOS PARA SABER O TEMPO DE EXECUCAO
	//long start = System.currentTimeMillis();
	//long end = System.currentTimeMillis();
	//System.out.println("Time: "+(end-start)/1000F);

	//ESCREVER EM RDF
    //miner1.modelo.write(System.out, "N-TRIPLES");