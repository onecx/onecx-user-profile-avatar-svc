package org.tkit.onecx.user.profile.avatar.rs.internal.controllers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.*;

import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.tkit.onecx.user.profile.avatar.domain.daos.AvatarDAO;
import org.tkit.onecx.user.profile.avatar.domain.models.Avatar;
import org.tkit.onecx.user.profile.avatar.rs.internal.mappers.AvatarMapper;
import org.tkit.onecx.user.profile.avatar.rs.internal.mappers.ExceptionMapper;
import org.tkit.quarkus.context.ApplicationContext;
import org.tkit.quarkus.jpa.exceptions.ConstraintException;
import org.tkit.quarkus.log.cdi.LogService;

import gen.org.tkit.onecx.user.profile.avatar.rs.internal.AvatarInternalApi;
import gen.org.tkit.onecx.user.profile.avatar.rs.internal.model.ProblemDetailResponseDTO;
import gen.org.tkit.onecx.user.profile.avatar.rs.internal.model.RefTypeDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@LogService
@ApplicationScoped
@Transactional(value = Transactional.TxType.NOT_SUPPORTED)
public class AvatarInternalRestController implements AvatarInternalApi {

    @Inject
    ExceptionMapper exceptionMapper;

    @Inject
    AvatarDAO avatarDAO;

    @Context
    UriInfo uriInfo;

    @Inject
    AvatarMapper avatarMapper;

    @Context
    HttpHeaders httpHeaders;

    @Override
    public Response deleteImage(String userId) {
        avatarDAO.deleteQueryByRefId(userId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    public Response deleteMyImage() {
        var userId = ApplicationContext.get().getPrincipal();
        avatarDAO.deleteQueryByRefId(userId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Override
    @Transactional
    public Response getImage(String userId, RefTypeDTO refType) {
        Avatar avatar = avatarDAO.findByUserIdAndRefType(userId, refType.toString());
        if (avatar == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(avatar.getImageData(), avatar.getMimeType())
                .header(HttpHeaders.CONTENT_LENGTH, avatar.getLength()).build();
    }

    @Override
    @Transactional
    public Response getMyImage(RefTypeDTO refType) {
        var userId = ApplicationContext.get().getPrincipal();

        return getImage(userId, refType);
    }

    @Override
    public Response updateImage(String userId, RefTypeDTO refType, byte[] body, Integer contentLength) {
        Avatar avatar = avatarDAO.findByUserIdAndRefType(userId, refType.toString());
        if (avatar == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var contentType = httpHeaders.getMediaType();
        contentType = new MediaType(contentType.getType(), contentType.getSubtype());

        avatar.setLength(contentLength);
        avatar.setMimeType(contentType.toString());
        avatar.setImageData(body);

        avatar = avatarDAO.update(avatar);

        return Response.ok(avatarMapper.map(avatar)).build();
    }

    @Override
    public Response updateMyImage(RefTypeDTO refType, byte[] body, Integer contentLength) {
        var userId = ApplicationContext.get().getPrincipal();

        return updateImage(userId, refType, body, contentLength);
    }

    @Override
    public Response uploadImage(Integer contentLength, String userId, RefTypeDTO refType, byte[] body) {
        var contentType = httpHeaders.getMediaType();
        contentType = new MediaType(contentType.getType(), contentType.getSubtype());
        var avatar = avatarMapper.create(userId, refType.toString(), contentType.toString(), contentLength);
        avatar.setLength(contentLength);
        avatar.setImageData(body);
        avatar = avatarDAO.create(avatar);

        var avatarInfoDTO = avatarMapper.map(avatar);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(avatarInfoDTO.getId()).build())
                .entity(avatarInfoDTO)
                .build();
    }

    @Override
    public Response uploadMyImage(Integer contentLength, RefTypeDTO refType, byte[] body) {
        var userId = ApplicationContext.get().getPrincipal();

        return uploadImage(contentLength, userId, refType, body);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> exception(ConstraintException ex) {
        return exceptionMapper.exception(ex);
    }

    @ServerExceptionMapper
    public RestResponse<ProblemDetailResponseDTO> constraint(ConstraintViolationException ex) {
        return exceptionMapper.constraint(ex);
    }
}
