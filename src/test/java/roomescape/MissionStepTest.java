package roomescape;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.auth.support.PasswordEncoder;
import roomescape.reservation.controller.ReservationController;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class MissionStepTest {
    private static final int DEFAULT_STORE_ID = 1;

    @Autowired
    private ReservationController reservationController;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO store (id, name) VALUES (?, ?), (?, ?)",
                DEFAULT_STORE_ID,
                "우테코 강남점",
                2,
                "우테코 잠실점"
        );
    }

    @Test
    @DisplayName("로그인하지 않은 사용자는 예약을 생성할 수 없다.")
    void createReservation_withoutLogin_returnsUnauthorized() {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("storeId", DEFAULT_STORE_ID);
        reservation.put("date", LocalDate.now().plusDays(1).toString());
        reservation.put("timeId", 1);
        reservation.put("themeId", 1);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("이메일 또는 비밀번호가 일치하지 않으면 로그인할 수 없다.")
    void login_fail_whenInvalidCredentials() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", "unknown@example.com");
        loginRequest.put("password", "wrong-password");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("로그인에 성공하면 세션 쿠키와 현재 회원 정보를 반환한다.")
    void login_success_returnsMemberAndSessionCookie() {
        AuthenticatedMember member = saveMember("브라운");

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", member.email());
        loginRequest.put("password", "password");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .cookie("JSESSIONID", notNullValue())
                .body("id", is(member.id().intValue()))
                .body("email", is(member.email()))
                .body("name", is(member.name()));
    }

    @Test
    @DisplayName("기존 세션이 있는 상태에서 로그인하면 기존 세션을 무효화하고 새 세션을 발급한다.")
    void login_withExistingSession_invalidatesOldSessionAndCreatesNewSession() {
        AuthenticatedMember member = createMemberAndLogin("브라운");

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", member.email());
        loginRequest.put("password", "password");

        String newSessionId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", member.sessionId())
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("JSESSIONID");

        assertThat(newSessionId).isNotEqualTo(member.sessionId());

        RestAssured.given().log().all()
                .cookie("JSESSIONID", member.sessionId())
                .when().get("/members/me")
                .then().log().all()
                .statusCode(401);

        RestAssured.given().log().all()
                .cookie("JSESSIONID", newSessionId)
                .when().get("/members/me")
                .then().log().all()
                .statusCode(200);
    }

    @Test
    @DisplayName("로그인한 사용자는 현재 회원 정보를 조회할 수 있다.")
    void findCurrentMember_withLogin_returnsLoginMember() {
        AuthenticatedMember member = createMemberAndLogin("브라운");

        RestAssured.given().log().all()
                .cookie("JSESSIONID", member.sessionId())
                .when().get("/members/me")
                .then().log().all()
                .statusCode(200)
                .body("id", is(member.id().intValue()))
                .body("email", is(member.email()))
                .body("name", is(member.name()));
    }

    @Test
    @DisplayName("로그인하지 않은 사용자는 현재 회원 정보를 조회할 수 없다.")
    void findCurrentMember_withoutLogin_returnsUnauthorized() {
        RestAssured.given().log().all()
                .when().get("/members/me")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("모바일 앱은 로그인 응답의 세션 쿠키를 직접 전달해 현재 회원 정보를 조회할 수 있다.")
    void findCurrentMember_mobileRequestWithSessionCookie_returnsLoginMember() {
        AuthenticatedMember member = saveMember("브라운");
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", member.email());
        loginRequest.put("password", "password");

        String sessionId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("JSESSIONID");

        RestAssured.given().log().all()
                .header("Cookie", "JSESSIONID=" + sessionId)
                .when().get("/members/me")
                .then().log().all()
                .statusCode(200)
                .body("id", is(member.id().intValue()))
                .body("email", is(member.email()))
                .body("name", is(member.name()));
    }

    @Test
    @DisplayName("모바일 앱 요청에 세션 쿠키가 없으면 현재 회원 정보를 조회할 수 없다.")
    void findCurrentMember_mobileRequestWithoutSessionCookie_returnsUnauthorized() {
        RestAssured.given().log().all()
                .when().get("/members/me")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("모바일 앱 요청의 세션 쿠키가 유효하지 않으면 현재 회원 정보를 조회할 수 없다.")
    void findCurrentMember_mobileRequestWithInvalidSessionCookie_returnsUnauthorized() {
        RestAssured.given().log().all()
                .header("Cookie", "JSESSIONID=invalid-session-id")
                .when().get("/members/me")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("모바일 앱은 세션 쿠키를 직접 전달해 예약을 생성하고 내 예약을 조회할 수 있다.")
    void reservationApis_mobileRequestWithSessionCookie_createAndFindMyReservations() {
        AuthenticatedMember brown = createMemberAndLogin("브라운");
        AuthenticatedMember pobi = createMemberAndLogin("포비");
        String date = LocalDate.now().plusDays(1).toString();
        int timeId = createTime("15:00");
        int otherTimeId = createTime("15:30");
        int themeId = createTheme("모바일 예약 테스트");
        createReservation(pobi.sessionId(), date, otherTimeId, themeId);

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("storeId", DEFAULT_STORE_ID);
        reservation.put("date", date);
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        int reservationId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .header("Cookie", "JSESSIONID=" + brown.sessionId())
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("member.id", is(brown.id().intValue()))
                .body("member.name", is(brown.name()))
                .body("time.id", is(timeId))
                .body("theme.id", is(themeId))
                .extract()
                .path("id");

        RestAssured.given().log().all()
                .header("Cookie", "JSESSIONID=" + brown.sessionId())
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].id", is(reservationId))
                .body("reservations[0].member.id", is(brown.id().intValue()))
                .body("reservations[0].member.name", is(brown.name()));
    }

    @Test
    @DisplayName("모바일 앱은 세션 쿠키를 직접 전달해 로그아웃하면 같은 쿠키를 다시 사용할 수 없다.")
    void logout_mobileRequestWithSessionCookie_invalidatesSession() {
        AuthenticatedMember member = createMemberAndLogin("브라운");

        RestAssured.given().log().all()
                .header("Cookie", "JSESSIONID=" + member.sessionId())
                .when().post("/logout")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .header("Cookie", "JSESSIONID=" + member.sessionId())
                .when().get("/members/me")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("로그아웃하면 기존 세션으로 인증 API를 사용할 수 없다.")
    void logout_invalidatesSession() {
        AuthenticatedMember member = createMemberAndLogin("브라운");

        RestAssured.given().log().all()
                .cookie("JSESSIONID", member.sessionId())
                .when().post("/logout")
                .then().log().all()
                .statusCode(204);

        RestAssured.given().log().all()
                .cookie("JSESSIONID", member.sessionId())
                .when().get("/members/me")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("로그인하지 않은 사용자는 내 예약을 조회, 변경, 취소할 수 없다.")
    void reservationApis_withoutLogin_returnUnauthorized() {
        Map<String, Object> reservationUpdate = new HashMap<>();
        reservationUpdate.put("date", LocalDate.now().plusDays(2).toString());
        reservationUpdate.put("timeId", 1);

        RestAssured.given().log().all()
                .when().get("/reservations")
                .then().log().all()
                .statusCode(401);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(reservationUpdate)
                .when().patch("/reservations/1")
                .then().log().all()
                .statusCode(401);

        RestAssured.given().log().all()
                .when().delete("/reservations/1")
                .then().log().all()
                .statusCode(401);
    }

    @Test
    @DisplayName("내 예약 조회는 현재 로그인한 사용자의 예약만 반환한다.")
    void findMyReservations_returnsOnlyLoginMemberReservations() {
        AuthenticatedMember brown = createMemberAndLogin("브라운");
        AuthenticatedMember pobi = createMemberAndLogin("포비");
        String date = LocalDate.now().plusDays(1).toString();
        int timeId = createTime("10:00");
        int otherTimeId = createTime("12:00");
        int themeId = createTheme("내 예약 조회 테스트");

        int brownReservationId = createReservation(brown.sessionId(), date, timeId, themeId);
        createReservation(pobi.sessionId(), date, otherTimeId, themeId);

        RestAssured.given().log().all()
                .cookie("JSESSIONID", brown.sessionId())
                .when().get("/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.size()", is(1))
                .body("reservations[0].id", is(brownReservationId))
                .body("reservations[0].member.id", is(brown.id().intValue()))
                .body("reservations[0].member.name", is(brown.name()));
    }

    @Test
    @DisplayName("다른 사용자의 예약은 변경하거나 취소할 수 없다.")
    void updateOrCancelOtherMemberReservation_returnsNotFound() {
        AuthenticatedMember brown = createMemberAndLogin("브라운");
        AuthenticatedMember pobi = createMemberAndLogin("포비");
        String date = LocalDate.now().plusDays(1).toString();
        int timeId = createTime("10:00");
        int otherTimeId = createTime("12:00");
        int themeId = createTheme("인가 테스트");
        int reservationId = createReservation(brown.sessionId(), date, timeId, themeId);

        Map<String, Object> reservationUpdate = new HashMap<>();
        reservationUpdate.put("date", LocalDate.now().plusDays(2).toString());
        reservationUpdate.put("timeId", otherTimeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", pobi.sessionId())
                .body(reservationUpdate)
                .when().patch("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(404);

        RestAssured.given().log().all()
                .cookie("JSESSIONID", pobi.sessionId())
                .when().delete("/reservations/" + reservationId)
                .then().log().all()
                .statusCode(404);
    }

    @Test
    @DisplayName("시간 관리 API")
    void timeManagementApi() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        int timeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");

        RestAssured.given().log().all()
                .when().get("/times")
                .then().log().all()
                .statusCode(200)
                .body("times.find { it.id == " + timeId + " }.startAt", is("10:00"));

        RestAssured.given().log().all()
                .when().delete("/admin/times/" + timeId)
                .then().log().all()
                .statusCode(204);
    }

    @Test
    @DisplayName("중복된 예약 시간을 추가하면 conflict를 반환한다.")
    void createDuplicatedReservationTime_returnsConflict() {
        Map<String, String> params = new HashMap<>();
        params.put("startAt", "10:00");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(params)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(409);
    }

    @Test
    @DisplayName("예약과 시간을 연결한다.")
    void connectReservationWithTime() {
        Map<String, String> time = new HashMap<>();
        time.put("startAt", "10:00");

        Map<String, String> theme = new HashMap<>();
        theme.put("name", "이름");
        theme.put("description", "내용");
        theme.put("thumbnail", "https://example.com/theme.png");

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("storeId", DEFAULT_STORE_ID);
        reservation.put("date", LocalDate.now().plusDays(1).toString());
        AuthenticatedMember member = createMemberAndLogin("브라운");

        int timeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");

        int themeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");

        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", member.sessionId())
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .body("member.id", is(member.id().intValue()))
                .body("member.name", is("브라운"))
                .body("time.id", is(timeId))
                .body("theme.id", is(themeId));

        RestAssured.given().log().all()
                .when().get("/admin/reservations")
                .then().log().all()
                .statusCode(200)
                .body("reservations.find { it.time.id == " + timeId + " }.theme.id", is(themeId));
    }

    @Test
    @DisplayName("예약 가능 시간 조회 후 예약하면 해당 시간이 예약 불가가 된다.")
    void reserveAfterFindingAvailableTime_makesTimeUnavailable() {
        Map<String, String> time = new HashMap<>();
        time.put("startAt", "23:00");

        Map<String, String> theme = new HashMap<>();
        theme.put("name", "예약 가능 시간 테스트");
        theme.put("description", "예약 가능 시간 테스트용 테마");
        theme.put("thumbnail", "https://example.com/availability-theme.png");
        String date = LocalDate.now().plusDays(1).toString();

        int timeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");

        int themeId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");

        RestAssured.given().log().all()
                .when().get("/times/availability?storeId=" + DEFAULT_STORE_ID + "&date=" + date + "&themeId=" + themeId)
                .then().log().all()
                .statusCode(200)
                .body("availableTimes.find { it.id == " + timeId + " }.isAvailable", is(true));

        Map<String, Object> reservation = new HashMap<>();
        reservation.put("storeId", DEFAULT_STORE_ID);
        reservation.put("date", date);
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);
        AuthenticatedMember member = createMemberAndLogin("브라운");

        RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", member.sessionId())
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201);

        RestAssured.given().log().all()
                .when().get("/times/availability?storeId=" + DEFAULT_STORE_ID + "&date=" + date + "&themeId=" + themeId)
                .then().log().all()
                .statusCode(200)
                .body("availableTimes.find { it.id == " + timeId + " }.isAvailable", is(false));
    }

    @Test
    @DisplayName("컨트롤러는 JdbcTemplate을 직접 의존하지 않는다.")
    void controllerDoesNotDependOnJdbcTemplate() {
        boolean isJdbcTemplateInjected = false;

        for (Field field : reservationController.getClass().getDeclaredFields()) {
            if (field.getType().equals(JdbcTemplate.class)) {
                isJdbcTemplateInjected = true;
                break;
            }
        }

        assertThat(isJdbcTemplateInjected).isFalse();
    }

    private AuthenticatedMember createMemberAndLogin(String name) {
        AuthenticatedMember member = saveMember(name);

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", member.email());
        loginRequest.put("password", "password");

        String sessionId = RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when().post("/login")
                .then().log().all()
                .statusCode(200)
                .extract()
                .cookie("JSESSIONID");

        return new AuthenticatedMember(member.id(), member.email(), member.name(), sessionId);
    }

    private AuthenticatedMember saveMember(String name) {
        String email = UUID.randomUUID() + "@example.com";
        String encodedPassword = passwordEncoder.encode("password");
        jdbcTemplate.update(
                "INSERT INTO member (email, password, name) VALUES (?, ?, ?)",
                email,
                encodedPassword,
                name
        );

        Long memberId = jdbcTemplate.queryForObject(
                "SELECT id FROM member WHERE email = ?",
                Long.class,
                email
        );

        return new AuthenticatedMember(memberId, email, name, null);
    }

    private int createTime(String startAt) {
        Map<String, String> time = new HashMap<>();
        time.put("startAt", startAt);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(time)
                .when().post("/admin/times")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");
    }

    private int createTheme(String name) {
        Map<String, String> theme = new HashMap<>();
        theme.put("name", name);
        theme.put("description", name + " 설명");
        theme.put("thumbnail", "https://example.com/theme.png");

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .body(theme)
                .when().post("/admin/themes")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");
    }

    private int createReservation(String sessionId, String date, int timeId, int themeId) {
        Map<String, Object> reservation = new HashMap<>();
        reservation.put("storeId", DEFAULT_STORE_ID);
        reservation.put("date", date);
        reservation.put("timeId", timeId);
        reservation.put("themeId", themeId);

        return RestAssured.given().log().all()
                .contentType(ContentType.JSON)
                .cookie("JSESSIONID", sessionId)
                .body(reservation)
                .when().post("/reservations")
                .then().log().all()
                .statusCode(201)
                .extract()
                .path("id");
    }

    private record AuthenticatedMember(
            Long id,
            String email,
            String name,
            String sessionId
    ) {
    }
}
