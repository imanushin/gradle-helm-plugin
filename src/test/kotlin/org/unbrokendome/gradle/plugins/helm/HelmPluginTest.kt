package org.unbrokendome.gradle.plugins.helm

import assertk.all
import assertk.assertThat
import assertk.assertions.containsOnly
import assertk.assertions.each
import assertk.assertions.extracting
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isSuccess
import assertk.assertions.prop
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Task
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import org.unbrokendome.gradle.plugins.helm.command.tasks.HelmAddRepository
import org.unbrokendome.gradle.plugins.helm.dsl.Filtering
import org.unbrokendome.gradle.plugins.helm.dsl.HelmChart
import org.unbrokendome.gradle.plugins.helm.dsl.HelmExtension
import org.unbrokendome.gradle.plugins.helm.dsl.HelmRepository
import org.unbrokendome.gradle.plugins.helm.dsl.helm
import org.unbrokendome.gradle.plugins.helm.dsl.repositories
import org.unbrokendome.gradle.pluginutils.test.assertions.assertk.containsTask
import org.unbrokendome.gradle.pluginutils.test.assertions.assertk.hasExtension
import org.unbrokendome.gradle.pluginutils.test.assertions.assertk.isPresent
import org.unbrokendome.gradle.pluginutils.test.assertions.assertk.taskDependencies
import org.unbrokendome.gradle.pluginutils.test.evaluate
import org.unbrokendome.gradle.pluginutils.test.spek.applyPlugin
import org.unbrokendome.gradle.pluginutils.test.spek.setupGradleProject
import java.net.URI


object HelmPluginTest : Spek({

    val project by setupGradleProject { applyPlugin<HelmPlugin>() }


    describe("applying the helm plugin") {

        it("project can be evaluated successfully") {
            assertThat {
                project.evaluate()
            }.isSuccess()
        }


        it("should create a helm DSL extension") {
            assertThat(project)
                .hasExtension<HelmExtension>("helm")
        }


        it("should create a helm filtering DSL extension") {
            assertThat(project)
                .hasExtension<HelmExtension>("helm")
                .hasExtension<Filtering>("filtering")
        }


        it("should create a helm charts DSL extension") {
            assertThat(project)
                .hasExtension<HelmExtension>("helm")
                .hasExtension<NamedDomainObjectContainer<HelmChart>>("charts")
        }
    }


    describe("repositories") {

        it("should create a helm repositories DSL extension") {
            assertThat(project)
                .hasExtension<HelmExtension>("helm")
                .hasExtension<NamedDomainObjectContainer<HelmRepository>>("repositories")
        }


        it("should create a HelmAddRepository task for each repository") {
            with(project.helm.repositories) {
                create("myRepo") { repo ->
                    repo.url.set(project.uri("http://repository.example.com"))
                }
            }

            assertThat(project)
                .containsTask<HelmAddRepository>("helmAddMyRepoRepository")
                .prop(HelmAddRepository::url)
                .isPresent().isEqualTo(URI("http://repository.example.com"))
        }


        it("should create a helmAddRepositories task that registers all repos") {

            with(project.helm.repositories) {
                create("myRepo1") { repo ->
                    repo.url.set(project.uri("http://repository1.example.com"))
                }
                create("myRepo2") { repo ->
                    repo.url.set(project.uri("http://repository2.example.com"))
                }
            }

            assertThat(project)
                .containsTask<Task>("helmAddRepositories")
                .taskDependencies.all {
                    each { it.isInstanceOf(HelmAddRepository::class) }
                    extracting { it.name }.containsOnly("helmAddMyRepo1Repository", "helmAddMyRepo2Repository")
                }
        }
    }
})
