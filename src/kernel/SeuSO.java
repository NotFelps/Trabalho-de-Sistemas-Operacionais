package kernel;
import java.util.*;

import kernel.PCB.Estado;
import operacoes.Operacao;
import operacoes.OperacaoES;
import escalonadores.*;

public class SeuSO extends SO {

	int trocasContexto;
	PCB pcbnovo, pcbaux;
	//HashMap<Integer, OperacaoES[]> listaES = new HashMap<Integer, OperacaoES[]>();
	int criaIdProcesso = 0;    //usado para criar o id de processo na ordem certa na função "criaProcesso"

	List<PCB> prontos = new ArrayList<PCB>();     //processos prontos para serem executados
	List<PCB> esperando = new ArrayList<PCB>();    //processos esperando operacaoES
	List<PCB> terminados = new ArrayList<PCB>();    //processos terminados
	
	PCB executandoCPU;

	//////////////////////////////////////////////////////////////////////

	public void verificaEsperando() {
        for(PCB p : esperando) {
			OperacaoES aux = (OperacaoES) p.codigo[p.contadorDePrograma];
            if(aux.ciclos <= 0) {
				if(p.codigo[p.contadorDePrograma+1] instanceof OperacaoES) {
					int aux = 
				}
			}
        }
    }

	//////////////////////////////////////////////////////////////////////


	//criar uma linkedlist pra cada dispositivo
	Queue<OperacaoES> listaD0 = new LinkedList<>();    //fila dispositivo 0
	Queue<OperacaoES> listaD1 = new LinkedList<>();	  //fila dispositivo 1
	Queue<OperacaoES> listaD2 = new LinkedList<>();    //fila dispositivo 2
	Queue<OperacaoES> listaD3 = new LinkedList<>();    //fila dispositivo 3
	Queue<OperacaoES> listaD4 = new LinkedList<>();    //fila dispositivo 4

	//criar uma variavel auxiliar do tipo OperacaoES para cada dispositivo
	OperacaoES opatualES_D0;
	OperacaoES opatualES_D1;
	OperacaoES opatualES_D2;
	OperacaoES opatualES_D3;
	OperacaoES opatualES_D4;

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
		executandoCPU = pcbProximo;
		processador.registradores = pcbProximo.registradores;
		prontos.add(pcbAtual); //OLHAR ESSA PARTE COM MAIS ATENCAO
		trocasContexto++;
	}

	@Override
	// Assuma que 0 <= idDispositivo <= 4
	protected OperacaoES proximaOperacaoES(int idDispositivo) { //recebe como parametro o dispositivo e retorna qual a proxima operacao a ser realizada no mesmo, por isso eh necessario guardar uma estrutura com cada uma das operacoes de cada um dos dispositivos

		switch(idDispositivo) {

			case 0 :
				if(opatualES_D0 != null) {
					if(opatualES_D0.ciclos > 0) {
						return opatualES_D0;
					} else {
						opatualES_D0 = listaD0.poll();
						if(opatualES_D0 != null) return opatualES_D0;
					}
				} else if(listaD0 != null) {
					opatualES_D0 = listaD0.poll();
					return opatualES_D0;	
				}	
				break;

			case 1 :
				if(opatualES_D1 != null) {
					if(opatualES_D1.ciclos > 0) {
						return opatualES_D1;
					} else {
						opatualES_D1 = listaD1.poll();
						if(opatualES_D1 != null) return opatualES_D1;
					}
				} else if(listaD1 != null) {
					opatualES_D1 = listaD1.poll();
					return opatualES_D1;
				}
				break;

			case 2 :
				if(opatualES_D2 != null) {
					if(opatualES_D2.ciclos > 0) {
						return opatualES_D2;
					} else {
						opatualES_D2 = listaD2.poll();
						if(opatualES_D2 != null) return opatualES_D2;
					}
				} else if(listaD2 != null) {
					opatualES_D2 = listaD2.poll();
					return opatualES_D2;
				}
				break;

			case 3 :
				if(opatualES_D3 != null) {
					if(opatualES_D3.ciclos > 0) {
						return opatualES_D3;
					} else {
						opatualES_D3 = listaD3.poll();
						if(opatualES_D3 != null) return opatualES_D3;
					}
				} else if(listaD3 != null) {
					opatualES_D3 = listaD3.poll();
					return opatualES_D3;
				}
				break;

			case 4 :
			if(opatualES_D4 != null) {
				if(opatualES_D4.ciclos > 0) {
					return opatualES_D4;
				} else {
					opatualES_D4 = listaD4.poll();
					if(opatualES_D4 != null) return opatualES_D4;
				}
			} else if(listaD4 != null) {
				opatualES_D4 = listaD4.poll();
				return opatualES_D4;
			}
				break;
					
	    }
		return null;
	}

	@Override
	protected Operacao proximaOperacaoCPU() {	
		return executandoCPU.codigo[executandoCPU.contadorDePrograma];
	}

	@Override
	protected void executaCicloKernel() {   //complexo
		//Verificar processos criados e colocar em seu devido estado/fila	
		if(pcbaux != null)	{
			pcbaux.estado = Estado.PRONTO;
			prontos.add(pcbaux);
			pcbaux = null;
		}

		if(prontos.get(0) != null) {

			//se o indice no contador de programa for uma operacao de ES
			if(prontos.get(0).codigo[prontos.get(0).contadorDePrograma] instanceof OperacaoES) {
				OperacaoES aux = (OperacaoES) prontos.get(0).codigo[prontos.get(0).contadorDePrograma];
				int aux2 = aux.idDispositivo;   //aux 2 eh o dispositivo da operacaoES
				switch (aux2) {
					case 0 :
						listaD0.add(aux);
						esperando.add(prontos.get(0));
						prontos.remove(prontos.get(0));
					break;

					case 1 :
						listaD1.add(aux);
						esperando.add(prontos.get(0));
						prontos.remove(prontos.get(0));
					break;

					case 2 :
						listaD2.add(aux);
						esperando.add(prontos.get(0));
						prontos.remove(prontos.get(0));
					break;

					case 3 :
						listaD3.add(aux);
						esperando.add(prontos.get(0));
						prontos.remove(prontos.get(0));
					break;

					case 4 :
						listaD4.add(aux);
						esperando.add(prontos.get(0));
						prontos.remove(prontos.get(0));
					break;
				}
			} else {
				executandoCPU = prontos.get(0);
				prontos.remove(prontos.get(0));
			}
		}

		//AGORA TEMOS QUE CHAMAR O ESCALONADOR EM QUESTAO PARA ORGANIZAR A FILA DE PRONTOS

		pcbaux = pcbnovo;
		pcbnovo = null;
	}

	@Override
	protected boolean temTarefasPendentes() {   //true se ainda tem processos que nao estao prontos
		return (numeroProcessos != terminados.size());
	}

	@Override
	protected Integer idProcessoNovo() {
		return pcbnovo.idProcesso;
	}

	@Override
	protected List<Integer> idProcessosProntos() {
		List<Integer> idProntos = new ArrayList<>();
		for (PCB aux : prontos) {
			idProntos.add(aux.idProcesso);
		}
		Collections.sort(idProntos);
		return idProntos;
	}

	@Override
	protected Integer idProcessoExecutando() {
		return executandoCPU.idProcesso;
	}

	@Override
	protected List<Integer> idProcessosEsperando() {
		List<Integer> idEsperando = new ArrayList<>();
		for (PCB aux : esperando) {
			idEsperando.add(aux.idProcesso);
		}
		Collections.sort(idEsperando);
		return idEsperando;
	}

	@Override
	protected List<Integer> idProcessosTerminados() {
		List<Integer> idTerminados = new ArrayList<>();
		for (PCB aux : terminados) {
			idTerminados.add(aux.idProcesso);
		}
		Collections.sort(idTerminados);
		return idTerminados;
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
