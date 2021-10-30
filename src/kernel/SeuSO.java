package kernel;
import java.util.HashMap;
import java.util.List;


import operacoes.Operacao;
import operacoes.OperacaoES;

public class SeuSO extends SO {

	PCB pcbnovo;
	//HashMap<Integer, OperacaoES[]> listaES = new HashMap<Integer, OperacaoES[]>();
	int criaIdProcesso = 0;    //usado para criar o id de processo na ordem certa na função "criaProcesso"

	@Override
	// ATENCÃO: cria o processo mas o mesmo 
	// só estará "pronto" no próximo ciclo
	protected void criaProcesso(Operacao[] codigo) {
		PCB pcb = new PCB();
		pcb.idProcesso = criaIdProcesso;
		criaIdProcesso++;
		pcb.estado = PCB.Estado.NOVO;
		pcb.contadorDePrograma = 0;  //número de operações finalizadas
		pcb.codigo = codigo;
		pcbnovo = pcb;
	}

	@Override
	protected void trocaContexto(PCB pcbAtual, PCB pcbProximo) {
		
	}

	@Override
	// Assuma que 0 <= idDispositivo <= 4
	protected OperacaoES proximaOperacaoES(int idDispositivo) { //recebe como parametro o dispositivo e retorna qual a proxima operacao a ser realizada no mesmo, por isso eh necessario guardar uma estrutura com cada uma das operacoes de cada um dos dispositivos
		
		return null;
	}

	@Override
	protected Operacao proximaOperacaoCPU() {
		
		return null;
	}

	@Override
	protected void executaCicloKernel() {   //complexo
		
	}

	@Override
	protected boolean temTarefasPendentes() {
		
		return false;
	}

	@Override
	protected Integer idProcessoNovo() {
		return pcbnovo.idProcesso;
	}

	@Override
	protected List<Integer> idProcessosProntos() {
		
		return null;
	}

	@Override
	protected Integer idProcessoExecutando() {
		
		return null;
	}

	@Override
	protected List<Integer> idProcessosEsperando() {
		
		return null;
	}

	@Override
	protected List<Integer> idProcessosTerminados() {
		
		return null;
	}


	//////////////////////////////////////////////////////////

	@Override
	protected int tempoEsperaMedio() {
		
		return 0;
	}

	@Override
	protected int tempoRespostaMedio() {
		
		return 0;
	}

	@Override
	protected int tempoRetornoMedio() {
		
		return 0;
	}
	
	@Override
	protected int trocasContexto() {
		
		return 0;
	}

	//////////////////////////////////////////////////////////

	@Override
	public void defineEscalonador(Escalonador e) {
		
		switch (e) {
			case  FIRST_COME_FIRST_SERVED:
			break;

			case  SHORTEST_JOB_FIRST:
			break;

			case SHORTEST_REMANING_TIME_FIRST :
			break;
			
			case ROUND_ROBIN_QUANTUM_5 :
			break;
		}
	}
}
