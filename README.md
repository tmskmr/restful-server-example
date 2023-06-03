# restful-server-example
IHEチュートリアル「FHIRの実装～HAPI FHIRでのPDQm対応～」用ソースコード一式

## 前提ソフトウェア
カッコ内は動作確認を行ったバージョン
- Eclipse (4.16.0)：統合開発環境（IDE）
- MyBatis Generatorプラグイン (1.4.0.201911242214)：MyBatis用のマッパークラス生成ツール
- Maven (3.6.0)：構成管理、ビルドツール
- Tomcat (9.0.36)：Javaプログラム（WARファイル）を実行するアプリケーション・サーバー
- Java (16.0.1)：Tomcat、Javaプログラムの実行環境

## MY_PATIENTテーブルの初期化
PSQLコマンドでDDL\init.sqlを実行する。

1. cd DDL
2. psql -U postgres -d <DB名> -f init.sql

## 設定方法
src\main\resources\application.propertiesファイルを編集する

### maindb.driver
データベースのドライバ―クラス

### maindb.url
データベースの接続URL

### maindb.username
データベースの接続ユーザー名

### maindb.password
データベースの接続パスワード

### patient.namespace
Patientリソースのidentifierのネームスペース

## ビルド方法(Eclipse)
1. Eclipse でプロジェクトを「既存 Maven プロジェクト」としてインポートする
2. プロジェクト・エクスローラーで「restful-server-example」を右クリックし、「実行」「Maven install」を順に選択する

## ビルド方法(Maven)
プロジェクトのルートフォルダで「mvn install」を実行する。（mvnコマンドにパスが通っていること）

## デプロイ方法
ビルドで作成されたtarget\restful-server-example.warを\apache-tomcat-9.0.36\webappsフォルダに置く
