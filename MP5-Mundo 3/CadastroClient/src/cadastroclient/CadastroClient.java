/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
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
