# MIGRATION_NOTES

- 본 빌드는 **자동 승급만 남기고** 나머지 기능(칭호 표시/보상/GUI 등)을 **완전히 제거**했습니다.
- 승급 기준은 **Playtime(ZachFR)** 의 `%playtime_seconds_all%` 값을 사용합니다.
- 승급 연동은 **LuckPerms 그룹**만 사용합니다. (트랙 미사용)
- `remove-groups`로 이전 그룹(칭호)을 정리합니다.
- `repositories 단일화` 요청이 있었으나, Spigot API와 LuckPerms API를 해석하기 위해 최소 2개의 리포지토리가 필요합니다.
  - PlaceholderAPI는 **리플렉션**으로 호출하여 별도 저장소 추가 없이 동작합니다.
- Java 8 / MC 1.16.5 / CatServer 기준으로 작성했습니다.