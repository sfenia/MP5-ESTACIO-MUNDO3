/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

