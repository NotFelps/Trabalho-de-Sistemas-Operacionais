package kernel;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import operacoes.Carrega;
import operacoes.Operacao;
import operacoes.OperacaoES;
import operacoes.Soma;

public abstract class SO {

	private HashMap<Integer, Operacao[]> novosProcessos = new HashMap<Integer, Operacao[]>();

	public void simula() {
		while (!novosProcessos.isEmpty() || temTarefasPendentes()) {
			Operacao[] codigo = novosProcessos.get(contadorCiclos);

			if (codigo != null) {
				novosProcessos.remove(contadorCiclos);
				criaProcesso(codigo);
			}

			executaUmCiclo();
			imprimeEstado();
		}

		imprimeEstatisticas();
	}


	}

	
	}
}

