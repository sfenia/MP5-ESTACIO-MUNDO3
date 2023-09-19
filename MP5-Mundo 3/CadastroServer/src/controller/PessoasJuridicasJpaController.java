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
import model.PessoasJuridicas;

/**
 *
 * @author leosc
 */
public class PessoasJuridicasJpaController implements Serializable {

    public PessoasJuridicasJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(PessoasJuridicas pessoasJuridicas) throws IllegalOrphanException, PreexistingEntityException, Exception {
        List<String> illegalOrphanMessages = null;
        Pessoas pessoasOrphanCheck = pessoasJuridicas.getPessoas();
        if (pessoasOrphanCheck != null) {
            PessoasJuridicas oldPessoasJuridicasOfPessoas = pessoasOrphanCheck.getPessoasJuridicas();
            if (oldPessoasJuridicasOfPessoas != null) {
                if (illegalOrphanMessages == null) {
                    illegalOrphanMessages = new ArrayList<String>();
                }
                illegalOrphanMessages.add("The Pessoas " + pessoasOrphanCheck + " already has an item of type PessoasJuridicas whose pessoas column cannot be null. Please make another selection for the pessoas field.");
            }
        }
        if (illegalOrphanMessages != null) {
            throw new IllegalOrphanException(illegalOrphanMessages);
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Pessoas pessoas = pessoasJuridicas.getPessoas();
            if (pessoas != null) {
                pessoas = em.getReference(pessoas.getClass(), pessoas.getIdPessoa());
                pessoasJuridicas.setPessoas(pessoas);
            }
            em.persist(pessoasJuridicas);
            if (pessoas != null) {
                pessoas.setPessoasJuridicas(pessoasJuridicas);
                pessoas = em.merge(pessoas);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findPessoasJuridicas(pessoasJuridicas.getIdPJuridica()) != null) {
                throw new PreexistingEntityException("PessoasJuridicas " + pessoasJuridicas + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(PessoasJuridicas pessoasJuridicas) throws IllegalOrphanException, NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            PessoasJuridicas persistentPessoasJuridicas = em.find(PessoasJuridicas.class, pessoasJuridicas.getIdPJuridica());
            Pessoas pessoasOld = persistentPessoasJuridicas.getPessoas();
            Pessoas pessoasNew = pessoasJuridicas.getPessoas();
            List<String> illegalOrphanMessages = null;
            if (pessoasNew != null && !pessoasNew.equals(pessoasOld)) {
                PessoasJuridicas oldPessoasJuridicasOfPessoas = pessoasNew.getPessoasJuridicas();
                if (oldPessoasJuridicasOfPessoas != null) {
                    if (illegalOrphanMessages == null) {
                        illegalOrphanMessages = new ArrayList<String>();
                    }
                    illegalOrphanMessages.add("The Pessoas " + pessoasNew + " already has an item of type PessoasJuridicas whose pessoas column cannot be null. Please make another selection for the pessoas field.");
                }
            }
            if (illegalOrphanMessages != null) {
                throw new IllegalOrphanException(illegalOrphanMessages);
            }
            if (pessoasNew != null) {
                pessoasNew = em.getReference(pessoasNew.getClass(), pessoasNew.getIdPessoa());
                pessoasJuridicas.setPessoas(pessoasNew);
            }
            pessoasJuridicas = em.merge(pessoasJuridicas);
            if (pessoasOld != null && !pessoasOld.equals(pessoasNew)) {
                pessoasOld.setPessoasJuridicas(null);
                pessoasOld = em.merge(pessoasOld);
            }
            if (pessoasNew != null && !pessoasNew.equals(pessoasOld)) {
                pessoasNew.setPessoasJuridicas(pessoasJuridicas);
                pessoasNew = em.merge(pessoasNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = pessoasJuridicas.getIdPJuridica();
                if (findPessoasJuridicas(id) == null) {
                    throw new NonexistentEntityException("The pessoasJuridicas with id " + id + " no longer exists.");
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
            PessoasJuridicas pessoasJuridicas;
            try {
                pessoasJuridicas = em.getReference(PessoasJuridicas.class, id);
                pessoasJuridicas.getIdPJuridica();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The pessoasJuridicas with id " + id + " no longer exists.", enfe);
            }
            Pessoas pessoas = pessoasJuridicas.getPessoas();
            if (pessoas != null) {
                pessoas.setPessoasJuridicas(null);
                pessoas = em.merge(pessoas);
            }
            em.remove(pessoasJuridicas);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<PessoasJuridicas> findPessoasJuridicasEntities() {
        return findPessoasJuridicasEntities(true, -1, -1);
    }

    public List<PessoasJuridicas> findPessoasJuridicasEntities(int maxResults, int firstResult) {
        return findPessoasJuridicasEntities(false, maxResults, firstResult);
    }

    private List<PessoasJuridicas> findPessoasJuridicasEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(PessoasJuridicas.class));
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

    public PessoasJuridicas findPessoasJuridicas(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(PessoasJuridicas.class, id);
        } finally {
            em.close();
        }
    }

    public int getPessoasJuridicasCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<PessoasJuridicas> rt = cq.from(PessoasJuridicas.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
