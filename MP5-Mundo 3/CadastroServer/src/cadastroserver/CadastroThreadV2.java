/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
        
        Usuarios usuario = ctrlUsu.findUsuarios(idUsuario);
        Pessoas pessoa = ctrlPessoa.findPessoas(idPessoa);
        Produtos produto = ctrlProd.findProdutos(idProduto);
        
        Movimentos movimento = new Movimentos();
        movimento.setIdMovimentos(getNextMovimentoId());
        movimento.setIdUsuario(usuario);
        movimento.setTipo(tipoMovimento);
        movimento.setIdPessoa(pessoa);
        movimento.setIdProduto(produto);
        movimento.setQuantidade(quantidade);
        movimento.setPrecoUnitario( valorUnitario);
        
        ctrlMov.create(movimento);
        
        produto.setQuantidade(produto.getQuantidade() + quantidade);
        ctrlProd.edit(produto);
        
        out.writeObject("Movimento de Entrada registrado com sucesso.");
    }

    private void processSaida(ObjectInputStream in, ObjectOutputStream out) throws IOException, ClassNotFoundException, Exception {
        char tipoMovimento = 'S'; // Tipo de movimento Saída

        int idPessoa = in.readInt();
        int idProduto = in.readInt();
        int quantidade = in.readInt();
        float valorUnitario = in.readFloat();
        int idUsuario = usuarioAutenticado.getIdUsuario();
        
        Usuarios usuario = ctrlUsu.findUsuarios(idUsuario);
        Pessoas pessoa = ctrlPessoa.findPessoas(idPessoa);
        Produtos produto = ctrlProd.findProdutos(idProduto);
        
        Movimentos movimento = new Movimentos();
        movimento.setIdMovimentos(getNextMovimentoId());
        movimento.setIdUsuario(usuario);
        movimento.setTipo(tipoMovimento);
        movimento.setIdPessoa(pessoa);
        movimento.setIdProduto(produto);
        movimento.setQuantidade(quantidade);
        movimento.setPrecoUnitario(valorUnitario);
        
        ctrlMov.create(movimento);

        produto.setQuantidade(produto.getQuantidade() - quantidade);
        ctrlProd.edit(produto);

        out.writeObject("Movimento de Saída registrado com sucesso.");
    }
    
    private synchronized int getNextMovimentoId() {
    List<Movimentos> movimentos = ctrlMov.findMovimentosEntities();
    int lastMovimentoId = 0;

    for (Movimentos movimento : movimentos) {
        if (movimento.getIdMovimentos() > lastMovimentoId) {
            lastMovimentoId = movimento.getIdMovimentos();
        }
    }

    return lastMovimentoId + 1;
    }
    
}
