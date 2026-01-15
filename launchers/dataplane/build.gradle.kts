plugins {
  `java-library`
  id("application")
  alias(libs.plugins.shadow)
}

dependencies {
  runtimeOnly(project(":dist:bom:dataplane-base-bom"))
  runtimeOnly(project(":extensions:data-plane:data-plane-http"))
  runtimeOnly(project(":extensions:data-plane:data-plane-signaling:data-plane-signaling-api"))
  runtimeOnly(project(":extensions:data-plane:data-plane-self-registration"))
  runtimeOnly(project(":extensions:data-plane:data-plane-public-api"))
}

application {
  mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.shadowJar {
  duplicatesStrategy = DuplicatesStrategy.INCLUDE
  mergeServiceFiles()
  archiveFileName.set("dataplane.jar")
}

edcBuild {
  publish.set(false)
}
