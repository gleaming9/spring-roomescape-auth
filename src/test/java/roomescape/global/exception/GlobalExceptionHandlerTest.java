package roomescape.global.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler exceptionHandler = new GlobalExceptionHandler();

    @Test
    @DisplayName("인가 실패 예외는 403 응답으로 변환한다.")
    void handleForbiddenException_returnsForbidden() {
        ResponseEntity<ProblemDetail> response = exceptionHandler.handleForbiddenException(
                new ForbiddenException("접근 권한이 없습니다.")
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetail()).isEqualTo("접근 권한이 없습니다.");
    }
}
