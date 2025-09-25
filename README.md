# UltimateAutoRank (MC 1.16.5 / Java 8 / CatServer)

**요약**: 과거 플러그인의 모든 기능을 제거하고, **자동 승급**만 남긴 경량 플러그인입니다.  
ZachFR **Playtime 3.0.x** + **PlaceholderAPI**로부터 `총 플레이타임(초)`을 받아, **LuckPerms 그룹 승급**을 수행합니다.

---

## 요구 사항
- **Java 8**
- **Spigot/Paper 1.16.5** (CatServer 호환)
- **LuckPerms** (필수)
- **PlaceholderAPI** (필수)
- **Playtime 3.0.x (by Zach_FR)** (필수)
  - 사용 플레이스홀더: `%playtime_seconds_all%` (총 누적 플레이타임, 초 단위)

> 참고: 플레이스홀더 목록은 공식 문서에서 확인 가능합니다. (예: `%playtime_seconds_all%`, `%playtime_minutes_all%`, ...)

## 설치
1. `plugins/` 폴더에 LuckPerms, PlaceholderAPI, Playtime 플러그인을 넣고 서버를 켭니다.
2. 생성된 `PlaceholderAPI` 폴더에서 필요한 확장이 자동 등록되며, Playtime이 설치되어 있으면 해당 플레이스홀더가 사용 가능합니다.
3. 본 플러그인의 JAR를 `plugins/`에 넣고 서버를 재시작합니다.
4. `config.yml`에서 랭크 규칙과 메시지를 원하는 대로 조정합니다.

## 명령어
- `/승급 조건` 또는 `/rank status` — 다음 승급 그룹과 **남은 플레이타임**을 보여줍니다.

## 설정(config.yml)
```yaml
check-interval-seconds: 60
playtime-seconds-placeholder: "%playtime_seconds_all%"
messages:
  prefix: "&7[&a승급&7]&r "
  status: "&e다음 등급: &b%next_group% &7| &e남은 플레이타임: &b%remaining%"
  maxed: "&a이미 최고 등급까지 승급 완료 상태입니다."
  promoted: "&a축하합니다! &e%new_group% &a등급으로 승급되었습니다."
ranks:
  - name: "Member"
    promote-to: "member"
    required-seconds: 3600
    remove-groups: ["default"]
  - name: "VIP"
    promote-to: "vip"
    required-seconds: 7200
    remove-groups: ["member"]
  - name: "MVP"
    promote-to: "mvp"
    required-seconds: 21600
    remove-groups: ["vip"]
```

- `required-seconds`는 **총 누적 플레이타임(초)** 기준입니다.
- `remove-groups`는 승급 시 제거할 이전 그룹들을 지정합니다. (이전 칭호/그룹 정리 용도)

## 동작 방식
- 서버 구동 후 10초 뒤부터, 그리고 `check-interval-seconds` 간격으로 **온라인 플레이어**를 점검합니다.
- 플레이어의 `%playtime_seconds_all%` 값을 읽고, 설정한 랭크 규칙의 조건을 만족하면 **LuckPerms 그룹**을 변경합니다.
- 여러 단계를 한 번에 만족했을 경우, 최고 조건에 도달한 **가장 높은 단계까지** 연속 승급합니다.

## 빌드
- `mvn -q -e -DskipTests clean package`
- 산출물: `target/ultimate-autorank-1.0.0.jar`

### Maven 구성 원칙
- Java 8, Spigot 1.16.5, `provided` 의존성.
- **maven-shade 사용 안 함**.
- `maven-jar-plugin`으로 `com/minkang/ultimate/**`, `plugin.yml`, `config.yml`만 포함.

## 주의
- PlaceholderAPI/Playtime/LuckPerms가 없으면 플러그인은 자동으로 비활성화합니다.
- 기존 '칭호' 시스템은 **완전히 제거**하였고, LuckPerms 그룹만 사용합니다.

## 변경/이전 메모
- 기존 UltimateTitleRank 코드베이스에서 **자동 승급**만 유지하고 나머지 기능 제거.
- config 키 구조 단순화 및 한국어 메시지 기본 제공.