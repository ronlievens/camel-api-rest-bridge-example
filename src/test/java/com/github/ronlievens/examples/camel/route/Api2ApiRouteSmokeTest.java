package com.github.ronlievens.examples.camel.route;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.restassured.RestAssured;
import lombok.val;
import org.apache.camel.CamelContext;
import org.apache.camel.EndpointInject;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.spring.junit5.CamelSpringBootTest;
import org.apache.camel.test.spring.junit5.UseAdviceWith;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.ronlievens.examples.camel.util.AssertUtils.readFileAsStringFromClasspath;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@CamelSpringBootTest
@UseAdviceWith
class Api2ApiRouteSmokeTest {

    private final static String API2API_ENDPOINT_A = "http://localhost:%d/a";
    private final static String API2API_ENDPOINT_B = "http://localhost:%d/b";

    private static String endpointA;
    private static String endpointB;

    @Autowired
    private CamelContext camelContext;

    @LocalServerPort
    private int port;

    @RegisterExtension
    static WireMockExtension wireMockServer = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        endpointA = API2API_ENDPOINT_A.formatted(wireMockServer.getPort());
        endpointB = API2API_ENDPOINT_B.formatted(wireMockServer.getPort());
        registry.add("api2api.endpoint.a", () -> endpointA);
        registry.add("api2api.endpoint.b", () -> endpointB);
    }

    @EndpointInject("mock:routeA")
    MockEndpoint mockEndpointRouteA;

    @EndpointInject("mock:routeB")
    MockEndpoint mockEndpointRouteB;


    @BeforeAll
    static void beforeAll() throws Exception {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    void beforeEach() throws Exception {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        adviceWith(camelContext, "route-A", a -> a.weaveAddLast().to("mock:endpointA"));
        adviceWith(camelContext, "route-B", a -> a.weaveAddLast().to("mock:endpointB"));

        camelContext.start();
    }

    @AfterEach
    void afterEach() throws Exception {
        mockEndpointRouteA.reset();
        mockEndpointRouteB.reset();

        wireMockServer.resetAll();
    }

    @Test
    void testRouteA() throws Exception {
        // GIVEN
        val jsonMessage = readFileAsStringFromClasspath("route-message-a.json");
        mockEndpointRouteA.expectedMessageCount(1);
        mockEndpointRouteB.expectedMessageCount(0);
        wireMockServer.stubFor(post(urlEqualTo("/a/my-route?test=iets&nog=iets"))
            .willReturn(
                ok()
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonMessage)

            ));

        // WHEN
        val response = RestAssured.given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(jsonMessage)
            .log().all()
            .when()
            .post("/my-route?test=iets&nog=iets")
            .then()
            .log().all()
            .extract()
            .response();

        // THEN
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.asString()).isNotBlank();
        JSONAssert.assertEquals(response.asString(), jsonMessage, true);
    }

    @Test
    void testRouteB() throws Exception {
        // GIVEN
        val jsonMessage = readFileAsStringFromClasspath("route-message-b.json");
        mockEndpointRouteA.expectedMessageCount(0);
        mockEndpointRouteB.expectedMessageCount(1);
        wireMockServer.stubFor(post(urlEqualTo("/b"))
            .willReturn(
                ok()
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonMessage)

            ));

        // WHEN
        val response = RestAssured.given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(jsonMessage)
            .log().all()
            .when()
            .post("/my-route")
            .then()
            .log().all()
            .extract()
            .response();

        // THEN
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.asString()).isNotBlank();
        JSONAssert.assertEquals(response.asString(), jsonMessage, true);
    }

    @Test
    void testRouteX() throws Exception {
        // GIVEN
        val jsonMessage = readFileAsStringFromClasspath("route-message-x.json");
        mockEndpointRouteA.expectedMessageCount(0);
        mockEndpointRouteB.expectedMessageCount(0);

        // WHEN
        val response = RestAssured.given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(jsonMessage)
            .log().all()
            .when()
            .post("/my-route")
            .then()
            .log().all()
            .extract()
            .response();

        // THEN
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.asString()).isBlank();
    }

    @Test
    void testRouteNonsense() throws Exception {
        // GIVEN
        val jsonMessage = readFileAsStringFromClasspath("route-message-nonsense.json");
        mockEndpointRouteA.expectedMessageCount(0);
        mockEndpointRouteB.expectedMessageCount(0);

        // WHEN
        val response = RestAssured.given()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .body(jsonMessage)
            .log().all()
            .when()
            .post("/my-route")
            .then()
            .log().all()
            .extract()
            .response();

        // THEN
        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.asString()).isBlank();
    }
}
