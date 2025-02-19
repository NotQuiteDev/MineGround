# 🎮 마틀그라운드 (Minecraft + 배틀그라운드)

## 📖 프로젝트 소개
**마틀그라운드**는 Minecraft와 배틀그라운드의 핵심 요소를 결합하여 새로운 게임 모드를 구현한 프로젝트입니다. 이 플러그인은 기존 Minecraft 서버를 전투 중심의 생존 게임 모드로 전환하며, 완전히 커스터마이징 가능한 다양한 기능과 메커니즘을 제공합니다.

---

## 🚀 주요 기능 (Features)

### 1. **자기장 시스템**
- 플레이어의 활동 영역을 점진적으로 축소시키는 자기장 시스템을 구현했습니다.
- 자기장 내부와 외부를 시각적으로 구분하고, 외부에 머물 경우 데미지를 받도록 설정했습니다.
- 자기장의 이동과 축소 과정에서 발생하는 다양한 버그를 해결하며, 안정성을 높였습니다.

![자기장이 끝까지 진행된 모습](images/self_field_full.png)

![자기장 밖에서 방향을 알려주는 바닥의 표시](images/direction_marker.png)

![다음 자기장의 예상 면적 표시](images/boundary_prediction.png)

### 2. **상자 및 보급 시스템**
- **랜덤 상자 분포**: 맵에 무작위로 상자를 배치하고, 매 라운드마다 새롭게 생성되도록 구현했습니다.
- **보급 상자**: 특정 아이템(무기, 방어구 등)이 포함된 보급 상자를 맵에 랜덤으로 스폰.
- **스페셜 상자**: 희귀 아이템(인챈트북, 부활 토템 등)을 포함한 특별 상자를 제작.
- **아이템 밸런스 조정**: 
  - 일반 상자의 아이템은 강화하고, 보급 상자의 아이템은 상대적으로 너프.
  - 인챈트 상자에서는 특정 인챈트(보호, 날카로움, 효율 등)의 확률을 조정하여 게임 균형 유지.

### 3. **전투 및 무기 메커니즘**
- 다양한 종류의 **특수 화살**을 제작:
  - 화염화살, 번개화살, TNT화살, 얼음화살, 순간이동 화살.
- **투명화와 낙하 효과**:
  - 투명화 및 느린 낙하 효과를 부여하는 아이템 추가.
  - 투명화 중에는 플레이어가 위치를 드러내지 않도록 화살표 표시 제거.
- **무기 인챈트**:
  - 인챈트 확률을 재조정하여 게임의 전략성을 강화.
  - 총알 수, 화살 전환 등을 자동화하여 플레이어 경험을 개선.

![화살의 아이템 이미지](images/arrows_collection.png)

![번개 화살 사용 시](images/lightning_arrow.gif)

![순간이동 화살](images/teleport_arrow.gif)

![얼음화살](images/ice_arrow.gif)

![폭발화살](images/explosion_arrow.gif)

![화염 화살](images/flame_arrow.gif)

### 4. **플레이어 관리 및 게임 페이즈**
- **생존자 관리**:
  - 게임 시작 시 생존자 수를 보스바에 표시하고, 플레이어가 죽으면 즉시 관전 모드로 전환.
- **우승자 처리**:
  - 마지막 생존자를 감지하여 우승으로 선언하고, 불꽃놀이 및 축하 이벤트 실행.
- **페이즈 관리**:
  - 맵 경계를 단계적으로 축소시키고, 각 페이즈에 맞는 기능을 실행.
- **게임 재시작**:
  - 게임 종료 후 30초 간격으로 다음 라운드를 자동 시작.

### 5. **통계 및 명예의 전당**
- 게임 내 **명예의 전당** 시스템을 추가:
  - 누적 우승 횟수, 참여 횟수, 누적 킬 수를 기반으로 랭킹 생성.
  - 우승자의 이름을 명예의 전당에 기록.
- 통계 데이터가 게임 내 HUD 및 메시지를 통해 실시간 표시되도록 구현.

![업적 시스템 화면](images/achievements_screen.png)

---

## 💡 구현 과정에서의 노력
1. **버그 해결**: 
   - 자기장 시스템 및 플레이어 관리 과정에서 발생한 여러 버그를 분석하고 수정하여 게임의 안정성을 높였습니다.
   - 특히, 죽은 플레이어가 생존자로 표시되거나 자기장이 올바르게 작동하지 않는 문제를 해결하는 데 많은 시간을 투자했습니다.
   
2. **커스터마이징 가능한 기능 개발**: 
   - 각 상자의 아이템 구성, 화살의 효과, 인챈트 확률 등을 YAML 파일에서 직접 설정할 수 있도록 제작하여, 다양한 게임 환경에 맞는 확장이 가능하게 했습니다.
   
3. **창의적인 아이디어 적용**: 
   - 기존 Minecraft 플러그인에서는 볼 수 없었던 특수 화살과 자기장 관련 시각적 효과를 추가하여 게임 몰입도를 높였습니다.
   - 예를 들어, 자기장 축소 경로를 파티클로 표시하여 플레이어들이 안전 지역으로 이동하도록 유도.
   
4. **밸런스 조정**:
   - 아이템의 밸런스를 지속적으로 테스트하며, 플레이어 경험을 최적화했습니다.
   - 테스트 과정에서는 커뮤니티에 글을 올려 플레이하며, 게임 플레이 데이터 및 피드백을 기반으로 개선 작업을 진행했습니다.

---

## 📊 추가 구현 예정 기능
- **포터블 상점 시스템**: 게임 중간에 사용할 수 있는 포션 및 인챈트 상점 추가.
- **블랙홀 화살**: 특정 범위의 블록과 플레이어를 흡수하는 특수 화살 개발.
- **마지막 페이즈 개선**: 최종 자기장 단계에서 발생하는 일부 비정상적인 블록 처리 문제 해결.

---

## 🎯 프로젝트의 의의
마틀그라운드는 단순한 생존 게임을 넘어, Minecraft를 기반으로 한 독창적이고 몰입감 있는 게임 모드 제작의 가능성을 탐구한 프로젝트입니다. 플러그인 개발 과정에서 게임 밸런스 조정, 버그 해결, 기능 확장을 통해 플레이어들에게 새로운 재미를 제공하고자 노력했습니다.
