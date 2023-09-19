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
import model.Pessoas;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import model.PessoasFisicas;

/**
 *
 * @author sfenia
 */
public class PessoasFisicasJpaController implements Serializable {

    public PessoasFisicasJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(PessoasFisicas pessoasFisicas) throws IllegalOrphanException, PreexistingEntityException, Exception {
        List<String> illegalOrphanMessages = null;
        Pessoas pessoasOrphanCheck = pessoasFisicas.getPessoas();
        if (pessoasOrphanCheck != null) {
            PessoasFisicas oldPessoasFisicasOfPessoas = pessoasOrphanCheck.getPessoasFisicas();
            if (oldPessoasFisicasOfPessoas != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("The Pessoas " + pessoasOrphanCheck + " already has an item of type PessoasFisicas whose pessoas column cannot be null. Please make another selection for the pessoas field.");
            }
        }
        if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pessoas pessoas = pessoasFisicas.getPessoas();
            if (pessoas != null) {
                pessoas = em.getReference(pessoas.getClass(), pessoas.getIdPessoa());
                pessoasFisicas.setPessoas(pessoas);
            }
            em.persist(pessoasFisicas);
            if (pessoas != null) {
                pessoas.setPessoasFisicas(pessoasFisicas);
                pessoas = em.merge(pessoas);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findPessoasFisicas(pessoasFisicas.getIdPFisica()) != null) {
                throw new PreexistingEntityException("PessoasFisicas " + pessoasFisicas + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(PessoasFisicas pessoasFisicas) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            PessoasFisicas persistentPessoasFisicas = em.find(PessoasFisicas.class, pessoasFisicas.getIdPFisica());
            Pessoas pessoasOld = persistentPessoasFisicas.getPessoas();
            Pessoas pessoasNew = pessoasFisicas.getPessoas();
            List<String> illegalOrphanMessages = null;
            if (pessoasNew != null && !pessoasNew.equals(pessoasOld)) {
                PessoasFisicas oldPessoasFisicasOfPessoas = pessoasNew.getPessoasFisicas();
                if (oldPessoasFisicasOfPessoas != null) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("The Pessoas " + pessoasNew + " already has an item of type PessoasFisicas whose pessoas column cannot be null. Please make another selection for the pessoas field.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (pessoasNew != null) {
                pessoasNew = em.getReference(pessoasNew.getClass(), pessoasNew.getIdPessoa());
                pessoasFisicas.setPessoas(pessoasNew);
            }
            pessoasFisicas = em.merge(pessoasFisicas);
            if (pessoasOld != null && !pessoasOld.equals(pessoasNew)) {
                pessoasOld.setPessoasFisicas(null);
                pessoasOld = em.merge(pessoasOld);
            }
            if (pessoasNew != null && !pessoasNew.equals(pessoasOld)) {
                pessoasNew.setPessoasFisicas(pessoasFisicas);
                pessoasNew = em.merge(pessoasNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = pessoasFisicas.getIdPFisica();
                if (findPessoasFisicas(id) == null) {
                    throw new NonexistentEntityException("The pessoasFisicas with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            PessoasFisicas pessoasFisicas;
            try {
                pessoasFisicas = em.getReference(PessoasFisicas.class, id);
                pessoasFisicas.getIdPFisica();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The pessoasFisicas with id " + id + " no longer exists.", enfe);
            }
            Pessoas pessoas = pessoasFisicas.getPessoas();
            if (pessoas != null) {
                pessoas.setPessoasFisicas(null);
                pessoas = em.merge(pessoas);
            }
            em.remove(pessoasFisicas);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<PessoasFisicas> findPessoasFisicasEntities() {
        return findPessoasFisicasEntities(true, -1, -1);
    }

    public List<PessoasFisicas> findPessoasFisicasEntities(int maxResults, int firstResult) {
        return findPessoasFisicasEntities(false, maxResults, firstResult);
    }

    private List<PessoasFisicas> findPessoasFisicasEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(PessoasFisicas.class));
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

    public PessoasFisicas findPessoasFisicas(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(PessoasFisicas.class, id);
        } finally {
            em.close();
        }
    }

    public int getPessoasFisicasCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<PessoasFisicas> rt = cq.from(PessoasFisicas.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
