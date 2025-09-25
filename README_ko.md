# UltimateAutoRank v1.1.1 (MC 1.16.5 / Java 8 / CatServer)

**자동 승급만 유지한 경량 플러그인.**  
- Playtime(3.x by Zach_FR) + PlaceholderAPI → `%playtime_seconds_all%`(총 누적 플레이타임·초) 사용
- LuckPerms 그룹 승급 + `remove-groups`로 이전 칭호/그룹 제거
- `/승급 조건` 또는 `/rank status`로 다음 등급 & 남은 플레이타임 조회

## 요구 사항
- Java 8
- Spigot/Paper 1.16.5 (CatServer 호환)
- LuckPerms, PlaceholderAPI, Playtime(3.x)

## 빌드
```
mvn -q -DskipTests clean package
```
산출물: `target/ultimate-autorank-1.1.1.jar`

## 설정(config.yml)
- `check-interval-seconds`: 온라인 유저 자동 점검 간격 (초)
- `playtime-seconds-placeholder`: 기본 `%playtime_seconds_all%`
- `ranks`: `required-seconds`(초), `promote-to`(승급 그룹), `remove-groups`(이전 그룹 제거)

## 주의
- 의존 플러그인 없으면 안전 비활성화
- 기존 칭호 시스템은 전부 제거, LuckPerms 그룹만 사용