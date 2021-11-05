package kernel;
import java.util.*;

import kernel.PCB.Estado;
import operacoes.*;
//import operacoes.Carrega;
import operacoes.Operacao;
import operacoes.OperacaoES;
import escalonadores.*;

public class SeuSO extends SO {

	int trocasContexto = 0;
	PCB pcbnovo, pcbaux;
	//HashMap<Integer, OperacaoES[]> listaES = new HashMap<Integer, OperacaoES[]>();
	int criaIdProcesso = 0;    //usado para criar o id de processo na ordem certa na função "criaProcesso"

	List<PCB> prontos = new LinkedList<PCB>();     //processos prontos para serem executados
	List<PCB> esperando = new LinkedList<PCB>();    //processos esperando operacaoES
	List<PCB> terminados = new LinkedList<PCB>();    //processos terminados
	
	int escalonadorEscolhido;
	int numeroProcessos;
	PCB executandoCPU;
	Integer processoCriado;


	//////////////////////////////////////////////////////////////////////

	public void verificaEsperando() {
        if(esperando != null) {
			for(PCB p : esperando) {
				Operacao aux2 = null;
				OperacaoES aux = (OperacaoES) p.codigo[p.contadorDePrograma];       //Operacao atual (com certeza ES)
				if(p.contadorDePrograma != (p.codigo.length-1) ) aux2 = (Operacao) p.codigo[p.contadorDePrograma+1]; // Proxima operacao do processador (pode ser null)
				if(aux.ciclos <= 0) {    //operacao acabou e processo precisa mudar de lugar

					//esse switch zera as variaveis auxiliares do dispositivo cuja ES ja acabou
					switch(aux.idDispositivo) {
						case 0 :
							opatualES_D0 = null;
							break;

						case 1 :
							opatualES_D1 = null;
							break;

						case 2 :
							opatualES_D2 = null;
							break;

						case 3 :
							opatualES_D3 = null;
							break;

						case 4 :
							opatualES_D4 = null;
							break;
					}

					if(aux2 == null) {   //tenho q colocar na lista de terminados e tirar da esperando
						esperando.remove(p);     //tira da lista de esperando
						terminados.add(p);    //coloca na lista de terminados
						p.estado = Estado.TERMINADO;
						//processo finalizado   
					}

					if(aux2 instanceof OperacaoES) {    //caso ele tenha uma operacaoES pra fazer no proximo índice de "codigo"
					OperacaoES auxES = (OperacaoES) aux2;     //auxES é a proxima operacao do processo, sendo ela com certeza de entrada e saida
						switch (auxES.idDispositivo) {
							case 0 :
								listaD0.add(auxES);       //nao mexo na lista de esperando pq o processo ja estava nela nesse caso, só adiciono na lista do dispositivo em questão
								p.contadorDePrograma++;
							break;

							case 1 :
								listaD1.add(auxES);
								p.contadorDePrograma++;
							break;

							case 2 :
								listaD2.add(auxES);
								p.contadorDePrograma++;
							break;

							case 3 :
								listaD3.add(auxES);
								p.contadorDePrograma++;
							break;

							case 4 :
								listaD4.add(auxES);
								p.contadorDePrograma++;
							break;
						}
					}

					if(aux2 instanceof Soma || aux2 instanceof Carrega) {      //se for uma operacao de soma ou carrega deve-se colocar o processo na fila de prontos
						esperando.remove(p);
						prontos.add(p);
						p.estado = Estado.PRONTO;
					}
				}
			}
		}
    }   

	//////////////////////////////////////////////////////////////////////


	//criar uma linkedlist pra cada dispositivo
	Queue<OperacaoES> listaD0 = new LinkedList<>();    //fila dispositivo 0
	Queue<OperacaoES> listaD1 = new LinkedList<>();	   //fila dispositivo 1
	Queue<OperacaoES> listaD2 = new LinkedList<>();    //fila dispositivo 2
	Queue<OperacaoES> listaD3 = new LinkedList<>();    //fila dispositivo 3
	Queue<OperacaoES> listaD4 = new LinkedList<>();    //fila dispositivo 4

	//criar uma variavel auxiliar do tipo OperacaoES para cada dispositivo
	OperacaoES opatualES_D0;
	OperacaoES opatualES_D1;
	OperacaoES opatualES_D2;
	OperacaoES opatualES_D3;
	OperacaoES opatualES_D4;

	//////////////////////////////////////////////////////////////////////

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
		numeroProcessos++;
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

		//Primeiramente deve-se colocar o primeiro processo de cada lista em sua respectiva variavel auxiliar
		switch(idDispositivo) {

			case 0 :
				if(!listaD0.isEmpty()) {
					if(opatualES_D0 == null) opatualES_D0 = listaD0.poll();
				}
				if(opatualES_D0 != null) {
					if(opatualES_D0.ciclos > 0) {
						return opatualES_D0;
					}
				} 
				break;

			case 1 :
				if(!listaD1.isEmpty()) {
					if(opatualES_D1 == null) opatualES_D1 = listaD1.poll();
				}
				if(opatualES_D1 != null) {
					if(opatualES_D1.ciclos > 0) {
						return opatualES_D1;
					}
				} 
				break;

			case 2 :
				if(!listaD2.isEmpty()) {
					if(opatualES_D2 == null) opatualES_D2 = listaD2.poll();
				}
				if(opatualES_D2 != null) {
					if(opatualES_D2.ciclos > 0) {
						return opatualES_D2;
					} 
				}
				break;

			case 3 :
				if(!listaD3.isEmpty()) {
					if(opatualES_D3 == null) opatualES_D3 = listaD3.poll();
				}
				if(opatualES_D3 != null) {
					if(opatualES_D3.ciclos > 0) {
						return opatualES_D3;
					}
				}
				break;

			case 4 :
				if(!listaD4.isEmpty()) {
					if(opatualES_D4 == null) opatualES_D4 = listaD4.poll();
				}
			if(opatualES_D4 != null) {
				if(opatualES_D4.ciclos > 0) {
					return opatualES_D4;
				}
			} 
				break;
				
	    }
		return null;
	}

	@Override
	protected Operacao proximaOperacaoCPU() {	//Apenas retorna a operação atual que está dentro de "executandoCPU"
		if(executandoCPU != null) return executandoCPU.codigo[executandoCPU.contadorDePrograma];
		return null;
	}

	@Override
	protected void executaCicloKernel() {
		
		processoCriado = null;
		//Verificar processos criados e colocar em seu devido estado/fila	
		if(pcbaux != null)	{
			pcbaux.estado = Estado.PRONTO;
			prontos.add(pcbaux);
			pcbaux = null;
		}

		verificaEsperando(); //arruma todas as listas dos dispositivos de ES

		if(!prontos.isEmpty()) {
			
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

		switch (escalonadorEscolhido) {
			case 0 :     //Lista de prontos eh por ordem de chegada, entao nao deve ser arrumada
			
			break;

			case 1 :
			break;

			case 2 :
			break;

			case 3 :
			break;
		}


		///////////////////////////////////////////////////////////////////////////////
		pcbaux = pcbnovo;
		if(pcbnovo != null) processoCriado = Integer.valueOf(pcbnovo.idProcesso);  //guarda id do processo novo desse ciclo
		pcbnovo = null;
	}

	@Override
	protected boolean temTarefasPendentes() {   //true se ainda tem processos que nao estao prontos
		return (numeroProcessos != terminados.size());
	}

	@Override
	protected Integer idProcessoNovo() {
		if(processoCriado != null) {
			return processoCriado;
		}
		return null;
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
		if(executandoCPU != null) return executandoCPU.idProcesso;
		return null;
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
		return 0;             //TIRAR DO COMENTARIO QUANDO CRIAR A VARIAVEL
		//return (tempoEspera/numeroProcessos);
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
		return trocasContexto;
	}

	//////////////////////////////////////////////////////////

	@Override
	public void defineEscalonador(Escalonador e) {
		
		switch (e) {
			case  FIRST_COME_FIRST_SERVED:
			escalonadorEscolhido = 0;
			break;

			case  SHORTEST_JOB_FIRST:
			escalonadorEscolhido = 1;
			break;

			case SHORTEST_REMANING_TIME_FIRST :
			escalonadorEscolhido = 2;
			break;
			
			case ROUND_ROBIN_QUANTUM_5 :
			escalonadorEscolhido = 3;
			break;
		}
	}
}
