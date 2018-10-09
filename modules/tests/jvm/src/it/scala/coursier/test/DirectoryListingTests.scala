package coursier.test

import coursier._
import coursier.core.{Authentication, Type}
import utest._

object DirectoryListingTests extends TestSuite {

  val user = sys.env("TEST_REPOSITORY_USER")
  val password = sys.env("TEST_REPOSITORY_PASSWORD")

  val repo = MavenRepository(
    sys.env.getOrElse("TEST_REPOSITORY", sys.error("TEST_REPOSITORY not set")),
    authentication = Some(Authentication(user, password))
  )

  val module = Module(org"com.abc", name"test")
  val version = "0.1"

  val tests = Tests {
    'jar - CentralTests.withArtifacts(
      module,
      version,
      attributes = Attributes(Type.jar),
      extraRepos = Seq(repo)
    ) {
      artifacts =>
        assert(artifacts.length == 1)
        assert(artifacts.headOption.exists(_.url.endsWith(".jar")))
    }

    'jarFoo - CentralTests.withArtifacts(
      module,
      version,
      attributes = Attributes(Type("jar-foo")),
      extraRepos = Seq(repo)
    ) {
      artifacts =>
        assert(artifacts.length == 1)
        assert(artifacts.headOption.exists(_.url.endsWith(".jar-foo")))
    }
  }

}