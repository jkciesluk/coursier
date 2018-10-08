package coursier.test

import coursier.{ Dependency, Module, Cache }
import coursier.test.compatibility._

import scala.async.Async.{ async, await }

import utest._

object IvyLocalTests extends TestSuite {

  val tests = TestSuite{
    'coursier {
      val module = Module("io.get-coursier", "coursier-core_2.11")
      val version = coursier.util.Properties.version

      val extraRepos = Seq(Cache.ivy2Local)

      // Assuming this module (and the sub-projects it depends on) is published locally
      'resolution - CentralTests.resolutionCheck(
        module, version,
        extraRepos
      )

      'uniqueArtifacts - async {

        val res = await(CentralTests.resolve(
          Set(Dependency(Module("io.get-coursier", "coursier-cli_2.12"), version, transitive = false)),
          extraRepos = extraRepos
        ))

        val artifacts = res.dependencyArtifacts(classifiers = Some(Seq("standalone")))
          .filter(t => t._2.`type` == "jar" && !t._3.optional)
          .map(_._3)
          .map(_.url)
          .groupBy(s => s)

        assert(artifacts.nonEmpty)
        assert(artifacts.forall(_._2.length == 1))
      }


      'javadocSources - async {
        val res = await(CentralTests.resolve(
          Set(Dependency(module, version)),
          extraRepos = extraRepos
        ))

        val artifacts = res.dependencyArtifacts().filter(_._2.`type` == "jar").map(_._3.url)
        val anyJavadoc = artifacts.exists(_.contains("-javadoc"))
        val anySources = artifacts.exists(_.contains("-sources"))

        assert(!anyJavadoc)
        assert(!anySources)
      }
    }
  }

}
