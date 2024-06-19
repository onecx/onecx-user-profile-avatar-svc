package org.tkit.onecx.user.profile.avatar.domain.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.TenantId;
import org.tkit.quarkus.jpa.models.TraceableEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "AVATAR", uniqueConstraints = {
        @UniqueConstraint(name = "AVATAR_CONSTRAINTS", columnNames = { "USER_ID", "REF_TYPE", "TENANT_ID" })
})
@SuppressWarnings("java:S2160")
public class Avatar extends TraceableEntity {

    @Column(name = "MIME_TYPE")
    private String mimeType;

    @Column(name = "REF_TYPE")
    private String refType;

    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "DATA_LENGTH")
    private Integer length;

    @Column(name = "DATA")
    private byte[] imageData;

    @TenantId
    @Column(name = "TENANT_ID")
    private String tenantId;

}
