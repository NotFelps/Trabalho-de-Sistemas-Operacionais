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
	PCB estadopronto = null; //processo no ESTADO pronto
	List<PCB> esperando = new LinkedList<PCB>();    //processos esperando operacaoES
	List<PCB> terminados = new LinkedList<PCB>();    //processos terminados
	
	int escalonadorEscolhido;
	int numeroProcessos;
	PCB executandoCPU;
	Integer processoCriado;
	int metodochamado = 0;

	public void adicionaEspera() {     //adiciona o contador tempoEspera dos processos que estao na lista de prontos
		if(!prontos.isEmpty()) {
			for(PCB p : prontos) {
				p.tempoEspera++;
			}
		}
	}

	//////////////////////////////////////////////////////////////////////

	public void verificaEsperando() {
        if(!esperando.isEmpty()) {
			//for(PCB p : esperando) {
			for(int i = 0; i<esperando.size();i++) {
				PCB p = esperando.get(i);
				Operacao aux2 = null;
				OperacaoES aux = (OperacaoES) p.codigo[p.contadorDePrograma];       //Operacao atual (com certeza ES)
				if((p.contadorDePrograma) != (p.codigo.length-1) ) aux2 = (Operacao) p.codigo[p.contadorDePrograma+1]; // Proxima operacao do processador (pode ser null)
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
						//System.out.println("\naux2 deu null aqui\n");
						esperando.remove(p);     //tira da lista de esperando
						terminados.add(p);    //coloca na lista de terminados
						p.estado = Estado.TERMINADO;
						p.cicloTerminado = cicloAtual;
						p.tempoRetorno = (p.cicloTerminado-p.cicloPronto);
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
						//System.out.print("PASSOU AQUIIIIIIII");
						esperando.remove(p);
						p.contadorDePrograma++;
						p.estado = Estado.PRONTO;
						prontos.add(p);
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
		pcb.cicloPronto = cicloAtual;
		if(escalonadorEscolhido == 3) pcb.rRobin = 1;
		if(escalonadorEscolhido == 2) pcb.srtf = 1;
	}

	@Override
	protected void trocaContexto(PCB pcbAtual, PCB pcbProximo) {
		executandoCPU = pcbProximo;
		if(executandoCPU.jaFoiCPU == 0) {
			executandoCPU.jaFoiCPU = 1;
			executandoCPU.tempoResposta = executandoCPU.tempoEspera;
		}
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
	protected Operacao proximaOperacaoCPU() {	//Retorna a operação atual que está dentro de "executandoCPU"
		
		
		//System.out.println(metodochamado+"\n");
		if(executandoCPU != null) {    //tem processo na CPU, e se eu mantive ele na CPU, eh pq a operacao atual eh de CPU
			Operacao nextOP = (Operacao) executandoCPU.codigo[executandoCPU.contadorDePrograma];   //eh o retorno da funcao
			executandoCPU.contadorDePrograma++;

			//arrumando o valor do proximo chute
			executandoCPU.contadorBurst++;
			executandoCPU.remainingTime--;

			//Agora eh necessario ver se o processo ainda vai usar a cpu ou se vai pra lista de esperando/terminado
			if(executandoCPU.contadorDePrograma == executandoCPU.codigo.length) {  //era a ultima operacao do processo
				PCB terminado = executandoCPU;
				terminado.cicloTerminado = cicloAtual;
				terminado.tempoRetorno = (terminado.cicloTerminado-terminado.cicloPronto);
				terminado.estado = Estado.TERMINADO;
				terminados.add(terminado);
				executandoCPU = null;
				//Aqui colocamos o processo na lista de terminados e limpamos a CPU

			} else {
				Operacao nextRealocar = (Operacao) executandoCPU.codigo[executandoCPU.contadorDePrograma];
				if(nextRealocar instanceof OperacaoES) {

					//processo acabou seu burst de CPU, portanto vou atualizar seu chute
					executandoCPU.proxChuteTamBurstCPU = ((executandoCPU.proxChuteTamBurstCPU+executandoCPU.contadorBurst)/2);
					executandoCPU.contadorBurst = 0;
					executandoCPU.remainingTime = executandoCPU.proxChuteTamBurstCPU;

					OperacaoES nextRealocarES = (OperacaoES) nextRealocar;
					switch(nextRealocarES.idDispositivo) {
						
						case 0 :
							listaD0.add(nextRealocarES);
							executandoCPU.estado = Estado.ESPERANDO;
							esperando.add(executandoCPU);
							executandoCPU = null;
							break;

						case 1 :
							listaD1.add(nextRealocarES);
							executandoCPU.estado = Estado.ESPERANDO;
							esperando.add(executandoCPU);
							executandoCPU = null;
							break;

						case 2 :
							listaD2.add(nextRealocarES);
							executandoCPU.estado = Estado.ESPERANDO;
							esperando.add(executandoCPU);
							executandoCPU = null;
							break;

						case 3 :
							listaD3.add(nextRealocarES);
							executandoCPU.estado = Estado.ESPERANDO;
							esperando.add(executandoCPU);
							executandoCPU = null;
							break;

						case 4 :
							listaD4.add(nextRealocarES);
							executandoCPU.estado = Estado.ESPERANDO;
							esperando.add(executandoCPU);
							executandoCPU = null;
							break;
					}
				} else if((executandoCPU.rRobin == 1) && (executandoCPU.contadorBurst == 5)) {
					
					executandoCPU.contadorBurst = 0;
					//System.out.println("PREEMPTADO!!");
					prontos.add(executandoCPU);
					/* if(executandoCPU.jaFoiCPU == 0) {
						executandoCPU.tempoResposta = executandoCPU.tempoEspera;
					} */
					//trocasContexto++;
					executandoCPU = null;
				}	
			}
			//return executandoCPU.codigo[executandoCPU.contadorDePrograma-1];
			//return nextOP.codigo[nextOP.contadorDePrograma];
			return nextOP;

		} else if(!prontos.isEmpty()) {    //nesse caso executandoCPU estava vazio
			//metodochamado++;
			executandoCPU = prontos.get(0);
			executandoCPU.estado = Estado.EXECUTANDO;
			executandoCPU.contadorBurst++;
			executandoCPU.remainingTime--;
			if(executandoCPU.jaFoiCPU == 0) {
				//executandoCPU.jaFoiCPU = 1;
				executandoCPU.tempoResposta = executandoCPU.tempoEspera;
			} 
			trocasContexto++;
			
			
			prontos.remove(executandoCPU);
			Operacao resposta = (Operacao) executandoCPU.codigo[executandoCPU.contadorDePrograma];
			executandoCPU.contadorDePrograma++;
			
			//agora devemos decidir entre manter o PCB na cpu ou realoca-lo
			if(executandoCPU.contadorDePrograma == executandoCPU.codigo.length) {
				//processo acabou e deve ser mandado para terminados
				executandoCPU.estado = Estado.TERMINADO;
				executandoCPU.cicloTerminado = cicloAtual;
				executandoCPU.tempoRetorno = (executandoCPU.cicloTerminado-executandoCPU.cicloPronto);
				terminados.add(executandoCPU);
				executandoCPU = null;
			} else {
					Operacao ajuda = executandoCPU.codigo[executandoCPU.contadorDePrograma];
					if(ajuda instanceof OperacaoES) {

						//processo acabou seu burst de CPU, portanto vou atualizar seu chute
						executandoCPU.proxChuteTamBurstCPU = ((executandoCPU.proxChuteTamBurstCPU+executandoCPU.contadorBurst)/2);
						executandoCPU.contadorBurst = 0;
						executandoCPU.remainingTime = executandoCPU.proxChuteTamBurstCPU;

						OperacaoES ajudaES = (OperacaoES) executandoCPU.codigo[executandoCPU.contadorDePrograma];
						switch (ajudaES.idDispositivo) {
							case 0 :
								listaD0.add(ajudaES);
								esperando.add(executandoCPU);
								executandoCPU.estado = 	Estado.ESPERANDO;
								executandoCPU = null;
							break;
		
							case 1 :
								listaD1.add(ajudaES);
								esperando.add(executandoCPU);
								executandoCPU.estado = 	Estado.ESPERANDO;
								executandoCPU = null;
							break;
		
							case 2 :
								listaD2.add(ajudaES);
								esperando.add(executandoCPU);
								executandoCPU.estado = 	Estado.ESPERANDO;
								executandoCPU = null;
							break;
		
							case 3 :
								listaD3.add(ajudaES);
								esperando.add(executandoCPU);
								executandoCPU.estado = 	Estado.ESPERANDO;
								executandoCPU = null;
							break;
		
							case 4 :
								listaD4.add(ajudaES);
								esperando.add(executandoCPU);
								executandoCPU.estado = 	Estado.ESPERANDO;
								executandoCPU = null;
							break;
						}
					} //o else nao vai existir pq se a proxima operacao for de CPU o processo vai continuar na CPU
			}
			
			return resposta;
		}
			return null;
	}

	@Override
	protected void executaCicloKernel() {

		processoCriado = null;
		//Verificar processos criados e colocar em seu devido estado/fila	
		if(pcbaux != null)	{
			pcbaux.estado = Estado.PRONTO;
			estadopronto = pcbaux;
			pcbaux = null;
		}

		if(estadopronto != null) {  //estadopronto.codigo[estadopronto.contadorDePrograma]
			
			//se o indice no contador de programa for uma operacao de ES
			if(estadopronto.codigo[estadopronto.contadorDePrograma] instanceof OperacaoES) {
				OperacaoES aux = (OperacaoES) estadopronto.codigo[estadopronto.contadorDePrograma];
				int aux2 = aux.idDispositivo;   //aux 2 eh o dispositivo da operacaoES
				switch (aux2) {
					case 0 :
						listaD0.add(aux);
						estadopronto.estado = Estado.ESPERANDO;
						esperando.add(estadopronto);
						estadopronto = null;
					break;

					case 1 :
						listaD1.add(aux);
						estadopronto.estado = Estado.ESPERANDO;
						esperando.add(estadopronto);
						estadopronto = null;
					break;

					case 2 :
						listaD2.add(aux);
						estadopronto.estado = Estado.ESPERANDO;
						esperando.add(estadopronto);
						estadopronto = null;
					break;

					case 3 :
						listaD3.add(aux);
						estadopronto.estado = Estado.ESPERANDO;
						esperando.add(estadopronto);
						estadopronto = null;
					break;

					case 4 :
						listaD4.add(aux);
						estadopronto.estado = Estado.ESPERANDO;
						esperando.add(estadopronto);
						estadopronto = null;
					break;
				}
			} else {
				prontos.add(estadopronto);
				estadopronto = null;
			}
		}

		verificaEsperando(); //arruma todas as listas dos dispositivos de ES
		adicionaEspera();

		//AGORA TEMOS QUE CHAMAR O ESCALONADOR EM QUESTAO PARA ORGANIZAR A FILA DE PRONTOS

		switch (escalonadorEscolhido) {
			case 0 :     //Lista de prontos eh por ordem de chegada, entao nao deve ser arrumada (FCFS)
			
				break;

			case 1 :    //lista de prontos deve ser ordenada em ordem do processo mais curto para o mais longo (SJB)
				Collections.sort(prontos);
				break;

			case 2 :   //deve verificar em cada cicloKernel qual a operacao com o menor remainingTime, tanto a operacao que esta na CPU quanto as da lista de pronto
			if(!prontos.isEmpty()) {
				Collections.sort(prontos);
				if(executandoCPU != null) {
					PCB compara = prontos.get(0);
					if(compara.remainingTime < executandoCPU.remainingTime) {
						executandoCPU.proxChuteTamBurstCPU = ((executandoCPU.proxChuteTamBurstCPU+executandoCPU.contadorBurst)/2);
						executandoCPU.contadorBurst = 0;
						executandoCPU.remainingTime = executandoCPU.proxChuteTamBurstCPU;
						prontos.add(executandoCPU);
						executandoCPU = null;
					}
				}
			}

			break;

			case 3 :   //cada processo tem de 5 em 5 ciclos para fazer sua operacao (Round Robin)

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
		int tempoEsperaT = 0;
		int contador = 0;
		for(PCB aux : terminados) {
			tempoEsperaT += aux.tempoEspera;
			contador++;
		}
		return (tempoEsperaT/contador);
	}

	@Override
	protected int tempoRespostaMedio() {
		int tempoResp = 0;
		int contador = 0;
		for(PCB aux : terminados) {
			tempoResp += aux.tempoResposta;
			contador++;
		}
		return (tempoResp/contador);
	}

	@Override
	protected int tempoRetornoMedio() {
		int mediaTerminados = 0; 
		int contador = 0;
		for(PCB aux : terminados) {
			mediaTerminados = mediaTerminados + aux.tempoRetorno;
			contador++;
		}
		int media = (mediaTerminados/contador);
		return media;
	}
	
	@Override
	protected int trocasContexto() {
		return trocasContexto-1;
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
