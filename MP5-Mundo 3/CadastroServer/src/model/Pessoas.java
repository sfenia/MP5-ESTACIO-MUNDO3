/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 *
 * @author leosc
 */
@Entity
@Table(name = "Pessoas")
@NamedQueries({
    @NamedQuery(name = "Pessoas.findAll", query = "SELECT p FROM Pessoas p"),
    @NamedQuery(name = "Pessoas.findByIdPessoa", query = "SELECT p FROM Pessoas p WHERE p.idPessoa = :idPessoa"),
    @NamedQuery(name = "Pessoas.findByNome", query = "SELECT p FROM Pessoas p WHERE p.nome = :nome"),
    @NamedQuery(name = "Pessoas.findByCidade", query = "SELECT p FROM Pessoas p WHERE p.cidade = :cidade"),
    @NamedQuery(name = "Pessoas.findByEstado", query = "SELECT p FROM Pessoas p WHERE p.estado = :estado"),
    @NamedQuery(name = "Pessoas.findByTelefone", query = "SELECT p FROM Pessoas p WHERE p.telefone = :telefone"),
    @NamedQuery(name = "Pessoas.findByEmail", query = "SELECT p FROM Pessoas p WHERE p.email = :email"),
    @NamedQuery(name = "Pessoas.findByLogradouro", query = "SELECT p FROM Pessoas p WHERE p.logradouro = :logradouro")})
public class Pessoas implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "idPessoa")
    private Integer idPessoa;
    @Column(name = "nome")
    private String nome;
    @Column(name = "cidade")
    private String cidade;
    @Column(name = "estado")
    private String estado;
    @Column(name = "telefone")
    private String telefone;
    @Column(name = "email")
    private String email;
    @Column(name = "logradouro")
    private String logradouro;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "pessoas")
    private PessoasJuridicas pessoasJuridicas;
    @OneToMany(mappedBy = "idPessoa")
    private Collection<Movimentos> movimentosCollection;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "pessoas")
    private PessoasFisicas pessoasFisicas;

    public Pessoas() {
    }

    public Pessoas(Integer idPessoa) {
        this.idPessoa = idPessoa;
    }

    public Integer getIdPessoa() {
        return idPessoa;
    }

    public void setIdPessoa(Integer idPessoa) {
        this.idPessoa = idPessoa;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public PessoasJuridicas getPessoasJuridicas() {
        return pessoasJuridicas;
    }

    public void setPessoasJuridicas(PessoasJuridicas pessoasJuridicas) {
        this.pessoasJuridicas = pessoasJuridicas;
    }

    public Collection<Movimentos> getMovimentosCollection() {
        return movimentosCollection;
    }

    public void setMovimentosCollection(Collection<Movimentos> movimentosCollection) {
        this.movimentosCollection = movimentosCollection;
    }

    public PessoasFisicas getPessoasFisicas() {
        return pessoasFisicas;
    }

    public void setPessoasFisicas(PessoasFisicas pessoasFisicas) {
        this.pessoasFisicas = pessoasFisicas;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (idPessoa != null ? idPessoa.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Pessoas)) {
            return false;
        }
        Pessoas other = (Pessoas) object;
        if ((this.idPessoa == null && other.idPessoa != null) || (this.idPessoa != null && !this.idPessoa.equals(other.idPessoa))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "model.Pessoas[ idPessoa=" + idPessoa + " ]";
    }
    
}
