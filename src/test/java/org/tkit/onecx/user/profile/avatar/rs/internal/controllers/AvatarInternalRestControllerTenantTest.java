package org.tkit.onecx.user.profile.avatar.rs.internal.controllers;

import static io.restassured.RestAssured.given;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;
import static jakarta.ws.rs.core.Response.Status.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.Objects;

import jakarta.ws.rs.core.HttpHeaders;

import org.junit.jupiter.api.Test;
import org.tkit.onecx.user.profile.avatar.test.AbstractTest;
import org.tkit.quarkus.security.test.GenerateKeycloakClient;
import org.tkit.quarkus.test.WithDBData;

import gen.org.tkit.onecx.user.profile.avatar.rs.internal.model.ImageInfoDTO;
import gen.org.tkit.onecx.user.profile.avatar.rs.internal.model.RefTypeDTO;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@TestHTTPEndpoint(AvatarInternalRestController.class)
@WithDBData(value = "data/testdata.xml", deleteBeforeInsert = true, deleteAfterTest = true, rinseAndRepeat = true)
@GenerateKeycloakClient(clientName = "testClient", scopes = { "ocx-up:read", "ocx-up:write", "ocx-up:delete", "ocx-up:all" })
class AvatarInternalRestControllerTenantTest extends AbstractTest {

    private static final String MEDIA_TYPE_IMAGE_JPG = "image/jpg";

    private static final File SMALL = new File(
            Objects.requireNonNull(AvatarInternalRestControllerTenantTest.class.getResource("/data/avatar_small.jpg"))
                    .getFile());
    private static final File FILE = new File(
            Objects.requireNonNull(AvatarInternalRestControllerTenantTest.class.getResource("/data/avatar_test.jpg"))
                    .getFile());

    @Test
    void uploadImage() {
        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", "user4")
                .queryParam("refType", RefTypeDTO.MEDIUM.toString())
                .when()
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .body(SMALL)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", "user4")
                .queryParam("refType", RefTypeDTO.MEDIUM.toString())
                .when()
                .header(APM_HEADER_PARAM, createToken("user2", "org2"))
                .body(SMALL)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(CREATED.getStatusCode())
                .extract()
                .body().as(ImageInfoDTO.class);
    }

    @Test
    void getImageJpgTest() {

        var userId = "user2";
        var refType = RefTypeDTO.MEDIUM;

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", userId)
                .queryParam("refType", refType)
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("{userId}")
                .then()
                .statusCode(CREATED.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user2", "org2"))
                .pathParam("userId", userId)
                .get("{userId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

    }

    @Test
    void getMyImageJpgTest() {

        var refType = RefTypeDTO.MEDIUM;

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .queryParam("refType", refType)
                .when()
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .post("me")
                .then()
                .statusCode(CREATED.getStatusCode());

        var data = given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user2", "org1"))
                .get("me")
                .then()
                .statusCode(OK.getStatusCode())
                .header(HttpHeaders.CONTENT_TYPE, MEDIA_TYPE_IMAGE_JPG)
                .extract().body().asByteArray();

        assertThat(data).isNotNull().isNotEmpty();

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .contentType(APPLICATION_JSON)
                .header(APM_HEADER_PARAM, createToken("user2", "org2"))
                .get("me")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void deleteImage() {
        var userId = "user1";

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", userId)
                .queryParam("refType", RefTypeDTO.SMALL)
                .header(APM_HEADER_PARAM, createToken("user1", "org2"))
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .delete("{userId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .pathParam("userId", userId)
                .queryParam("refType", RefTypeDTO.SMALL)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .when()
                .body(FILE)
                .contentType(MEDIA_TYPE_IMAGE_JPG)
                .delete("{userId}")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }

    @Test
    void deleteMyImage() {

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .queryParam("refType", RefTypeDTO.SMALL)
                .header(APM_HEADER_PARAM, createToken("user1", "org2"))
                .when()
                .delete("me")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());

        given()
                .auth().oauth2(getKeycloakClientToken("testClient"))
                .queryParam("refType", RefTypeDTO.SMALL)
                .header(APM_HEADER_PARAM, createToken("user1", "org1"))
                .when()
                .delete("me")
                .then()
                .statusCode(NO_CONTENT.getStatusCode());
    }
}
