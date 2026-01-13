plugins {
  `java-library`
  id("application")
  alias(libs.plugins.shadow)
}

dependencies {
  implementation(project(":dist:bom:controlplane-base-bom"))
  implementation(project(":dist:bom:dataplane-base-bom"))
  implementation(project(":extensions:common:iam:iam-mock"))
  implementation(project(":extensions:data-plane:data-plane-self-registration"))
}

application {
  mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
  mergeServiceFiles()
  archiveFileName.set("minimal.jar")
}

edcBuild {
  publish.set(false)
}
