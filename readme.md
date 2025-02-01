# 나만의 맛집 일기장 Foody
-  http:map-sv.site (예정)


## 1. 제작 기간 & 참여 인원

- 2025년 01월 31일 ~ 02월 07일 (예정)
- 개인 프로젝트


## 2. 사용 기술

#### `Back-end`

- Java 21
- Spring Boot 3.3.8
- Gradle
- Spring Data JPA
- postgresql


## 3. 도메인

```
 ─ domain
    ├─ auth          // 인증/인가
    ├─ favorite      // 즐겨찾기
    ├─ image         // 이미지
    ├─ post          // 게시글
```

## 4. 차트 & 다이어그램 설계

<details>
<summary><b style="cursor:pointer">ERD 설계</b></summary>
<div markdown="1">

```mermaid
erDiagram
    User ||--o{ Post : "작성"
    User ||--o{ Favorite : "즐겨찾기 추가"
    Post ||--o{ Favorite : "즐겨찾기에 등록됨"
    Post ||--o{ Image : "이미지 포함"

    User {
        LONG id
        STRING email
        STRING password
        STRING nickname
        STRING imageUri
        STRING kakaoImageUri
        STRING red
        STRING yellow
        STRING green
        STRING blue
        STRING purple
        DATETIME createdAt
        DATETIME updatedAt
        DATETIME deletedAt
        STRING hashedRefreshToken
    }

    Post {
        LONG id
        DECIMAL latitude
        DECIMAL longitude
        ENUM color
        STRING address
        STRING title
        STRING description
        DATETIME date
        INTEGER score
        DATETIME createdAt
        DATETIME updatedAt
        DATETIME deletedAt
    }

    Favorite {
        LONG id
        DATETIME createdAt
        DATETIME updatedAt
        DATETIME deletedAt
    }

    Image {
        LONG id
        STRING uri
        DATETIME createdAt
        DATETIME updatedAt
        DATETIME deletedAt
    }
```

</div>
</details>

<details>
<summary><b style="cursor:pointer">플로우 차트</b></summary>
<div markdown="2">

```mermaid
flowchart TD
    start["인증 페이지"] --> login{"로그인 또는 회원가입"}
    login -- "회원가입 선택" --> signup{"회원가입 종류 선택"}
    signup -- "소셜 회원가입" --> social_signup["소셜 회원가입"]
    signup -- "이메일 회원가입" --> self_signup["이메일 회원가입"]

    login -- "로그인 선택" --> signin["로그인"]
    social_signup --> map_home["지도 페이지"]
    self_signup --> map_home
    signin --> map_home

    map_home --> menu["네비 메뉴"]
    menu --> place_search["장소 검색"]
    menu --> feed_list["피드 목록"]
    menu --> calendar["캘린더"]
    menu --> profile["프로필 수정"]

    place_search --> add_edit_place["장소 추가"]
    place_search --> marker_filter["마커 필터링"]

    feed_list --> feed_detail["피드 상세"]
    feed_detail --> edit_feed["피드 수정"]
    feed_detail --> favorite_feed["즐겨찾기"]

    calendar --> feed_list
    profile --> category["카테고리 설정"]
```

</div>
</details>

<details>
<summary><b style="cursor:pointer">시퀸스 다이어그램</b></summary>
<div markdown="3">

```mermaid
sequenceDiagram
    participant 사용자 as 사용자
    participant 앱 as 앱 (프론트엔드)
    participant 서버 as 서버 (백엔드)
    participant DB as 데이터베이스

    사용자 ->> 앱: 소셜 로그인 요청
    앱 ->> 서버: 로그인/회원가입 요청 (소셜/일반)
    서버 ->> DB: 사용자 정보 조회
    alt 신규 사용자
        서버 ->> DB: 사용자 정보 저장
    end
    서버 -->> 앱: 로그인 성공 응답
    앱 -->> 사용자: 지도 페이지 이동

    사용자 ->> 앱: 장소 검색 요청
    앱 ->> 서버: 장소 목록 요청
    서버 ->> DB: 장소 데이터 조회
    DB -->> 서버: 장소 목록 반환
    서버 -->> 앱: 장소 목록 전송
    앱 -->> 사용자: 지도에 장소 표시
```

</div>
</details>

## 5. 핵심 기능

## 6. 프로젝트를 진행하며 고민한 Technical Issue

## 7. 트러블 슈팅

<details>
<summary style="cursor: pointer">스프링 프로젝트 시작 후 바로 종료(shutdown)</summary>
<div markdown="1">

- 문제 상황
```gradle
com.zaxxer.hikari.HikariDataSource : HikariPool-1 - Shutdown initiated.
```

- 해결
- 처음 의존성 설정 시 아래와 같은 의존성이 빠져 있었음
```gradle
implementation 'org.springframework.boot:spring-boot-starter-web'
```

</div>
</details>
