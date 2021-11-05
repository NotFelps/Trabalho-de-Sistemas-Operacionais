package escalonadores;

public class EscalonadorPai {     //classe que irá herdar os outros escalonadores
    
    public void verificaES() {
        //nesse metodo o escalonador deve conferir se as operacoesES dos processos esperando ja terminaram, e assim definir se o PCB vai pra fila de pronto, outra fila de dispositivo (esperando) ou para a fila de terminados
        for(int i=0; i < 5 ; i++) {
            switch(i) {
                case 0 :
                    if(opatualES_D0.ciclos <= 0) {
                        opatualES_D0 = null;
                    }
                    break;

                    case 1 :
                    if(opatualES_D1.ciclos <= 0) {
                        opatualES_D1 = null;
                    }
                    break;

                    case 2 :
                    if(opatualES_D2.ciclos <= 0) {
                        opatualES_D2 = null;
                    }
                    break;

                    case 3 :
                    if(opatualES_D3.ciclos <= 0) {
                        opatualES_D3 = null;
                    }
                    break;

                    case 4 :
                    if(opatualES_D4.ciclos <= 0) {
                        opatualES_D4 = null;
                    }
                    break;


            }
        }
    }

    public void verificaEsperando() {
        for(PCB p : esperando) {
            
        }
    }

    public void ordena() {
        
    }

}
