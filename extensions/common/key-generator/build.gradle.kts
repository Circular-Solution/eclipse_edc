plugins {
  `java-library`
}

dependencies {
  api(project(":spi:common:core-spi"))
  implementation("com.nimbusds:nimbus-jose-jwt:9.37.3")
}
