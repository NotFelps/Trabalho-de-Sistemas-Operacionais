package kernel;
import operacoes.Operacao;

public class PCB { //sugestão do prof : public class PCB implements Comparable<PCB>
	public enum Estado {NOVO, PRONTO, EXECUTANDO, ESPERANDO, TERMINADO};
	public int idProcesso; // primeiro processo criado deve ter id = 0
	public Estado estado = Estado.NOVO;
	public int[] registradores = new int[5];
	public int contadorDePrograma;
	public Operacao[] codigo;

	/*
	professor sugere colocar algo como : 

	//para o Round_robin precisa ter um algo como um contador que vai de 0 a 5, pois ele tem 5 ciclos, e após os 5 ciclos ele sai do processador


	int proxChuteTamBurstCPU = 4;
	int contadorBurst = 3;

	int remainingTime = 4; //deve começar no valor em que se inicia o proxChuteTamBurstCPU, e ir recebendo " -1 " a cada final de ciclo
	esse "remainingTime" será o tempo que levaremos em conta para definir qual o proximo processo que deve ser executado na cpu

	@Override
	public int compareTo(PCB outro) {
		if(this < outro)
			return -1;
		else if(this > outro)
			return 1;
		else
			return 0; 
	}
	*/

}
