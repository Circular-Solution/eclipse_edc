plugins {
  `java-library`
}

dependencies {
  api(project(":spi:data-plane:data-plane-spi"))
  api(project(":spi:common:core-spi"))
  api(project(":spi:common:web-spi"))
  implementation("com.nimbusds:nimbus-jose-jwt:9.37.3")
  implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
}
