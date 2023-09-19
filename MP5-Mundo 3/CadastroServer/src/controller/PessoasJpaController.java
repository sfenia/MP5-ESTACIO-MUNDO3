/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import controller.exceptions.IllegalOrphanException;
import controller.exceptions.NonexistentEntityException;
import controller.exceptions.PreexistingEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import model.PessoasJuridicas;
import model.PessoasFisicas;
import model.Movimentos;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import model.Pessoas;

/**
 *
 * @author leosc
 */
public class PessoasJpaController implements Serializable {

    public PessoasJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Pessoas pessoas) throws PreexistingEntityException, Exception {
        if (pessoas.getMovimentosCollection() == null) {
            pessoas.setMovimentosCollection(new ArrayList<Movimentos>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            PessoasJuridicas pessoasJuridicas = pessoas.getPessoasJuridicas();
            if (pessoasJuridicas != null) {
                pessoasJuridicas = em.getReference(pessoasJuridicas.getClass(), pessoasJuridicas.getIdPJuridica());
                pessoas.setPessoasJuridicas(pessoasJuridicas);
            }
            PessoasFisicas pessoasFisicas = pessoas.getPessoasFisicas();
            if (pessoasFisicas != null) {
                pessoasFisicas = em.getReference(pessoasFisicas.getClass(), pessoasFisicas.getIdPFisica());
                pessoas.setPessoasFisicas(pessoasFisicas);
            }
            Collection<Movimentos> attachedMovimentosCollection = new ArrayList<Movimentos>();
            for (Movimentos movimentosCollectionMovimentosToAttach : pessoas.getMovimentosCollection()) {
                movimentosCollectionMovimentosToAttach = em.getReference(movimentosCollectionMovimentosToAttach.getClass(), movimentosCollectionMovimentosToAttach.getIdMovimentos());
                attachedMovimentosCollection.add(movimentosCollectionMovimentosToAttach);
            }
            pessoas.setMovimentosCollection(attachedMovimentosCollection);
            em.persist(pessoas);
            if (pessoasJuridicas != null) {
                Pessoas oldPessoasOfPessoasJuridicas = pessoasJuridicas.getPessoas();
                if (oldPessoasOfPessoasJuridicas != null) {
                    oldPessoasOfPessoasJuridicas.setPessoasJuridicas(null);
                    oldPessoasOfPessoasJuridicas = em.merge(oldPessoasOfPessoasJuridicas);
                }
                pessoasJuridicas.setPessoas(pessoas);
                pessoasJuridicas = em.merge(pessoasJuridicas);
            }
            if (pessoasFisicas != null) {
                Pessoas oldPessoasOfPessoasFisicas = pessoasFisicas.getPessoas();
                if (oldPessoasOfPessoasFisicas != null) {
                    oldPessoasOfPessoasFisicas.setPessoasFisicas(null);
                    oldPessoasOfPessoasFisicas = em.merge(oldPessoasOfPessoasFisicas);
                }
                pessoasFisicas.setPessoas(pessoas);
                pessoasFisicas = em.merge(pessoasFisicas);
            }
            for (Movimentos movimentosCollectionMovimentos : pessoas.getMovimentosCollection()) {
                Pessoas oldIdPessoaOfMovimentosCollectionMovimentos = movimentosCollectionMovimentos.getIdPessoa();
                movimentosCollectionMovimentos.setIdPessoa(pessoas);
                movimentosCollectionMovimentos = em.merge(movimentosCollectionMovimentos);
                if (oldIdPessoaOfMovimentosCollectionMovimentos != null) {
                    oldIdPessoaOfMovimentosCollectionMovimentos.getMovimentosCollection().remove(movimentosCollectionMovimentos);
                    oldIdPessoaOfMovimentosCollectionMovimentos = em.merge(oldIdPessoaOfMovimentosCollectionMovimentos);
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findPessoas(pessoas.getIdPessoa()) != null) {
                throw new PreexistingEntityException("Pessoas " + pessoas + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Pessoas pessoas) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pessoas persistentPessoas = em.find(Pessoas.class, pessoas.getIdPessoa());
            PessoasJuridicas pessoasJuridicasOld = persistentPessoas.getPessoasJuridicas();
            PessoasJuridicas pessoasJuridicasNew = pessoas.getPessoasJuridicas();
            PessoasFisicas pessoasFisicasOld = persistentPessoas.getPessoasFisicas();
            PessoasFisicas pessoasFisicasNew = pessoas.getPessoasFisicas();
            Collection<Movimentos> movimentosCollectionOld = persistentPessoas.getMovimentosCollection();
            Collection<Movimentos> movimentosCollectionNew = pessoas.getMovimentosCollection();
            List<String> illegalOrphanMessages = null;
            if (pessoasJuridicasOld != null && !pessoasJuridicasOld.equals(pessoasJuridicasNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain PessoasJuridicas " + pessoasJuridicasOld + " since its pessoas field is not nullable.");
            }
            if (pessoasFisicasOld != null && !pessoasFisicasOld.equals(pessoasFisicasNew)) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("You must retain PessoasFisicas " + pessoasFisicasOld + " since its pessoas field is not nullable.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (pessoasJuridicasNew != null) {
                pessoasJuridicasNew = em.getReference(pessoasJuridicasNew.getClass(), pessoasJuridicasNew.getIdPJuridica());
                pessoas.setPessoasJuridicas(pessoasJuridicasNew);
            }
            if (pessoasFisicasNew != null) {
                pessoasFisicasNew = em.getReference(pessoasFisicasNew.getClass(), pessoasFisicasNew.getIdPFisica());
                pessoas.setPessoasFisicas(pessoasFisicasNew);
            }
            Collection<Movimentos> attachedMovimentosCollectionNew = new ArrayList<Movimentos>();
            for (Movimentos movimentosCollectionNewMovimentosToAttach : movimentosCollectionNew) {
                movimentosCollectionNewMovimentosToAttach = em.getReference(movimentosCollectionNewMovimentosToAttach.getClass(), movimentosCollectionNewMovimentosToAttach.getIdMovimentos());
                attachedMovimentosCollectionNew.add(movimentosCollectionNewMovimentosToAttach);
            }
            movimentosCollectionNew = attachedMovimentosCollectionNew;
            pessoas.setMovimentosCollection(movimentosCollectionNew);
            pessoas = em.merge(pessoas);
            if (pessoasJuridicasNew != null && !pessoasJuridicasNew.equals(pessoasJuridicasOld)) {
                Pessoas oldPessoasOfPessoasJuridicas = pessoasJuridicasNew.getPessoas();
                if (oldPessoasOfPessoasJuridicas != null) {
                    oldPessoasOfPessoasJuridicas.setPessoasJuridicas(null);
                    oldPessoasOfPessoasJuridicas = em.merge(oldPessoasOfPessoasJuridicas);
                }
                pessoasJuridicasNew.setPessoas(pessoas);
                pessoasJuridicasNew = em.merge(pessoasJuridicasNew);
            }
            if (pessoasFisicasNew != null && !pessoasFisicasNew.equals(pessoasFisicasOld)) {
                Pessoas oldPessoasOfPessoasFisicas = pessoasFisicasNew.getPessoas();
                if (oldPessoasOfPessoasFisicas != null) {
                    oldPessoasOfPessoasFisicas.setPessoasFisicas(null);
                    oldPessoasOfPessoasFisicas = em.merge(oldPessoasOfPessoasFisicas);
                }
                pessoasFisicasNew.setPessoas(pessoas);
                pessoasFisicasNew = em.merge(pessoasFisicasNew);
            }
            for (Movimentos movimentosCollectionOldMovimentos : movimentosCollectionOld) {
                if (!movimentosCollectionNew.contains(movimentosCollectionOldMovimentos)) {
                    movimentosCollectionOldMovimentos.setIdPessoa(null);
                    movimentosCollectionOldMovimentos = em.merge(movimentosCollectionOldMovimentos);
                }
            }
            for (Movimentos movimentosCollectionNewMovimentos : movimentosCollectionNew) {
                if (!movimentosCollectionOld.contains(movimentosCollectionNewMovimentos)) {
                    Pessoas oldIdPessoaOfMovimentosCollectionNewMovimentos = movimentosCollectionNewMovimentos.getIdPessoa();
                    movimentosCollectionNewMovimentos.setIdPessoa(pessoas);
                    movimentosCollectionNewMovimentos = em.merge(movimentosCollectionNewMovimentos);
                    if (oldIdPessoaOfMovimentosCollectionNewMovimentos != null && !oldIdPessoaOfMovimentosCollectionNewMovimentos.equals(pessoas)) {
                        oldIdPessoaOfMovimentosCollectionNewMovimentos.getMovimentosCollection().remove(movimentosCollectionNewMovimentos);
                        oldIdPessoaOfMovimentosCollectionNewMovimentos = em.merge(oldIdPessoaOfMovimentosCollectionNewMovimentos);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = pessoas.getIdPessoa();
                if (findPessoas(id) == null) {
                    throw new NonexistentEntityException("The pessoas with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws IllegalOrphanException, NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pessoas pessoas;
            try {
                pessoas = em.getReference(Pessoas.class, id);
                pessoas.getIdPessoa();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The pessoas with id " + id + " no longer exists.", enfe);
            }
            List<String> illegalOrphanMessages = null;
            PessoasJuridicas pessoasJuridicasOrphanCheck = pessoas.getPessoasJuridicas();
            if (pessoasJuridicasOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Pessoas (" + pessoas + ") cannot be destroyed since the PessoasJuridicas " + pessoasJuridicasOrphanCheck + " in its pessoasJuridicas field has a non-nullable pessoas field.");
            }
            PessoasFisicas pessoasFisicasOrphanCheck = pessoas.getPessoasFisicas();
            if (pessoasFisicasOrphanCheck != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("This Pessoas (" + pessoas + ") cannot be destroyed since the PessoasFisicas " + pessoasFisicasOrphanCheck + " in its pessoasFisicas field has a non-nullable pessoas field.");
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            Collection<Movimentos> movimentosCollection = pessoas.getMovimentosCollection();
            for (Movimentos movimentosCollectionMovimentos : movimentosCollection) {
                movimentosCollectionMovimentos.setIdPessoa(null);
                movimentosCollectionMovimentos = em.merge(movimentosCollectionMovimentos);
            }
            em.remove(pessoas);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Pessoas> findPessoasEntities() {
        return findPessoasEntities(true, -1, -1);
    }

    public List<Pessoas> findPessoasEntities(int maxResults, int firstResult) {
        return findPessoasEntities(false, maxResults, firstResult);
    }

    private List<Pessoas> findPessoasEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Pessoas.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Pessoas findPessoas(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Pessoas.class, id);
        } finally {
            em.close();
        }
    }

    public int getPessoasCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Pessoas> rt = cq.from(Pessoas.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
