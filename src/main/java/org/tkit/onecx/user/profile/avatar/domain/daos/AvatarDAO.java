package org.tkit.onecx.user.profile.avatar.domain.daos;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

import org.tkit.onecx.user.profile.avatar.domain.models.Avatar;
import org.tkit.onecx.user.profile.avatar.domain.models.Avatar_;
import org.tkit.quarkus.jpa.daos.AbstractDAO;
import org.tkit.quarkus.jpa.exceptions.DAOException;

@ApplicationScoped
@Transactional
public class AvatarDAO extends AbstractDAO<Avatar> {

    public Avatar findByUserIdAndRefType(String userId, String refType) {
        try {
            var cb = this.getEntityManager().getCriteriaBuilder();
            var cq = this.criteriaQuery();
            var root = cq.from(Avatar.class);

            cq.where(cb.and(cb.equal(root.get(Avatar_.USER_ID), userId),
                    cb.equal(root.get(Avatar_.REF_TYPE), refType)));

            return this.getEntityManager().createQuery(cq).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        } catch (Exception ex) {
            throw new DAOException(AvatarDAO.ErrorKeys.FIND_ENTITY_BY_REF_ID_REF_TYPE_FAILED, ex);
        }
    }

    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = DAOException.class)
    public void deleteQueryByRefId(String userId) throws DAOException {
        try {
            var cq = deleteQuery();
            var root = cq.from(Avatar.class);
            var cb = this.getEntityManager().getCriteriaBuilder();

            cq.where(cb.equal(root.get(Avatar_.USER_ID), userId));
            getEntityManager().createQuery(cq).executeUpdate();
            getEntityManager().flush();
        } catch (Exception e) {
            throw handleConstraint(e, ErrorKeys.FAILED_TO_DELETE_BY_REF_ID_QUERY);
        }
    }

    public enum ErrorKeys {

        FAILED_TO_DELETE_BY_REF_ID_QUERY,

        FIND_ENTITY_BY_REF_ID_REF_TYPE_FAILED
    }
}
