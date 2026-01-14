plugins {
  `java-library`
  id("application")
  alias(libs.plugins.shadow)
}

dependencies {
  runtimeOnly(project(":dist:bom:controlplane-base-bom"))
  runtimeOnly(project(":dist:bom:dataplane-base-bom"))
  runtimeOnly(project(":extensions:common:iam:iam-mock"))
  runtimeOnly(project(":extensions:common:iam:decentralized-identity:identity-did-core"))
}

application {
  mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.shadowJar {
  duplicatesStrategy = DuplicatesStrategy.INCLUDE
  mergeServiceFiles()
  archiveFileName.set("minimal.jar")
}

edcBuild {
  publish.set(false)
}
