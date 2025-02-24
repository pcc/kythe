load("@bazel_gazelle//:deps.bzl", "gazelle_dependencies")
load("@bazel_tools//tools/build_defs/repo:git.bzl", "git_repository")
load("@bazel_tools//tools/build_defs/repo:http.bzl", "http_archive")
load("@io_bazel_rules_go//go:deps.bzl", "go_register_toolchains", "go_rules_dependencies")
load("@io_kythe//:setup.bzl", "maybe")
load("@io_kythe//tools:build_rules/shims.bzl", "go_repository")
load("@io_kythe//tools/build_rules/llvm:repo.bzl", "git_llvm_repository")
load("@io_kythe//third_party/leiningen:lein_repo.bzl", "lein_repository")
load("@io_kythe//tools/build_rules/lexyacc:lexyacc.bzl", "lexyacc_configure")

def _rule_dependencies():
    gazelle_dependencies()
    go_rules_dependencies()
    go_register_toolchains()

def _cc_dependencies():
    maybe(
        http_archive,
        name = "net_zlib",
        build_file = "@io_kythe//third_party:zlib.BUILD",
        sha256 = "c3e5e9fdd5004dcb542feda5ee4f0ff0744628baf8ed2dd5d66f8ca1197cb1a1",
        strip_prefix = "zlib-1.2.11",
        urls = ["https://zlib.net/zlib-1.2.11.tar.gz"],
    )

    maybe(
        http_archive,
        name = "org_libzip",
        build_file = "@io_kythe//third_party:libzip.BUILD",
        sha256 = "a5d22f0c87a2625450eaa5e10db18b8ee4ef17042102d04c62e311993a2ba363",
        strip_prefix = "libzip-rel-1-5-1",
        urls = [
            # Bazel does not like the official download link at libzip.org,
            # so use the GitHub release tag.
            "https://github.com/nih-at/libzip/archive/rel-1-5-1.zip",
        ],
    )

    maybe(
        http_archive,
        name = "boringssl",  # Must match upstream workspace name.
        # Gitiles creates gzip files with an embedded timestamp, so we cannot use
        # sha256 to validate the archives.  We must rely on the commit hash and https.
        # Commits must come from the master-with-bazel branch.
        url = "https://boringssl.googlesource.com/boringssl/+archive/4be3aa87917b20fedc45fa1fc5b6a2f3738612ad.tar.gz",
    )

    maybe(
        http_archive,
        name = "com_github_tencent_rapidjson",
        build_file = "@io_kythe//third_party:rapidjson.BUILD",
        sha256 = "8e00c38829d6785a2dfb951bb87c6974fa07dfe488aa5b25deec4b8bc0f6a3ab",
        strip_prefix = "rapidjson-1.1.0",
        url = "https://github.com/Tencent/rapidjson/archive/v1.1.0.zip",
    )

    # Make sure to update regularly in accordance with Abseil's principle of live at HEAD
    maybe(
        http_archive,
        name = "com_google_absl",
        sha256 = "3601822b4d3c7cc62d891a2d0993b902ad1858e4faf41d895678d3a7749ec503",
        strip_prefix = "abseil-cpp-ca3f87560a0eef716195cadf66dc6b938a579ec6",
        url = "https://github.com/abseil/abseil-cpp/archive/ca3f87560a0eef716195cadf66dc6b938a579ec6.zip",
    )

    maybe(
        http_archive,
        name = "com_google_googletest",
        sha256 = "89cebb92b9a7eb32c53e180ccc0db8f677c3e838883c5fbd07e6412d7e1f12c7",
        strip_prefix = "googletest-d175c8bf823e709d570772b038757fadf63bc632",
        url = "https://github.com/google/googletest/archive/d175c8bf823e709d570772b038757fadf63bc632.zip",
    )

    maybe(
        http_archive,
        name = "com_github_gflags_gflags",
        sha256 = "19713a36c9f32b33df59d1c79b4958434cb005b5b47dc5400a7a4b078111d9b5",
        strip_prefix = "gflags-2.2.2",
        url = "https://github.com/gflags/gflags/archive/v2.2.2.zip",
    )

    maybe(
        http_archive,
        name = "com_github_google_glog",
        build_file = "@io_kythe//third_party:googlelog.BUILD",
        sha256 = "ce61883437240d650be724043e8b3c67e257690f876ca9fd53ace2a791cfea6c",
        strip_prefix = "glog-bac8811710c77ac3718be1c4801f43d37c1aea46",
        url = "https://github.com/google/glog/archive/bac8811710c77ac3718be1c4801f43d37c1aea46.zip",
    )

    maybe(
        http_archive,
        name = "org_brotli",
        sha256 = "fb511e09ea284fcd18fe2a2632744609a77f69c345428b9f0d2cc15171215f06",
        strip_prefix = "brotli-ee2a5e1540cbd6ef883a897499d9596307f7f7f9",
        url = "https://github.com/google/brotli/archive/ee2a5e1540cbd6ef883a897499d9596307f7f7f9.zip",
    )

    maybe(
        http_archive,
        name = "com_google_riegeli",
        sha256 = "5c1714329c19759201b7f2c6a2cf8b255b6f10c752b197d6e8847b8574dcd96b",
        strip_prefix = "riegeli-6b1dd7be479f6ffffdec06c39f352bd5a87b63b7",
        url = "https://github.com/google/riegeli/archive/6b1dd7be479f6ffffdec06c39f352bd5a87b63b7.zip",
    )

    maybe(
        http_archive,
        name = "org_libmemcached_libmemcached",
        build_file = "@io_kythe//third_party:libmemcached.BUILD",
        sha256 = "e22c0bb032fde08f53de9ffbc5a128233041d9f33b5de022c0978a2149885f82",
        strip_prefix = "libmemcached-1.0.18",
        url = "https://launchpad.net/libmemcached/1.0/1.0.18/+download/libmemcached-1.0.18.tar.gz",
    )

    maybe(
        http_archive,
        name = "se_haxx_curl",
        build_file = "@io_kythe//third_party:curl.BUILD",
        sha256 = "ff3e80c1ca6a068428726cd7dd19037a47cc538ce58ef61c59587191039b2ca6",
        strip_prefix = "curl-7.49.1",
        urls = [
            "http://bazel-mirror.storage.googleapis.com/curl.haxx.se/download/curl-7.49.1.tar.gz",
            "https://curl.haxx.se/download/curl-7.49.1.tar.gz",
        ],
    )

    maybe(
        http_archive,
        name = "com_googlesource_code_re2",
        sha256 = "ae9b962dbd6427565efd3e9503acb40a1385b21962c29050546c9347ac7fa93f",
        strip_prefix = "re2-2019-01-01",
        url = "https://github.com/google/re2/archive/2019-01-01.zip",
    )

    maybe(
        http_archive,
        name = "com_github_stedolan_jq",
        build_file = "@io_kythe//third_party:jq.BUILD",
        sha256 = "998c41babeb57b4304e65b4eb73094279b3ab1e63801b6b4bddd487ce009b39d",
        strip_prefix = "jq-1.4",
        url = "https://github.com/stedolan/jq/releases/download/jq-1.4/jq-1.4.tar.gz",
    )

    maybe(
        http_archive,
        name = "com_github_google_snappy",
        build_file = "@io_kythe//third_party:snappy.BUILD",
        sha256 = "61e05a0295fd849072668b1f3494801237d809427cfe8fd014cda455036c3ef7",
        strip_prefix = "snappy-1.1.7",
        url = "https://github.com/google/snappy/archive/1.1.7.zip",
    )

    maybe(
        http_archive,
        name = "com_github_google_leveldb",
        build_file = "@io_kythe//third_party:leveldb.BUILD",
        sha256 = "5b2bd7a91489095ad54bb81ca6544561025b48ec6d19cc955325f96755d88414",
        strip_prefix = "leveldb-1.20",
        url = "https://github.com/google/leveldb/archive/v1.20.zip",
    )

    maybe(
        git_llvm_repository,
        name = "org_llvm",
    )

    lexyacc_configure()

def _java_dependencies():
    maybe(
        # For @com_google_common_flogger
        http_archive,
        name = "google_bazel_common",
        strip_prefix = "bazel-common-b3778739a9c67eaefe0725389f03cf821392ac67",
        urls = ["https://github.com/google/bazel-common/archive/b3778739a9c67eaefe0725389f03cf821392ac67.zip"],
    )
    maybe(
        git_repository,
        name = "com_google_common_flogger",
        commit = "ca8ad22bc1479b5675118308f88ef3fff7d26c1f",
        remote = "https://github.com/google/flogger",
    )

    maybe(
        native.maven_jar,
        name = "com_google_code_gson_gson",
        artifact = "com.google.code.gson:gson:2.8.5",
        sha1 = "f645ed69d595b24d4cf8b3fbb64cc505bede8829",
    )

    maybe(
        native.maven_jar,
        name = "com_google_guava_guava",
        artifact = "com.google.guava:guava:26.0-jre",
        sha1 = "6a806eff209f36f635f943e16d97491f00f6bfab",
    )

    maybe(
        native.maven_jar,
        name = "com_google_re2j_re2j",
        artifact = "com.google.re2j:re2j:1.2",
        sha1 = "4361eed4abe6f84d982cbb26749825f285996dd2",
    )

    maybe(
        native.maven_jar,
        name = "com_google_code_findbugs_jsr305",
        artifact = "com.google.code.findbugs:jsr305:3.0.1",
        sha1 = "f7be08ec23c21485b9b5a1cf1654c2ec8c58168d",
    )

    maybe(
        native.maven_jar,
        name = "com_google_errorprone_error_prone_annotations",
        artifact = "com.google.errorprone:error_prone_annotations:2.3.1",
        sha1 = "a6a2b2df72fd13ec466216049b303f206bd66c5d",
    )

    maybe(
        native.maven_jar,
        name = "org_ow2_asm_asm",
        artifact = "org.ow2.asm:asm:7.0",
        sha1 = "d74d4ba0dee443f68fb2dcb7fcdb945a2cd89912",
    )

    maybe(
        native.maven_jar,
        name = "junit_junit",
        artifact = "junit:junit:4.12",
        sha1 = "2973d150c0dc1fefe998f834810d68f278ea58ec",
    )

    maybe(
        native.maven_jar,
        name = "com_beust_jcommander",
        artifact = "com.beust:jcommander:1.48",
        sha1 = "bfcb96281ea3b59d626704f74bc6d625ff51cbce",
    )

    maybe(
        native.maven_jar,
        name = "com_google_truth_truth",
        artifact = "com.google.truth:truth:0.44",
        sha1 = "11eff954c0c14da7d43276d7b3bcf71463105368",
    )

    maybe(
        native.maven_jar,
        name = "com_googlecode_java_diff_utils",
        artifact = "com.googlecode.java-diff-utils:diffutils:1.3.0",
        sha1 = "7e060dd5b19431e6d198e91ff670644372f60fbd",
    )

    maybe(
        native.maven_jar,
        name = "com_google_auto_value_auto_value",
        artifact = "com.google.auto.value:auto-value:1.5.4",
        sha1 = "65183ddd1e9542d69d8f613fdae91540d04e1476",
    )

    maybe(
        native.maven_jar,
        name = "com_google_auto_service_auto_service",
        artifact = "com.google.auto.service:auto-service:1.0-rc4",
        sha1 = "44954d465f3b9065388bbd2fc08a3eb8fd07917c",
    )

    maybe(
        native.maven_jar,
        name = "com_google_auto_auto_common",
        artifact = "com.google.auto:auto-common:0.10",
        sha1 = "c8f153ebe04a17183480ab4016098055fb474364",
    )

    maybe(
        native.maven_jar,
        name = "javax_annotation_jsr250_api",
        artifact = "javax.annotation:jsr250-api:1.0",
        sha1 = "5025422767732a1ab45d93abfea846513d742dcf",
    )

    maybe(
        native.maven_jar,
        name = "com_google_common_html_types",
        artifact = "com.google.common.html.types:types:1.0.8",
        sha1 = "9e9cf7bc4b2a60efeb5f5581fe46d17c068e0777",
    )

def _go_dependencies():
    maybe(
        go_repository,
        name = "com_github_golang_protobuf",
        build_file_proto_mode = "disable_global",
        custom = "protobuf",
        importpath = "github.com/golang/protobuf",
        patch_args = ["-p1"],
        patches = ["@io_bazel_rules_go//third_party:com_github_golang_protobuf-extras.patch"],
        tag = "v1.3.0",
    )

    maybe(
        go_repository,
        name = "com_github_google_uuid",
        custom = "uuid",
        importpath = "github.com/google/uuid",
        tag = "v1.1.0",
    )

    maybe(
        go_repository,
        name = "com_github_jmhodges_levigo",
        custom = "levigo",
        importpath = "github.com/jmhodges/levigo",
        tag = "v1.0.0",
    )

    maybe(
        go_repository,
        name = "com_github_google_go_cmp",
        custom = "cmp",
        importpath = "github.com/google/go-cmp",
        tag = "v0.2.0",
    )

    maybe(
        go_repository,
        name = "org_golang_x_sync",
        commit = "42b317875d0f",
        custom = "sync",
        custom_git = "https://github.com/golang/sync.git",
        importpath = "golang.org/x/sync",
    )

    maybe(
        go_repository,
        name = "com_github_sourcegraph_jsonrpc2",
        commit = "a3d86c792f0f5a0c0c2c4ed9157125e914cb5534",
        custom = "jsonrpc2",
        importpath = "github.com/sourcegraph/jsonrpc2",
    )

    maybe(
        go_repository,
        name = "com_github_hanwen_go_fuse",
        custom = "go_fuse",
        importpath = "github.com/hanwen/go-fuse",
        tag = "v1.0.0",
    )

    maybe(
        go_repository,
        name = "com_github_golang_snappy",
        custom = "snappy",
        importpath = "github.com/golang/snappy",
        tag = "v0.0.1",
    )

    maybe(
        go_repository,
        name = "com_github_sourcegraph_go_langserver",
        commit = "e526744fd766a8f42e55bd92a3843c2afcdbf08c",
        custom = "langserver",
        importpath = "github.com/sourcegraph/go-langserver",
    )

    maybe(
        go_repository,
        name = "com_github_sergi_go_diff",
        commit = "da645544ed44df016359bd4c0e3dc60ee3a0da43",
        custom = "diff",
        importpath = "github.com/sergi/go-diff",
    )

    maybe(
        go_repository,
        name = "com_github_google_subcommands",
        custom = "subcommands",
        importpath = "github.com/google/subcommands",
        tag = "1.0.1",
    )

    maybe(
        go_repository,
        name = "org_golang_x_tools",
        commit = "589c23e65e65055d47b9ad4a99723bc389136265",
        custom = "x_tools",
        custom_git = "https://github.com/golang/tools.git",
        importpath = "golang.org/x/tools",
        patch_args = ["-p1"],
        patches = ["@io_bazel_rules_go//third_party:org_golang_x_tools-extras.patch"],
    )

    maybe(
        go_repository,
        name = "org_golang_x_text",
        custom = "x_text",
        custom_git = "https://github.com/golang/text.git",
        importpath = "golang.org/x/text",
        tag = "v0.3.0",
    )

    maybe(
        go_repository,
        name = "org_golang_x_net",
        commit = "3a22650c66bd",
        custom = "x_net",
        custom_git = "https://github.com/golang/net.git",
        importpath = "golang.org/x/net",
    )

    maybe(
        go_repository,
        name = "com_github_pkg_errors",
        custom = "errors",
        importpath = "github.com/pkg/errors",
        tag = "v0.8.1",
    )

    maybe(
        go_repository,
        name = "org_bitbucket_creachadair_stringset",
        custom = "stringset",
        custom_git = "https://bitbucket.org/creachadair/stringset.git",
        importpath = "bitbucket.org/creachadair/stringset",
        tag = "v0.0.3",
    )

    maybe(
        go_repository,
        name = "org_bitbucket_creachadair_shell",
        custom = "shell",
        custom_git = "https://bitbucket.org/creachadair/shell.git",
        importpath = "bitbucket.org/creachadair/shell",
        tag = "v0.0.5",
    )

    maybe(
        go_repository,
        name = "org_golang_google_grpc",
        custom = "grpc",
        custom_git = "https://github.com/grpc/grpc-go.git",
        importpath = "google.golang.org/grpc",
        tag = "v1.16.0",
    )

    maybe(
        go_repository,
        name = "org_golang_x_oauth2",
        commit = "d2e6202438beef2727060aa7cabdd924d92ebfd9",
        custom = "x_oauth2",
        custom_git = "https://github.com/golang/oauth2.git",
        importpath = "golang.org/x/oauth2",
    )

    maybe(
        go_repository,
        name = "com_github_google_go_querystring",
        custom = "querystring",
        importpath = "github.com/google/go-querystring",
        tag = "v1.0.0",
    )

    maybe(
        go_repository,
        name = "com_github_apache_beam",
        build_file_proto_mode = "disable",
        commit = "3cf3a4708c588e32e85a1d4cfdb29ab5a0203efc",
        custom = "beam",
        importpath = "github.com/apache/beam",
    )

    maybe(
        go_repository,
        name = "com_github_googleapis_gax_go",
        build_file_proto_mode = "disable",
        custom = "googleapis_gax",
        importpath = "github.com/googleapis/gax-go",
        tag = "v1.0.1",
    )

    maybe(
        go_repository,
        name = "org_golang_google_api",
        commit = "3097bf831ede4a24e08a3316254e29ca726383e3",
        custom = "google_api",
        custom_git = "https://github.com/google/google-api-go-client.git",
        importpath = "google.golang.org/api",
    )

    maybe(
        go_repository,
        name = "com_google_cloud_go",
        custom = "google_cloud",
        custom_git = "https://github.com/GoogleCloudPlatform/google-cloud-go.git",
        importpath = "cloud.google.com/go",
        tag = "v0.26.0",
    )

    maybe(
        go_repository,
        name = "io_opencensus_go",
        custom = "opencensus",
        custom_git = "https://github.com/census-instrumentation/opencensus-go.git",
        importpath = "go.opencensus.io",
        tag = "v0.15.0",
    )

    maybe(
        go_repository,
        name = "com_github_syndtr_goleveldb",
        commit = "5d6fca44a948d2be89a9702de7717f0168403d3d",
        importpath = "github.com/syndtr/goleveldb",
    )

    maybe(
        go_repository,
        name = "com_github_minio_highwayhash",
        custom = "highwayhash",
        importpath = "github.com/minio/highwayhash",
        tag = "v1.0.0",
    )

    maybe(
        go_repository,
        name = "org_golang_x_sys",
        commit = "49385e6e15226593f68b26af201feec29d5bba22",
        custom = "x_sys",
        custom_git = "https://github.com/golang/sys.git",
        importpath = "golang.org/x/sys",
    )

    maybe(
        go_repository,
        name = "com_github_datadog_zstd",
        custom = "zstd",
        importpath = "github.com/DataDog/zstd",
        tag = "v1.3.5",
    )

    maybe(
        go_repository,
        name = "com_github_beevik_etree",
        custom = "etree",
        importpath = "github.com/beevik/etree",
        tag = "v1.1.0",
    )

    maybe(
        go_repository,
        name = "com_github_google_orderedcode",
        importpath = "github.com/google/orderedcode",
        tag = "v0.0.1",
    )

    maybe(
        go_repository,
        name = "com_github_ghodss_yaml",
        commit = "c7ce16629ff4cd059ed96ed06419dd3856fd3577",
        custom = "ghodss_yaml",
        importpath = "github.com/ghodss/yaml",
    )

    maybe(
        go_repository,
        name = "in_gopkg_yaml_v2",
        custom = "yaml",
        importpath = "gopkg.in/yaml.v2",
        tag = "v2.2.2",
    )

    maybe(
        go_repository,
        name = "com_github_mholt_archiver",
        custom = "archiver",
        importpath = "github.com/mholt/archiver",
        tag = "v3.1.1",
    )

    maybe(
        go_repository,
        name = "com_github_dsnet_compress",
        custom = "compress",
        importpath = "github.com/dsnet/compress",
        tag = "v0.0.1",
    )

    maybe(
        go_repository,
        name = "com_github_nwaples_rardecode",
        custom = "rardecode",
        importpath = "github.com/nwaples/rardecode",
        tag = "v1.0.0",
    )

    maybe(
        go_repository,
        name = "com_github_pierrec_lz4",
        custom = "lz4",
        importpath = "github.com/pierrec/lz4",
        tag = "v2.0.8",
    )

    maybe(
        go_repository,
        name = "com_github_ulikunitz_xz",
        commit = "590df8077fbcb06ad62d7714da06c00e5dd2316d",
        custom = "xz",
        importpath = "github.com/ulikunitz/xz",
    )

    maybe(
        go_repository,
        name = "com_github_xi2_xz",
        commit = "48954b6210f8d154cb5f8484d3a3e1f83489309e",
        custom = "xi2xz",
        importpath = "github.com/xi2/xz",
    )

    maybe(
        http_archive,
        name = "org_brotli_go",
        sha256 = "fb511e09ea284fcd18fe2a2632744609a77f69c345428b9f0d2cc15171215f06",
        strip_prefix = "brotli-ee2a5e1540cbd6ef883a897499d9596307f7f7f9/go",
        url = "https://github.com/google/brotli/archive/ee2a5e1540cbd6ef883a897499d9596307f7f7f9.zip",
    )

def _bindings():
    maybe(
        native.bind,
        name = "vnames_config",
        actual = "@io_kythe//kythe/data:vnames_config",
    )

    maybe(
        native.bind,
        name = "libuuid",
        actual = "@io_kythe//third_party:libuuid",
    )

    maybe(
        native.bind,
        name = "libmemcached",
        actual = "@org_libmemcached_libmemcached//:libmemcached",
    )

    maybe(
        native.bind,
        name = "guava",  # required by @com_google_protobuf
        actual = "@io_kythe//third_party/guava",
    )

    maybe(
        native.bind,
        name = "gson",  # required by @com_google_protobuf
        actual = "@com_google_code_gson_gson//jar",
    )

    maybe(
        native.bind,
        name = "zlib",  # required by @com_google_protobuf
        actual = "@net_zlib//:zlib",
    )

def _extractor_image_dependencies():
    """Defines external repositories necessary for extractor images."""
    maybe(
        http_archive,
        name = "com_github_philwo_bazelisk",
        sha256 = "cb6a208f559fd08d205527b69d597ef36f7e1a922fe1df64081e52dd544f7666",
        strip_prefix = "bazelisk-0.0.2",
        urls = ["https://github.com/philwo/bazelisk/archive/0.0.2.zip"],
    )
    go_repository(
        name = "com_github_hashicorp_go_version",
        importpath = "github.com/hashicorp/go-version",
        tag = "v1.1.0",
    )

def _sample_ui_dependencies():
    """Defines external repositories necessary for building the sample UI."""
    lein_repository(
        name = "org_leiningen",
        sha256 = "a0a1f093677045c4e1e40219ccc989acd61433f61c50e098a2185faf4f03553c",
        version = "2.5.3",
    )

def kythe_dependencies(sample_ui = True):
    """Defines external repositories for Kythe dependencies.

    Call this once in your WORKSPACE file to load all @io_kythe dependencies.
    """
    _cc_dependencies()
    _go_dependencies()
    _java_dependencies()

    # proto_library, cc_proto_library, and java_proto_library rules implicitly
    # depend on @com_google_protobuf for protoc and proto runtimes.
    # TODO(justbuchanan): update to the next tagged release when available
    maybe(
        http_archive,
        name = "com_google_protobuf",
        sha256 = "ef62ee52bedc3a0ec0aeb7911279243119d53eac7f8bbbec833761c07e802bcb",
        strip_prefix = "protobuf-8e5ea65953f3c47e01bca360ecf3abdf2c8b1c33",
        urls = ["https://github.com/protocolbuffers/protobuf/archive/8e5ea65953f3c47e01bca360ecf3abdf2c8b1c33.zip"],
    )

    maybe(
        http_archive,
        name = "bazel_skylib",
        sha256 = "ca4e3b8e4da9266c3a9101c8f4704fe2e20eb5625b2a6a7d2d7d45e3dd4efffd",
        strip_prefix = "bazel-skylib-0.5.0",
        urls = ["https://github.com/bazelbuild/bazel-skylib/archive/0.5.0.zip"],
    )

    _rule_dependencies()
    if sample_ui:
        _sample_ui_dependencies()
    _bindings()
    _extractor_image_dependencies()
