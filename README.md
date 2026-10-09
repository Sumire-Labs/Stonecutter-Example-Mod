# Stonecutter Mod Template

Javaパッケージ `com.sumirelabs`
Minecraft **1.16.5 / 1.18.2 / 1.20.1 / 1.21.1 / 26.1〜26.3**を対象（正式パッチ版を含む9リリース・18ターゲット）

## 準備

Gradle実行用に **JDK 25**、Gitが必要です。各対象のJava 8 / 17 / 21 / 25コンパイラはGradle toolchainが選択し、未インストールならFoojayから取得します。IDEのGradle JVMも25にしてください。

Cleanroom VersioningはGit履歴を使うため、ZIPから展開した場合は初回ビルド前に `git init -b main`、`git add .`、`git commit -m "Initial template"` を実行します。GitHub Actionsでは完全な履歴とタグが必要です。

## 構成と設定

```text
src/main/java/com/sumirelabs/             Mod本体・共通定数・ローダー別の入口
src/main/resources/                      メタデータ・assets・data
build.gradle                             modern Forge / NeoForgeの共通ビルド
stonecutter.gradle                       Stonecutter制御・集約タスク・Forgix
settings.gradle                          対象選択・プラグインの解決

gradle/build.fabric.gradle              Fabric用ビルド方式
gradle/scripts/mod.gradle               共通のtoolchain・token・リソース選択

gradle.properties                       Mod情報・対象選択・Forgix
versions/support.properties             対応するMinecraft版の一覧
versions/<Minecraft>/gradle.properties  Java / ローダーの一覧と依存バージョン
versions/<Minecraft>-<loader>/build/    Stonecutter生成物・ビルド出力
```

対応するMinecraft版は `versions/support.properties` の `minecraft_versions`、各版のローダーは `versions/<Minecraft>/gradle.properties` の `loaders` で管理します。対応一覧の生成ファイルや更新スクリプトはありません。版を追加するときは一覧と、その版のpropertiesを追加します。

普段の設定はルートと `versions/<Minecraft>/gradle.properties` で行います。`targets` を `-P` で指定すると `target_versions` / `target_loaders` より優先します。対象ローダーが存在しないMinecraft版との組み合わせは作りません。`stonecutter_active` はIDEで編集する対象で、選択集合にないときはその集合の先頭を使用します。

| 設定 | 役割 |
| --- | --- |
| `target_versions` | Gradleへ読み込むMinecraft版。`all` なら対応一覧の全版 |
| `target_loaders` | 上記の版で読み込むローダー。`all` なら各版に存在する対応ローダー |
| `targets` | 任意の正確な組み合わせ。この指定があれば上の2つより優先 |
| `stonecutter_active` | 編集する基準の版・ローダー。ビルド対象を絞る設定ではない |
| `forgix_minecraft_version` | 読み込んだ対象のうち、Forgixで統合する1つのMinecraft版 |
| `forgix_loaders` | 統合するローダー候補。指定したMinecraft版にあるものだけを選択 |

初期設定の `target_versions=1.20.1` / `target_loaders=all` では、1.20.1 ForgeとFabricを読み込みます。`stonecutter_active=1.20.1-fabric` はFabric側を編集の基準にします。`forgix_minecraft_version=1.20.1` は、その2つを統合する設定です。`forgix_loaders=fabric,forge,neoforge` にNeoForgeも含めていますが、対応一覧で1.20.1 NeoForgeを選んでいないため、その版では対象になりません。

例えば1.21.1のFabric + NeoForgeを編集・ビルド・統合する場合は、次の設定にします。

```properties
target_versions=1.21.1
target_loaders=all
stonecutter_active=1.21.1-fabric
forgix_minecraft_version=1.21.1
forgix_loaders=fabric,forge,neoforge
```

この場合はその版に存在するFabricとNeoForgeだけを統合します。統合の実行は `mergeJars`、通常ビルドは `build` / `buildAll` です。

`mod_package` を変える場合はJavaのpackage宣言とディレクトリも変更します。TokenEnvoyは文字列・リソースを置換するプラグインで、Javaのpackage文は置換しません。Mod本体も共通定数の `StonecutterExampleMod` も `src/main/java` に置き、各対象でコンパイルします。全世代で共有する無条件のコードはJava 8で使用可能な言語・ライブラリに揃えてください。

Minecraft APIを利用する共通コードは `src/main/java` に置き、Stonecutterの `//? if >=1.21 {` や `//? if fabric {` で分岐します。初期テンプレートは各ローダーの初期化時にMod名とバージョンをログ出力します。

`fabric.mod.json`、`META-INF/mods.toml`、`META-INF/neoforge.mods.toml` はすべて `src/main/resources` に置きます。ビルド時に対象ローダーのものだけを選ぶため、配布Jarへ他ローダーのメタデータは入りません。Fabric APIやArchitectury APIは初期化に不要なため依存していません。

成果物は `versions/<Minecraft>-<loader>/build/libs/` の、`-dev` / `-sources` が付かないJarです。

## ローダーの範囲

| 対象 | 構成 |
| --- | --- |
| 1.16.5 / 1.18.2 / 1.20.1 | Forge（TauMC版ModDevGradle）/ Fabric（Loom） |
| 1.21.1 | Fabric（Loom）/ NeoForge（TauMC版ModDevGradle） |
| 26.1 / 26.1.1 / 26.1.2 / 26.2 / 26.3 | Fabric（非難読化Loom）/ NeoForge（TauMC版ModDevGradle） |

## modern側の共通ビルド設定

Forge 1.16.5 / 1.18.2 / 1.20.1と、NeoForge 1.21.1 / 26.xは `build.gradle` でまとめて設定しています。[TauMC版ModDevGradle](https://git.taumc.org/TauMC/ModDevGradle)の `org.taumc.moddev.legacyforge` / `org.taumc.moddev` を使い、開発起動・Modのsource set登録・AT検証・成果物選択を共有します。プラグインのバージョンは `tau_moddev_version` で管理します。

Forge 1.16.5では `useMojangClassNames` を有効にし、modern側のクラス名をMojmapへ揃えています。配布バイトコードは各版の `java_version` に従います。Forge 1.16.5は `moddev_toolchain_version=21` でコンパイル・開発起動にJava 21を使い、`--release 8` で通常Forge向けのJava 8互換Jarを生成します。使用可能なJava構文・標準APIもJava 8の範囲です。同版のFabricは従来どおりJDK 8を使います。

TauGradleのversioning / publishing / Stonecutter補助プラグインは導入していません。バージョン計算はCleanroom Versioning、トークン置換はTokenEnvoyが担当します。

```properties
# auto: ローカルではMinecraftのソースを生成し、CIではバイナリパッチ方式を使用
# true: ソース生成を省略 / false: ソース生成を実行
moddev_disable_recompilation=auto
moddev_validate_access_transformers=true
```

これらは `versions/<Minecraft>/gradle.properties` でも版別に上書きできます。共有ATは `src/main/resources/META-INF/accesstransformer.cfg` に置けます。

modern Forgeの配布用Jarは `build/libs/` の再難読化済み `reobfJar`、開発用の同名Jarは `build/devlibs/` に分かれます。Forgixには配布用のタスク出力を明示しています。Fabricは `gradle/build.fabric.gradle` に集約しています。

## バージョン管理とTokenEnvoy

[Cleanroom Versioning](https://github.com/CleanroomMC/Versioning) **3.3.0**をルートに適用し、計算結果を各Stonecutterプロジェクトへ渡します。同じリポジトリからビルドする全Minecraft版でModバージョンが一致します。`version` / `mod_version` は定義しません。タグは `v1.0.0` などで設定します。既定のreleaseブランチは `main`、開発ブランチは `develop/<major>.<minor>` です。詳細なタグ・公開ポリシーは公式READMEを参照してください。

[TokenEnvoy](https://github.com/CleanroomMC/TokenEnvoy) **3.0.0**が `@{MOD_ID}`、`@{MOD_NAME}`、`@{MOD_VERSION}` などをJava文字列（annotation値を含む）とリソース内で置換します。Blossomは使用していません。Stonecutterのソース切り替え後、各ターゲットのコンパイル時に置換するため、テンプレートの元ソースを書き換えません。

## Forgix

[Forgix](https://github.com/PacifistMC/Forgix) **2.0.0**を適用しています。初期設定は1.20.1のFabric + Forgeを統合します。

ルートの `enable_forgix=true` / `false` で切り替えられます。`false` ではForgixプラグインを適用せず、統合タスクも登録しません。通常の `build` / `buildAll` は利用できます。`true` でも統合は `mergeJars` を明示実行したときだけ行います。コマンド単位では `'-Penable_forgix=false'` で上書きできます。既存の統合Jarは無効化しても削除しません。

```powershell
.\gradlew.bat mergeJars '-Ptarget_versions=1.20.1' '-Pforgix_minecraft_version=1.20.1' '-Pforgix_loaders=fabric,forge'

# 26.3はFabric + NeoForge
.\gradlew.bat mergeJars '-Ptarget_versions=26.3' '-Pforgix_minecraft_version=26.3' '-Pforgix_loaders=fabric,neoforge'
```

出力は `build/forgix/`。入力は各ビルド方式の配布用Jarを明示し、ビルド依存も設定しています。異なるMinecraft版をまとめる `mergeVersions` は設定していません。

## GitHub Actionsとリリース

[CleanroomModTemplateのワークフロー](https://github.com/CleanroomMC/CleanroomModTemplate/tree/main/.github/workflows)と同じく、通常ビルド・GitHub Release・CurseForge / Modrinth公開の3種類を用意しています。

| ワークフロー | 実行タイミングと処理 |
| --- | --- |
| `build.yml` | ブランチへのpush・PR・手動実行。対応一覧の全ターゲットをビルドして配布Jarを保存 |
| `release.yml` | タグのpush。同じビルドを再利用し、全配布JarをGitHub Releaseへ添付。リリースノートを自動生成 |
| `release-to-cf-mr.yml` | Actionsから手動実行。既存タグを指定し、そのコミットをビルドして有効な公開先へアップロード |

CIの対象は `versions/support.properties` と各版の `loaders` です。ローカル作業用の `target_versions` / `target_loaders` で公開範囲は絞りません。`enable_forgix=true` なら、`forgix_loaders` に含まれるローダーが2つ以上あるMinecraft版すべての統合Jarも生成します。CIでは各版を順に指定するため、`forgix_minecraft_version` の固定値には依存しません。`false` なら統合ビルドを省略します。

GitHub Releaseには個別Jarと生成した統合Jarを添付します。CurseForge / Modrinthには各Minecraft版の統合Jarを公開し、統合対象外のローダーは個別Jarを公開します。統合を無効にした場合はすべて個別Jarです。Minecraft版・対応ローダー・Java版は各版のpropertiesから設定し、公開バージョン番号には `v1.0.0+1.20.1-merged` のように対象を付けて区別します。dev / sources / javadoc Jarは公開対象に含めません。

公開先をルート `gradle.properties` で設定してください。初期状態では両方無効です。

```properties
publish_modrinth=true
modrinth_project_id=あなたのプロジェクトID
publish_curseforge=true
curseforge_project_id=あなたの数値プロジェクトID
release_type=release
```

`release_type` は `release` / `beta` / `alpha`。プロジェクトIDは版別propertiesでも上書きできます。GitHubのSettings → Secrets and variables → Actionsに、有効な公開先の `MODRINTH_TOKEN` / `CURSEFORGE_TOKEN` を登録します。GitHub Releaseは標準の `GITHUB_TOKEN` を利用します。

設定をコミットして `v1.0.0` などのタグをpushするとGitHub Releaseが作成されます。ハイフンを含むタグ（例：`v1.0.0-beta.1`）はGitHubでprereleaseになります。その後、Actions → Publish to CurseForge and Modrinth → Run workflowで同じタグを指定します。公開設定は指定タグのコミットに含まれている必要があります。変更履歴は直前の `v` + 数字で始まる到達可能なタグから生成し、初回は全コミットを使用します。同じタグの再公開では、すでに公開済みのファイルとの重複に注意してください。

## 参考

- [Stonecutterソース・API](https://codeberg.org/stonecutter/stonecutter)
- [CeleritasのStonecutter構成](https://git.taumc.org/embeddedt/celeritas/src/branch/stonecutter)
- [Fabric 26.3開発情報](https://www.fabricmc.net/2026/09/15/263.html)

ビルドスクリプトの役割分離を参考にしています。参考Modの実装コードはコピーしていません。
