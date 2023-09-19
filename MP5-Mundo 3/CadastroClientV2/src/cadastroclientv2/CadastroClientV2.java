/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
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


