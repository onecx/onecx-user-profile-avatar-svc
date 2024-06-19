package org.tkit.onecx.user.profile.avatar.rs.external.v1.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;

import org.tkit.onecx.user.profile.avatar.domain.daos.AvatarDAO;
import org.tkit.onecx.user.profile.avatar.domain.models.Avatar;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.user.profile.avatar.rs.external.v1.AvatarExternalV1Api;
import gen.org.tkit.onecx.user.profile.avatar.rs.external.v1.model.RefTypeDTOV1;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class AvatarExternalV1RestController implements AvatarExternalV1Api {

    @Inject
    AvatarDAO avatarDAO;

    @Override
    @Transactional
    public Response getImage(String userId, RefTypeDTOV1 refType) {
        Avatar avatar = avatarDAO.findByUserIdAndRefType(userId, refType.toString());
        if (avatar == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(avatar.getImageData(), avatar.getMimeType())
                .header(HttpHeaders.CONTENT_LENGTH, avatar.getLength()).build();
    }

    @Override
    @Transactional
    public Response getMyImage(RefTypeDTOV1 refType) {
        var userId = ApplicationContext.get().getPrincipal();

        return getImage(userId, refType);
    }
}
