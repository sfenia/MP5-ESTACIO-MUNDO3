
# Servidores e clientes baseados em Socket, com uso de Threads.

Threads usada no lado do cliente quanto no lado do servidor, acessando o banco de dados via JPA.

O objetivo desta prática é desenvolver servidores Java com base em Sockets, criar clientes síncronos e assíncronos para esses servidores, e utilizar Threads para implementação de processos paralelos.




## Procedimento 1 – Criando o Servidor e Cliente de Teste

No primeiro procedimento, criamos os seguintes componentes:
CadastroServer:

package cadastroserver;

import controller.MovimentosJpaController;
import controller.PessoasJpaController;
import controller.ProdutosJpaController;
import controller.UsuariosJpaController;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
/**
 *
 * @author sfenia
 */
public class CadastroServer {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("CadastroServerPU");
        ProdutosJpaController ctrlProd = new ProdutosJpaController(emf);
        UsuariosJpaController ctrlUsu = new UsuariosJpaController(emf);
        MovimentosJpaController ctrlMov = new MovimentosJpaController(emf);
        PessoasJpaController ctrlPessoa = new PessoasJpaController(emf);

        try (ServerSocket serverSocket = new ServerSocket(4321)) {
            System.out.println("Servidor aguardando conexoes na porta 4321...");
            
            while (true) {
                Socket socket = serverSocket.accept();
                CadastroThreadV2 thread = new CadastroThreadV2(ctrlUsu, ctrlMov, ctrlProd, ctrlPessoa, null, socket);
                thread.start(); // Inicia a thread
                System.out.println("thread iniciado!");
            }
            
        }
    }
}

CadastroThread:

package cadastroserver;

import controller.ProdutosJpaController;
import controller.UsuariosJpaController;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Produtos;
import model.Usuarios;
/**
 *
 * @author sfenia
 */
public class CadastroThread extends Thread {
    private final ProdutosJpaController ctrl;
    private final UsuariosJpaController ctrlUsu;
    private final Socket s1;

    public CadastroThread(ProdutosJpaController ctrl, UsuariosJpaController ctrlUsu, Socket s1) {
        this.ctrl = ctrl;
        this.ctrlUsu = ctrlUsu;
        this.s1 = s1;
    }

    @Override
public void run() {
    try (ObjectOutputStream out = new ObjectOutputStream(s1.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(s1.getInputStream())){
        
        String login = (String) in.readObject();
        String senha = (String) in.readObject();
        List<Usuarios> usuariosList = ctrlUsu.findUsuariosEntities();
        Usuarios usuarioAutenticado = null;
        
        for (Usuarios usuario : usuariosList) {
            if (usuario.getLogin().equals(login) && usuario.getSenha().equals(senha)) {
                usuarioAutenticado = usuario;
                break; 
            }
        }
        
        if (usuarioAutenticado == null) {
            System.out.println("Credenciais inválidas. Desconectando cliente.");
            return;
        }

        System.out.println("Usuario autenticado: " + usuarioAutenticado.getLogin());

        while (true) {
            String comando =(String) in.readObject();

            if (comando.equals("L")) {
                List<Produtos> produtos = ctrl.findProdutosEntities();
                out.writeObject(produtos);
                System.out.println("Enviando lista de produtos para o cliente.");
                break;
            }
        }
        
    try {
        if (out != null) {
            out.close();
        }
        if (in != null) {
            in.close();
        }
        if (s1 != null && !s1.isClosed()) {
            s1.close();
        }
    } catch (IOException ex) {
        System.err.println("Erro ao fechar os fluxos e o socket: " + ex.getMessage());
    }

    } catch (IOException ex) {
        System.err.println("Erro de comunicação: " + ex.getMessage());
    }   catch (ClassNotFoundException ex) { 
            Logger.getLogger(CadastroThread.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
}

CadastroClient:

package cadastroclient;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import model.Produtos;

/**
 *
 * @author sfenia
 */
public class CadastroClient {

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public static void main(String[] args)throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket("localhost", 4321);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                        
            out.writeObject("op1"); 
            out.writeObject("op1"); 
            out.writeObject("L"); 
            System.out.println("Usuario conectado com sucesso");
            
            List<Produtos> produtos = (List<Produtos>) in.readObject();
            for (Produtos produto : produtos) {
                System.out.println(produto.getNome());
            }
        }
    }   
}



Perguntas e Respostas

a) Como funcionam as classes Socket e ServerSocket?

A classe ServerSocket espera por conexões de clientes e, quando uma conexão é estabelecida, cria um Socket para a comunicação com esse cliente. Ambas as classes são fundamentais para a implementação de comunicação cliente-servidor em Java.

b) Qual a importância das portas para a conexão com servidores?

As portas são números de identificação associados aos processos de comunicação em um servidor. Elas são essenciais para direcionar dados para os serviços corretos em um servidor, permitindo que vários serviços funcionem simultaneamente no mesmo endereço IP. Portas garantem que os dados sejam entregues ao aplicativo de destino correto, possibilitando a comunicação entre clientes e serviços específicos.

c) Para que servem as classes de entrada e saída ObjectInputStream e ObjectOutputStream, e por que os objetos transmitidos devem ser serializáveis?

    ObjectOutputStream: Permite que objetos sejam convertidos em uma sequência de bytes, tornando-os adequados para transmissão em rede.
    ObjectInputStream: Realiza a operação oposta, convertendo a sequência de bytes recebida de volta para objetos.

Os objetos transmitidos devem ser serializáveis porque a serialização garante que os objetos sejam convertidos em um formato padronizado de bytes, que pode ser transmitido pela rede e reconstruído em objetos idênticos do lado receptor.

d) Por que, mesmo utilizando as classes de entidades JPA no cliente, foi possível garantir o isolamento do acesso ao banco de dados?

O isolamento do acesso ao banco de dados é alcançado usando o padrão de arquitetura Cliente-Servidor e a abstração fornecida pelas classes de entidades JPA. As classes de entidades JPA permitem que o cliente interaja com os dados de forma orientada a objetos, enquanto a lógica de acesso ao banco de dados é tratada no servidor.
Procedimento 2 – Alimentando a Base

No segundo procedimento, criamos os seguintes componentes:
CadastroThreadV2:

package cadastroserver;

import controller.MovimentosJpaController;
import controller.PessoasJpaController;
import controller.ProdutosJpaController;
import controller.UsuariosJpaController;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Movimentos;
import model.Produtos;
import model.Pessoas;
import model.Usuarios;
 
/**
 *
 * @author sfenia
 */
public class CadastroThreadV2 extends Thread {
    private final UsuariosJpaController ctrlUsu;
    private final MovimentosJpaController ctrlMov;
    private final ProdutosJpaController ctrlProd;
    private final PessoasJpaController ctrlPessoa;
    private Usuarios usuarioAutenticado;
    private final Socket s1;

    public CadastroThreadV2(UsuariosJpaController ctrlUsu, MovimentosJpaController ctrlMov, ProdutosJpaController ctrlProd, PessoasJpaController ctrlPessoa, Usuarios usuarioAutenticado, Socket s1) {  
        this.ctrlUsu = ctrlUsu; 
        this.ctrlMov = ctrlMov;
        this.ctrlProd = ctrlProd;
        this.ctrlPessoa = ctrlPessoa;
        this.s1 = s1;
    }

    @Override
    public void run() {
        String mensagemAutenticacao = "";
        boolean menu = true;
        try (ObjectOutputStream out = new ObjectOutputStream(s1.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(s1.getInputStream())) {
            
            String login = (String) in.readObject();
            String senha = (String) in.readObject();
            
            List<Usuarios> usuariosList = ctrlUsu.findUsuariosEntities();
            for (Usuarios usuario : usuariosList) {
                if (usuario.getLogin().equals(login) && usuario.getSenha().equals(senha)) {
                    usuarioAutenticado = usuario;
                    mensagemAutenticacao = "Usuario conectado com sucesso";
                    break; 
                }
            }

            if (usuarioAutenticado == null) {
                System.out.println("Credenciais inválidas. Desconectando cliente.");
                mensagemAutenticacao = "Credenciais inválidas. Desconectando cliente.";
                menu = false;
            }
            
            String dataHora = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy").format(new Date()); // Obtém a data e hora formatada
            String mensagemCompleta = ">> Nova comunicação em " + dataHora  ;

            out.writeObject(mensagemCompleta);
            out.writeObject(mensagemAutenticacao);
            out.flush();
            
            while (menu == true) {
                String comando = (String) in.readObject();
                switch (comando) {
                    case "E":
                        System.out.println("opçao E");
                        processEntrada(in, out);
                        break;
                    case "S":
                        System.out.println("opçao S");
                        processSaida(in, out);
                        break;
                    case "L":
                        List<Produtos> produtosList = ctrlProd.findProdutosEntities();
                        dataHora = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy").format(new Date());
                        mensagemCompleta = ">> Nova comunicação em " + dataHora  ;
                        out.writeObject(mensagemCompleta);
                        for (Produtos produto : produtosList) {
                            String produtoInfo = produto.getNome() + " : " + produto.getQuantidade();
                            out.writeObject(produtoInfo);
                            out.flush();
                        }   
                        break;
                    case "X":
                        menu = false;
                        break;
                    default:
                        
                        break;
                }
            }
        } catch (IOException ex) {
            System.err.println("Erro de comunicação: " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(CadastroThreadV2.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CadastroThreadV2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void processEntrada(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException, Exception {
        char tipoMovimento = 'E'; // Tipo de movimento Entrada

        int idPessoa = in.readInt();
        int idProduto = in.readInt();
        int quantidade = in.readInt();
        float valorUnitario = in.readFloat();
        int idUsuario = usuarioAutenticado.getIdUsuario();


CadastroClientV2:

package cadastroclientv2;

import java.io.*;
import java.net.*;
import java.util.List;
import javax.swing.*;
import model.Produtos;

/**
 *
 * @author sfenia
 */
public class CadastroClientV2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
            Socket socket = new Socket("localhost", 4321);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            boolean menu = true;
            
            System.out.print("Login: ");
            String login = reader.readLine();
            System.out.print("Senha: ");
            String senha = reader.readLine();

            out.writeObject(login);
            out.writeObject(senha);
            out.writeObject("Mensagem do servidor para o cliente.");
            out.flush();
            
            JFrame frame = new JFrame("Mensagens do Servidor");
            JTextArea textArea = new JTextArea(20, 50);
            textArea.setEditable(false);
            frame.add(new JScrollPane(textArea));
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            ThreadClient threadClient = new ThreadClient(in, textArea);
            threadClient.start();

            while (menu == true) {
                System.out.println("L - Listar | E - Entrada | S - Saida | X - Finalizar");
                String opcao = reader.readLine().toUpperCase();
                out.writeObject(opcao);
                out.flush();

                switch (opcao) {
                    case "X":
                        menu = false;
                       
                        break;
                    case "L":
                        break;

                    case "E":
                    case "S":
                        System.out.print("ID da pessoa: ");
                        int idPessoa = Integer.parseInt(reader.readLine());

                        System.out.print("ID do produto: ");
                        int idProduto = Integer.parseInt(reader.readLine());

                        System.out.print("Quantidade: ");
                        int quantidade = Integer.parseInt(reader.readLine());

                        System.out.print("Valor unitário: ");
                        float valorUnitario = Float.parseFloat(reader.readLine());

                        out.writeInt(idPessoa);
                        out.writeInt(idProduto);
                        out.writeInt(quantidade);
                        out.writeFloat(valorUnitario);
                        out.flush();
                        break;

                    default:
                        System.out.println("Opção inválida.");
                        break;
                }

            }
    }    
}



ThreadClient:

package cadastroclientv2;

import java.io.ObjectInputStream;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 *
 * @author sfenia
 */
public class ThreadClient extends Thread {
    private  ObjectInputStream in;
    private final JTextArea textArea;

    public ThreadClient(ObjectInputStream in, JTextArea textArea) {
        this.in = in;
        this.textArea = textArea;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object data = in.readObject();
                String mensagem = (String) data;
                SwingUtilities.invokeLater(() -> {
                textArea.append( mensagem + "\n");
                textArea.setCaretPosition(textArea.getDocument().getLength()); // Rolagem automática
                });   
            }
        } catch (Exception e) {
            // Lidar com exceções, se necessário
        }
    }
}




## Perguntas e Respostas

Como as Threads podem ser utilizadas para o tratamento assíncrono das respostas enviadas pelo servidor?

Através de uma Thread dedicada à comunicação com o servidor, o cliente pode continuar sua execução normal enquanto aguarda respostas. Isso mantém a interface gráfica responsiva, evitando bloqueios. Ao receber respostas, a Thread assíncrona atualiza a interface de usuário. Isso permite uma melhor experiência do usuário, pois as operações de comunicação não interferem na interatividade da interface.

a) Para que serve o método invokeLater, da classe SwingUtilities?

O método invokeLater da classe SwingUtilities é usado para executar uma determinada ação de forma assíncrona na thread de eventos do Swing, que é responsável pela atualização da interface gráfica. Isso é essencial para garantir que as atualizações na interface ocorram de forma segura, evitando conflitos entre threads e mantendo a responsividade da aplicação.

b) Como os objetos são enviados e recebidos pelo Socket Java?

Em Java, objetos são enviados e recebidos através de sockets utilizando a serialização. A serialização converte os objetos em uma sequência de bytes que podem ser transmitidos pela rede. O ObjectOutputStream é usado para escrever objetos em bytes, que são enviados pelo socket. O ObjectInputStream é usado para ler os bytes recebidos e reconstruir os objetos originais. A classe dos objetos deve implementar a interface Serializable para permitir a serialização e desserialização.

c) Compare a utilização de comportamento assíncrono ou síncrono nos clientes com Socket Java, ressaltando as características relacionadas ao bloqueio do processamento.

Na utilização de sockets Java, o comportamento assíncrono permite que os clientes continuem executando outras tarefas enquanto esperam por respostas do servidor. Isso evita bloqueios e mantém a responsividade do programa. Por outro lado, o comportamento síncrono exige que o cliente aguarde a resposta do servidor antes de continuar, o que pode resultar em bloqueios e tornar o programa menos eficiente em termos de utilização de recursos e tempo de resposta. O comportamento assíncrono é preferível para garantir a fluidez da interação do usuário e otimizar o uso de recursos do sistema.
Conclusão

Neste projeto, desenvolvemos uma aplicação cliente-servidor com o objetivo de administrar registros. Implementamos de maneira sólida as funcionalidades de autenticação, entrada e saída de dados, organização eficaz ao dividir em classes de controle independentes, utilização apropriada de threads para comunicação assíncrona e desenvolvimento de uma interface gráfica utilizando a biblioteca Swing. Isso resultou em uma experiência significativa na aplicação de princípios teóricos em um contexto real.
Linguagens e Ferramentas Utilizadas

    Java
    Java Persistence API (JPA)
    Swing (Biblioteca para desenvolvimento da interface gráfica)

Este projeto foi desenvolvido como parte do curso de Desenvolvimento Full Stack na disciplina "Por que não paralelizar?" no período de Mundo 3, em 2023.2.

