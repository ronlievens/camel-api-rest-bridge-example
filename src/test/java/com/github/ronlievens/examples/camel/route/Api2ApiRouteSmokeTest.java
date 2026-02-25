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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static com.github.ronlievens.examples.camel.route.RestApiBridgeRoute.ROUTE_ID_A;
import static com.github.ronlievens.examples.camel.route.RestApiBridgeRoute.ROUTE_ID_B;
import static com.github.ronlievens.examples.camel.util.AssertUtils.readFileAsStringFromClasspath;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.apache.camel.builder.AdviceWith.adviceWith;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@CamelSpringBootTest
@UseAdviceWith
class Api2ApiRouteSmokeTest {

    public static final String MOCK_ROUTE_A = "mock:" + ROUTE_ID_A;
    public static final String MOCK_ROUTE_B = "mock:" + ROUTE_ID_B;

    private final static String API2API_ENDPOINT_A = "http://localhost:%d/a";
    private final static String API2API_ENDPOINT_B = "http://localhost:%d/b";

    private static String endpointA;
    private static String endpointB;

    @Autowired
    private CamelContext camelContext;

    @LocalServerPort
    private int port;

    @RegisterExtension
    static WireMockExtension wireMockServerA = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

    @RegisterExtension
    static WireMockExtension wireMockServerB = WireMockExtension.newInstance().options(wireMockConfig().dynamicPort()).build();

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        endpointA = API2API_ENDPOINT_A.formatted(wireMockServerA.getPort());
        endpointB = API2API_ENDPOINT_B.formatted(wireMockServerB.getPort());
        registry.add("api2api.endpoint.a", () -> endpointA);
        registry.add("api2api.endpoint.b", () -> endpointB);
    }

    @EndpointInject(MOCK_ROUTE_A)
    MockEndpoint mockEndpointRouteA;

    @EndpointInject(MOCK_ROUTE_B)
    MockEndpoint mockEndpointRouteB;


    @BeforeAll
    static void beforeAll() throws Exception {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    @BeforeEach
    void beforeEach() throws Exception {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        adviceWith(camelContext, ROUTE_ID_A, a -> a.weaveAddLast().to(MOCK_ROUTE_A));
        adviceWith(camelContext, ROUTE_ID_B, a -> a.weaveAddLast().to(MOCK_ROUTE_B));

        camelContext.start();
    }

    @AfterEach
    void afterEach() throws Exception {
        mockEndpointRouteA.reset();
        mockEndpointRouteB.reset();

        wireMockServerA.resetAll();
        wireMockServerB.resetAll();
    }

    @Test
    void testRouteA() throws Exception {
        // GIVEN
        val jsonMessage = readFileAsStringFromClasspath("route-message-a.json");
        mockEndpointRouteA.expectedMessageCount(1);
        mockEndpointRouteB.expectedMessageCount(0);
        wireMockServerA.stubFor(post(urlEqualTo("/a/my-route?test=iets&nog=iets"))
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

        mockEndpointRouteA.assertIsSatisfied();
        mockEndpointRouteB.assertIsSatisfied();
    }

    @Test
    void testRouteB() throws Exception {
        // GIVEN
        val jsonMessage = readFileAsStringFromClasspath("route-message-b.json");
        mockEndpointRouteA.expectedMessageCount(0);
        mockEndpointRouteB.expectedMessageCount(1);
        wireMockServerB.stubFor(post(urlEqualTo("/b"))
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

        mockEndpointRouteA.assertIsSatisfied();
        mockEndpointRouteB.assertIsSatisfied();
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

        mockEndpointRouteA.assertIsSatisfied();
        mockEndpointRouteB.assertIsSatisfied();
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

        mockEndpointRouteA.assertIsSatisfied();
        mockEndpointRouteB.assertIsSatisfied();
    }
}
