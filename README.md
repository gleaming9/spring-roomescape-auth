# 방탈출 예약 관리

## 미션 범위

이 프로젝트는 `roomescape-member`에서 구현한 방탈출 예약 서비스에 인증 흐름을 붙이는 미션이다.

1단계에서는 웹 브라우저 사용자를 대상으로 세션 기반 로그인을 적용한다. 기존에는 요청으로 전달된 예약자 이름을 신뢰해 예약 생성, 조회, 변경, 취소를 처리했지만, 1단계부터는 로그인한 회원을 기준으로 사용자 예약 기능을 처리한다. Spring Security는 사용하지 않고 인증 흐름을 직접 구현한다.

이번 단계의 핵심 결정은 다음과 같다.

| 항목 | 선택 |
| --- | --- |
| 로그인 상태 유지 방식 | `HttpSession` |
| 웹 인증 정보 전달 방식 | 브라우저 쿠키의 `JSESSIONID` |
| 세션 저장 정보 | 로그인한 회원의 id |
| 인증 공통 처리 | `Interceptor`에서 인증 필요 요청 차단 |
| 로그인 사용자 전달 | `ArgumentResolver`로 컨트롤러 파라미터에 전달 |
| 예약 소유자 판단 | 요청의 이름이 아니라 로그인 회원 id |

2단계 모바일 인증에서는 토큰 기반 인증 수단을 추가할 수 있다. 이를 고려해 컨트롤러와 예약 서비스는 세션을 직접 다루지 않고, 공통 인증 처리 계층을 통해 현재 로그인 회원을 전달받는 구조를 목표로 한다.

## 기능 요구사항

### 인증

- 회원은 이메일과 비밀번호로 로그인할 수 있다.
- 로그인에 성공하면 서버는 세션에 로그인 회원 id를 저장한다.
- 브라우저는 이후 요청마다 세션 쿠키를 함께 보내고, 서버는 세션으로 현재 회원을 식별한다.
- 로그인에 실패하면 `401 Unauthorized`를 반환한다.
- 회원은 로그아웃할 수 있다.
- 로그아웃하면 기존 세션은 더 이상 로그인 상태로 사용할 수 없다.
- 현재 로그인한 회원 정보를 조회할 수 있다.

### 사용자 예약

- 사용자는 로그인 후 예약을 생성할 수 있다.
- 예약 생성 시 요청 본문의 이름이 아니라 로그인 회원 정보를 기준으로 예약자를 결정한다.
- 사용자는 로그인 후 자신의 예약 목록을 조회할 수 있다.
- 사용자는 로그인 후 자신의 예약 날짜와 시간을 변경할 수 있다.
- 사용자는 로그인 후 자신의 예약을 취소할 수 있다.
- 로그인하지 않은 사용자는 예약 생성, 내 예약 조회, 내 예약 변경, 내 예약 취소 기능을 사용할 수 없다.
- 다른 회원의 예약을 변경하거나 취소할 수 없다.

### 예약 정책

- 지나간 날짜와 시간에 대한 예약은 생성할 수 없다.
- 같은 날짜, 시간, 테마에 이미 예약이 존재하면 예약을 생성할 수 없다.
- 예약이 존재하는 예약 시간은 삭제할 수 없다.
- 잘못된 날짜/시간 형식, 필수값 누락, 양수가 아닌 id 등 유효하지 않은 입력값은 거부한다.
- 이미 지난 예약은 취소하거나 변경할 수 없다.
- 예약을 변경할 때 같은 날짜, 시간, 테마에 다른 예약이 존재하면 거부한다.
- 예약을 변경할 때 현재 예약과 같은 날짜와 시간으로 변경할 수 없다.

### 예약 시간

- 예약 시간을 등록할 수 있다.
- 전체 예약 시간 목록을 조회할 수 있다.
- 예약 시간을 삭제할 수 있다.
- 사용자는 특정 날짜와 테마의 예약 가능한 시간을 조회할 수 있다.

### 테마

- 테마의 이름, 설명, 썸네일 이미지 URL을 입력해 등록할 수 있다.
- 전체 테마 목록을 조회할 수 있다.
- 테마를 삭제할 수 있다.
- 사용자는 최근 1주일 동안 예약이 많았던 테마 상위 10개를 확인할 수 있다.

### 관리자 기능

- 관리자는 전체 예약 목록을 조회할 수 있다.
- 관리자는 특정 예약을 삭제할 수 있다.
- 관리자 인증/인가와 매장 매니저 권한은 3단계에서 별도로 확장한다.

## 인증 설계

### 세션 기반 웹 로그인

로그인 성공 시 서버는 세션에 로그인 회원 id를 저장한다.

```java
session.setAttribute("loginMemberId", memberId);
```

이후 브라우저는 `JSESSIONID` 쿠키를 자동으로 전달한다. 서버는 이 세션 id로 세션을 찾고, 세션에 저장된 `loginMemberId`로 현재 회원을 식별한다.

세션에는 회원 이름이나 이메일이 아니라 회원 id만 저장한다. 이름은 표시용 정보이며, 예약 소유자 판단에는 사용하지 않는다.

### 인증 필수 API

다음 API는 로그인한 사용자만 사용할 수 있다.

| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/members/me` | 현재 로그인 회원 조회 |
| `POST` | `/reservations` | 예약 생성 |
| `GET` | `/reservations` | 내 예약 조회 |
| `PATCH` | `/reservations/{id}` | 내 예약 변경 |
| `DELETE` | `/reservations/{id}` | 내 예약 취소 |

다음 API는 로그인 없이 사용할 수 있다.

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/login` | 로그인 |
| `POST` | `/logout` | 로그아웃 |
| `GET` | `/themes` | 테마 목록 조회 |
| `GET` | `/themes/popularity` | 인기 테마 조회 |
| `GET` | `/times` | 예약 시간 목록 조회 |
| `GET` | `/times/availability` | 예약 가능 시간 조회 |

### 공통 처리 위치

`Interceptor`는 인증이 필요한 요청이 컨트롤러에 도달하기 전에 로그인 여부를 확인한다. 로그인하지 않은 요청은 `401 Unauthorized`로 차단한다.

`ArgumentResolver`는 인증된 요청에서 현재 로그인 회원을 컨트롤러 파라미터로 전달한다. 컨트롤러는 `HttpSession`, 쿠키, 요청 헤더를 직접 다루지 않는다.

## API 명세

### 공통 에러 응답

에러 응답은 RFC 7807 `ProblemDetail` 형식을 따른다. `detail`에는 사용자가 문제를 이해하고 다음 행동을 판단할 수 있는 메시지를 담는다.

**Response**

```http
Content-Type: application/json
```

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "에러 메시지"
}
```

요청 본문 검증에 실패한 경우에는 `errors` 필드에 잘못된 필드와 사유가 함께 포함된다.

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "입력값이 올바르지 않습니다.",
  "errors": [
    {
      "field": "date",
      "reason": "예약 날짜는 비어 있을 수 없습니다."
    }
  ]
}
```

| statusCode | 상황 |
| --- | --- |
| `400 Bad Request` | 요청 본문, 쿼리 파라미터, path variable의 형식이 잘못된 경우, 필수값이 누락된 경우, 양수가 아닌 id처럼 유효하지 않은 값이 전달된 경우, 지나간 날짜와 시간으로 예약 생성/변경을 요청한 경우, 이미 지난 예약을 취소/변경하려는 경우 |
| `401 Unauthorized` | 로그인하지 않은 사용자가 인증이 필요한 API를 호출한 경우, 로그인 정보가 올바르지 않은 경우, 세션이 만료되었거나 유효하지 않은 경우 |
| `404 Not Found` | 요청에서 조회, 변경, 삭제, 참조한 예약, 예약 시간, 테마 또는 회원이 존재하지 않는 경우, 다른 회원의 예약에 접근한 경우 |
| `405 Method Not Allowed` | 지원하지 않는 HTTP 메서드로 요청한 경우 |
| `409 Conflict` | 이미 존재하는 자원을 생성하려는 경우, 같은 날짜+시간+테마에 예약이 이미 존재하는 경우, 예약이 존재하는 예약 시간을 삭제하려는 경우 |
| `415 Unsupported Media Type` | 지원하지 않는 Content-Type으로 요청한 경우 |
| `500 Internal Server Error` | 서버 내부 오류가 발생한 경우. 내부 오류 상세는 응답에 노출하지 않는다. |

## 인증 API

### 로그인 API

**Request**

```http
POST /login HTTP/1.1
Content-Type: application/json
```

```json
{
  "email": "brown@example.com",
  "password": "password"
}
```

**Response**

```http
HTTP/1.1 200 OK
Set-Cookie: JSESSIONID=...
Content-Type: application/json
```

```json
{
  "id": 1,
  "email": "brown@example.com",
  "name": "브라운"
}
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `200 OK` | 로그인 성공 |
| `400 Bad Request` | 이메일 또는 비밀번호가 비어 있거나 형식이 올바르지 않은 경우 |
| `401 Unauthorized` | 이메일 또는 비밀번호가 일치하지 않는 경우 |

### 로그아웃 API

**Request**

```http
POST /logout HTTP/1.1
Cookie: JSESSIONID=...
```

**Response**

```http
HTTP/1.1 204 No Content
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `204 No Content` | 로그아웃 성공. 기존 세션이 없어도 같은 응답을 반환한다. |

### 현재 로그인 회원 조회 API

**Request**

```http
GET /members/me HTTP/1.1
Cookie: JSESSIONID=...
```

**Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "id": 1,
  "email": "brown@example.com",
  "name": "브라운"
}
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `200 OK` | 현재 로그인 회원 조회 성공 |
| `401 Unauthorized` | 로그인하지 않았거나 세션이 유효하지 않은 경우 |

## 예약 API

### 예약 추가 API

예약자 이름은 요청으로 받지 않는다. 서버는 현재 로그인한 회원을 예약자로 사용한다.

**Request**

```http
POST /reservations HTTP/1.1
Content-Type: application/json
Cookie: JSESSIONID=...
```

```json
{
  "date": "2026-05-20",
  "timeId": 1,
  "themeId": 1
}
```

**Response**

```http
HTTP/1.1 201 Created
Content-Type: application/json
```

```json
{
  "id": 1,
  "date": "2026-05-20",
  "member": {
    "id": 1,
    "email": "brown@example.com",
    "name": "브라운"
  },
  "time": {
    "id": 1,
    "startAt": "10:00"
  },
  "theme": {
    "id": 1,
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://example.com/theme.png"
  }
}
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `201 Created` | 예약 생성 성공 |
| `400 Bad Request` | 날짜 형식이 `yyyy-MM-dd`가 아닌 경우, 필수값이 누락된 경우, `timeId` 또는 `themeId`가 양수가 아닌 경우, 지나간 날짜와 시간에 대한 예약인 경우 |
| `401 Unauthorized` | 로그인하지 않은 사용자가 예약 생성을 요청한 경우 |
| `404 Not Found` | `timeId` 또는 `themeId`에 해당하는 예약 시간 또는 테마가 존재하지 않는 경우 |
| `409 Conflict` | 같은 날짜, 시간, 테마에 이미 예약이 존재하는 경우 |

### 내 예약 조회 API

현재 로그인한 회원의 예약 목록만 조회한다.

**Request**

```http
GET /reservations HTTP/1.1
Cookie: JSESSIONID=...
```

**Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "reservations": [
    {
      "id": 1,
      "date": "2026-05-20",
      "member": {
        "id": 1,
        "email": "brown@example.com",
        "name": "브라운"
      },
      "time": {
        "id": 1,
        "startAt": "10:00"
      },
      "theme": {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://example.com/theme.png"
      }
    }
  ]
}
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `200 OK` | 현재 로그인 회원의 예약 목록 조회 성공 |
| `401 Unauthorized` | 로그인하지 않은 사용자가 예약 목록 조회를 요청한 경우 |

### 내 예약 변경 API

현재 로그인한 회원의 예약만 변경할 수 있다.

**Request**

```http
PATCH /reservations/1 HTTP/1.1
Content-Type: application/json
Cookie: JSESSIONID=...
```

```json
{
  "date": "2026-05-21",
  "timeId": 2
}
```

**Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "id": 1,
  "date": "2026-05-21",
  "member": {
    "id": 1,
    "email": "brown@example.com",
    "name": "브라운"
  },
  "time": {
    "id": 2,
    "startAt": "11:00"
  },
  "theme": {
    "id": 1,
    "name": "레벨2 탈출",
    "description": "우테코 레벨2를 탈출하는 내용입니다.",
    "thumbnail": "https://example.com/theme.png"
  }
}
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `200 OK` | 본인 예약의 날짜와 시간 변경 성공 |
| `400 Bad Request` | 예약 id 또는 `timeId`가 양수가 아닌 경우, 날짜 형식이 `yyyy-MM-dd`가 아닌 경우, 필수값이 누락된 경우, 이미 지난 예약을 변경하려는 경우, 지나간 날짜와 시간으로 변경하려는 경우, 현재 예약과 같은 날짜와 시간으로 변경하려는 경우 |
| `401 Unauthorized` | 로그인하지 않은 사용자가 예약 변경을 요청한 경우 |
| `404 Not Found` | 예약이 존재하지 않거나 현재 로그인 회원의 예약이 아닌 경우, `timeId`에 해당하는 예약 시간이 존재하지 않는 경우 |
| `409 Conflict` | 변경하려는 날짜, 시간, 테마에 이미 예약이 존재하는 경우 |

### 내 예약 취소 API

현재 로그인한 회원의 예약만 취소할 수 있다.

**Request**

```http
DELETE /reservations/1 HTTP/1.1
Cookie: JSESSIONID=...
```

**Response**

```http
HTTP/1.1 204 No Content
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `204 No Content` | 본인 예약 취소 성공 |
| `400 Bad Request` | 예약 id가 양수가 아닌 경우, 이미 지난 예약을 취소하려는 경우 |
| `401 Unauthorized` | 로그인하지 않은 사용자가 예약 취소를 요청한 경우 |
| `404 Not Found` | 예약이 존재하지 않거나 현재 로그인 회원의 예약이 아닌 경우 |

### 관리자 예약 조회 API

**Request**

```http
GET /admin/reservations HTTP/1.1
```

**Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "reservations": [
    {
      "id": 1,
      "date": "2026-05-20",
      "member": {
        "id": 1,
        "email": "brown@example.com",
        "name": "브라운"
      },
      "time": {
        "id": 1,
        "startAt": "10:00"
      },
      "theme": {
        "id": 1,
        "name": "레벨2 탈출",
        "description": "우테코 레벨2를 탈출하는 내용입니다.",
        "thumbnail": "https://example.com/theme.png"
      }
    }
  ]
}
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `200 OK` | 관리자 예약 목록 조회 성공 |

### 관리자 예약 삭제 API

**Request**

```http
DELETE /admin/reservations/1 HTTP/1.1
```

**Response**

```http
HTTP/1.1 204 No Content
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `204 No Content` | 예약 삭제 성공 |
| `400 Bad Request` | 예약 id가 양수가 아닌 경우 |
| `404 Not Found` | 삭제하려는 예약이 존재하지 않는 경우 |

## 시간 API

### 시간 추가 API

**Request**

```http
POST /admin/times HTTP/1.1
Content-Type: application/json
```

```json
{
  "startAt": "10:00"
}
```

**Response**

```http
HTTP/1.1 201 Created
Content-Type: application/json
```

```json
{
  "id": 1,
  "startAt": "10:00"
}
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `201 Created` | 예약 시간 생성 성공 |
| `400 Bad Request` | 예약 시간이 비어 있거나 시간 형식이 `HH:mm`이 아닌 경우 |
| `409 Conflict` | 같은 시작 시간의 예약 시간이 이미 존재하는 경우 |

### 시간 조회 API

**Request**

```http
GET /times HTTP/1.1
```

**Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "times": [
    {
      "id": 1,
      "startAt": "10:00"
    }
  ]
}
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `200 OK` | 예약 시간 목록 조회 성공 |

### 시간 삭제 API

**Request**

```http
DELETE /admin/times/1 HTTP/1.1
```

**Response**

```http
HTTP/1.1 204 No Content
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `204 No Content` | 예약 시간 삭제 성공 |
| `400 Bad Request` | 예약 시간 id가 양수가 아닌 경우 |
| `404 Not Found` | 삭제하려는 예약 시간이 존재하지 않는 경우 |
| `409 Conflict` | 해당 예약 시간에 연결된 예약이 존재하는 경우 |

### 예약 가능한 시간 조회 API

**Request**

```http
GET /times/availability?date=2026-05-20&themeId=1 HTTP/1.1
```

**Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "availableTimes": [
    {
      "id": 1,
      "startAt": "10:00",
      "isAvailable": true
    }
  ]
}
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `200 OK` | 예약 가능 시간 조회 성공 |
| `400 Bad Request` | `date` 형식이 `yyyy-MM-dd`가 아니거나 필수 쿼리 파라미터가 누락된 경우, `themeId`가 양수가 아닌 경우 |
| `404 Not Found` | `themeId`에 해당하는 테마가 존재하지 않는 경우 |

## 테마 API

### 테마 추가 API

**Request**

```http
POST /admin/themes HTTP/1.1
Content-Type: application/json
```

```json
{
  "name": "공포",
  "description": "무서움",
  "thumbnail": "https://example.com/theme.png"
}
```

**Response**

```http
HTTP/1.1 201 Created
Content-Type: application/json
```

```json
{
  "id": 1,
  "name": "공포",
  "description": "무서움",
  "thumbnail": "https://example.com/theme.png"
}
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `201 Created` | 테마 생성 성공 |
| `400 Bad Request` | 이름, 설명, 썸네일이 비어 있거나 길이 제한을 초과한 경우 |
| `409 Conflict` | 같은 이름의 테마가 이미 존재하는 경우 |

### 테마 목록 조회 API

**Request**

```http
GET /themes HTTP/1.1
```

**Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "themes": [
    {
      "id": 1,
      "name": "공포",
      "description": "무서움",
      "thumbnail": "https://example.com/theme.png"
    }
  ]
}
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `200 OK` | 테마 목록 조회 성공 |

### 테마 삭제 API

**Request**

```http
DELETE /admin/themes/1 HTTP/1.1
```

**Response**

```http
HTTP/1.1 204 No Content
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `204 No Content` | 테마 삭제 성공 |
| `400 Bad Request` | 테마 id가 양수가 아닌 경우 |
| `404 Not Found` | 삭제하려는 테마가 존재하지 않는 경우 |

### 인기 테마 조회 API

**Request**

```http
GET /themes/popularity?days=7&size=10 HTTP/1.1
```

**Response**

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "themes": [
    {
      "id": 1,
      "name": "공포",
      "description": "무서움",
      "thumbnail": "https://example.com/theme.png"
    }
  ]
}
```

**Status Code**

| statusCode | 상황 |
| --- | --- |
| `200 OK` | 인기 테마 조회 성공 |
| `400 Bad Request` | `days` 또는 `size`가 양수가 아니거나 필수 쿼리 파라미터가 누락된 경우 |

## 프론트엔드 페이지

### 사용자 예약 페이지

- 경로: `http://localhost:8080`
- 사용자는 로그인 후 예약 생성, 내 예약 조회, 내 예약 변경, 내 예약 취소를 사용할 수 있다.
- 로그인하지 않은 사용자는 공개 조회 기능만 사용할 수 있다.
- 사용자는 인기 테마를 확인하고, 날짜와 테마를 선택해 예약 가능한 시간을 조회할 수 있다.
- 사용자는 예약 가능한 시간을 선택한 뒤 이름 입력 없이 예약할 수 있다.
- 예약 요청에 실패하면 화면에서 실패 메시지를 확인할 수 있다.
- 새로고침 시 `GET /members/me`로 로그인 상태를 복구한다.

### 관리자 페이지

- 경로: `http://localhost:8080/admin`
- 관리자는 예약 목록을 조회하고 예약을 삭제할 수 있다.
- 관리자는 예약 시간과 테마를 등록하거나 삭제할 수 있다.
- 관리자 인증/인가와 매장별 접근 제한은 3단계에서 확장한다.

## 1단계 테스트 관점

- 올바른 이메일과 비밀번호로 로그인하면 세션이 생성된다.
- 잘못된 이메일 또는 비밀번호로 로그인하면 `401 Unauthorized`를 반환한다.
- 로그인하지 않고 예약 생성, 내 예약 조회, 내 예약 변경, 내 예약 취소를 요청하면 `401 Unauthorized`를 반환한다.
- 로그인한 회원이 예약을 생성하면 요청 이름이 아니라 로그인 회원 기준으로 예약이 저장된다.
- 로그인한 회원은 자신의 예약만 조회한다.
- 로그인한 회원이 다른 회원의 예약을 변경하거나 취소하려 하면 실패한다.
- 로그아웃 후 인증이 필요한 API를 호출하면 `401 Unauthorized`를 반환한다.

## PR 설명에 포함할 내용

```text
로그인 상태 유지 방식: 웹 브라우저 흐름에 맞춰 HttpSession을 사용하고, 세션에는 loginMemberId만 저장했습니다.
인증이 필요한 API: 사용자 예약 생성/조회/변경/취소와 현재 로그인 회원 조회 API입니다.
공통 처리 위치: Interceptor에서 인증 필수 요청을 차단하고, ArgumentResolver에서 현재 로그인 회원을 컨트롤러 파라미터로 전달했습니다.
테스트한 인증 실패 상황: 미로그인 예약 생성/조회/변경/취소, 로그인 실패, 로그아웃 후 인증 API 호출, 다른 회원의 예약 변경/취소 요청입니다.
```
