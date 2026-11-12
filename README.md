# JSL Hotel

ホテルの客室予約からオンライン決済、会員・メンバーシップ管理、管理者機能までを備えたホテル予約システムです。
3名のチームで開発し、チームリーダーを担当しました。

[日本語](#日本語) | [한국어](#한국어)

---

## 日本語

### 概要

JSP/Servletで構築したMVC構成のWebアプリケーションです。
客室検索・予約・決済（Toss Payments）・予約確認メール送信まで、予約の一連の流れを実装しています。
チーム開発終了後は、自宅サーバー（Ubuntu Server + Tomcat 9）で運用しています。

### 主な機能

**ユーザー**
- 空室検索・客室詳細・予約
- Toss Paymentsによるオンライン決済
- 予約確認・領収書表示、予約完了メールの送信
- 会員登録・ログイン・会員情報変更・退会
- ID/パスワード検索（メールによる仮パスワード発行）
- メンバーシップ（等級・ポイント・会員カード）
- お知らせ・FAQ・Q&A（閲覧用パスワード付き）
- ホテル案内（概要・客室・施設・アクセス）

**管理者**
- 会員管理（一覧・詳細・状態変更）
- 客室管理（状態変更・画像アップロード・メイン画像設定）
- お知らせ（添付ファイル付き）・FAQ・Q&A回答の管理
- 予約確認

### 技術スタック

| 区分 | 技術 |
|------|------|
| Backend | Java, JSP, Servlet, JSTL, JDBC |
| Frontend | HTML, CSS, JavaScript, jQuery |
| Database | Oracle Database XE 21c |
| 外部連携 | Toss Payments API, Gmail SMTP（JavaMail）, SFTP（JSch） |
| Server | Apache Tomcat 9, Ubuntu Server, Cloudflare Tunnel, Tailscale |
| 協業 | Git, GitHub |

### システム構成

    ブラウザ
       │ HTTPS
       ▼
    Cloudflare Tunnel
       │
       ▼
    Webサーバー（Ubuntu Server / Tomcat 9）
       │ Tailscale（プライベートネットワーク）
       ▼
    DBサーバー（Ubuntu Server）
       ├─ Oracle Database XE 21c
       └─ 画像・添付ファイル保存（SFTP）

### 設計のポイント

- **コマンドパターンによるMVC構成**：コントローラー（Servlet）が処理を `CommonExecute` を実装したコマンドクラスに振り分ける構成にし、機能追加をしやすくしました。
- **ファイル保存の分離**：客室画像やお知らせの添付ファイルは、WebサーバーではなくDBサーバー側にSFTPで保存し、Webサーバーを入れ替えてもデータが残るようにしました。
- **パスワードのハッシュ化**：会員パスワードはSHA-256でハッシュ化して保存しています。

### 運用・セキュリティ改善

- `FileDownServlet` のパストラバーサル（経路操作）脆弱性を修正しました。
- DB・SFTP・メール・決済の機密情報をソースコードから分離し、サーバー上の設定ファイル（`/etc/jslhotel.properties`）から読み込む `AppConfig` クラスを追加しました。
- WindowsからUbuntu Serverへ移行し、Tomcatをsystemdで常時稼働させています。

### ローカルでの実行方法

1. Eclipseで「Dynamic Web Project」としてインポートし、Tomcat 9を設定します。
2. 以下の形式で設定ファイルを作成します。

        db.url=jdbc:oracle:thin:@localhost:1521/XEPDB1
        db.user=your_db_user
        db.password=your_db_password
        sftp.host=localhost
        sftp.user=your_sftp_user
        sftp.password=your_sftp_password
        mail.sender=your_address@gmail.com
        mail.reservation.password=your_gmail_app_password
        mail.password-reset.password=your_gmail_app_password
        toss.secret-key=your_toss_test_secret_key

3. 設定ファイルの場所は、既定では `/etc/jslhotel.properties` です。別の場所に置く場合は、環境変数 `JSLHOTEL_CONFIG` にパスを指定してください。

---

## 한국어

### 개요

JSP/Servlet 기반 MVC 구조의 호텔 예약 시스템입니다. 객실 검색·예약·토스페이먼츠 결제·예약 확인 메일까지 예약 전 과정을 구현했으며, 3명 팀 프로젝트에서 팀장을 맡았습니다. 현재는 자택 서버(Ubuntu Server + Tomcat 9)에서 운영 중입니다.

### 주요 기능

- 사용자: 빈 객실 검색·예약, 토스페이먼츠 결제, 예약 확인·영수증, 예약 완료 메일, 회원가입·로그인·정보수정·탈퇴, ID/비밀번호 찾기(임시 비밀번호 메일), 멤버십(등급·포인트·카드), 공지·FAQ·Q&A, 호텔 안내
- 관리자: 회원 관리, 객실 관리(상태·이미지 업로드), 공지(첨부파일)·FAQ·Q&A 답변 관리, 예약 확인

### 기술 스택

Java / JSP / Servlet / JSTL / JDBC / Oracle XE 21c / jQuery / Toss Payments API / JavaMail / JSch(SFTP) / Tomcat 9 / Ubuntu Server / Cloudflare Tunnel / Tailscale

### 포인트

- 커맨드 패턴 기반 MVC 구조로 기능 추가가 쉽도록 설계
- 이미지·첨부파일은 SFTP로 DB 서버에 저장해 웹 서버와 데이터 분리
- 회원 비밀번호 SHA-256 해시 저장
- 파일 다운로드 경로 조작 취약점 수정
- DB·SFTP·메일·결제 비밀값을 소스에서 분리해 서버 설정 파일에서 읽도록 개선
