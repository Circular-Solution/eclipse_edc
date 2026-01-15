package org.eclipse.edc.extension;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import org.eclipse.edc.runtime.metamodel.annotation.Extension;
import org.eclipse.edc.runtime.metamodel.annotation.Inject;
import org.eclipse.edc.spi.security.Vault;
import org.eclipse.edc.spi.system.ServiceExtension;
import org.eclipse.edc.spi.system.ServiceExtensionContext;

@Extension("Key Generator Extension")
public class KeyGeneratorExtension implements ServiceExtension {

  @Inject
  private Vault vault;

  @Override
  public void initialize(ServiceExtensionContext context) {
    try {
      var key = new ECKeyGenerator(Curve.P_256)
          .keyID("private-key")
          .generate();
      vault.storeSecret("private-key", key.toJSONString());
      vault.storeSecret("public-key", key.toPublicJWK().toJSONString());
    } catch (JOSEException e) {
      throw new RuntimeException("Failed to generate signing keys", e);
    }
  }
}
