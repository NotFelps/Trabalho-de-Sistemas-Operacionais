package kernel;
import operacoes.Operacao;

public class PCB implements Comparable<PCB> { //sugestão do prof : public class PCB implements Comparable<PCB>
	public enum Estado {NOVO, PRONTO, EXECUTANDO, ESPERANDO, TERMINADO};
	public int idProcesso; // primeiro processo criado deve ter id = 0
	public Estado estado = Estado.NOVO;
	public int[] registradores = new int[5];
	public int contadorDePrograma;     //começa em 0
	public Operacao[] codigo;

	int cicloPronto = 0;
	int cicloTerminado = 0;;
	int tempoRetorno = 0;    //tempos desde que o PCB fica pronto ate acabar
	int tempoResposta = 0;   //tempo que o PCB fica na lista de prontos ate ir pra CPU pela primeira vez
	int tempoEspera = 0;     //soma dos tempos em que o PCB fica na lista de prontos
	int jaFoiCPU = 0;      // 0 = nunca foi na CPU ; 1 = ja foi na CPU

	/*	
	professor sugere colocar algo como : 

	//para o Round_robin precisa ter um algo como um contador que vai de 0 a 5, pois ele tem 5 ciclos, e após os 5 ciclos ele sai do processador
	*/

	int proxChuteTamBurstCPU = 5;      //vou sempre inicializar chutando um burst de 3 ciclos
	int contadorBurst = 0;    //inicializa no 0 e vai sendo adicionado em +1 a cada ciclo em que o processo passa na cpu, e é zerado quando o processo sai da CPU

	int remainingTime = 4; //deve começar no valor em que se inicia o proxChuteTamBurstCPU, e ir recebendo " -1 " a cada final de ciclo
	/*
	esse "remainingTime" será o tempo que levaremos em conta para definir qual o proximo processo que deve ser executado na cpu
	*/
	@Override
	public int compareTo(PCB outro) {
		if(this.proxChuteTamBurstCPU < outro.proxChuteTamBurstCPU) {
			return -1;
		} else if(this.proxChuteTamBurstCPU > outro.proxChuteTamBurstCPU) {
			return 1;
		} else {   //nesse caso ambos os PCBs tem o mesmo tamanho de chute, entao devemos ordenar pelo que possui o menor idProcessos
			if(this.idProcesso < outro.idProcesso) 
				return -1;
			else if(this.idProcesso > outro.idProcesso)
				return 1;
		}
		return 0;	 
	}
	

}
