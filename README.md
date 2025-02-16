# ⏰ AlarmApp_Android

![Android](https://img.shields.io/badge/Android-32-brightgreen?logo=android)  
알림 앱 **AlarmApp**은 사용자가 원하는 시간에 알람을 설정하고, 지정된 시간에 푸시 알림을 받을 수 있도록 도와주는 앱입니다.

## 📲 주요 기능
- ⏰ **알람 설정 및 관리**
  - 원하는 시간에 알람을 등록하고 관리할 수 있습니다.
  - 삼성 시계 앱과 유사한 디자인 및 UX 제공.
- 🔔 **푸시 알림 연동**
  - 설정된 알람 시간이 되면 푸시 알림 전송.
  - 백엔드 서버와 연동하여 푸시 알림 스케줄링.
- 📝 **알람 편집 기능**
  - 기존 알람을 수정 및 삭제 가능.
  - 남은 시간 표시 기능 제공.

## 🛠️ 기술 스택
- **Android (Java)**
- **Jetpack Components**
  - LiveData, ViewModel, Room (로컬 DB)
- **Firebase Cloud Messaging (FCM)**
  - 푸시 알림 전송 및 수신
