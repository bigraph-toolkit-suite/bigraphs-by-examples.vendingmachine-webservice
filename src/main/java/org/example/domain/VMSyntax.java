package org.example.domain;

import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.impl.signature.DynamicSignatureBuilder;
import org.bigraphs.spring.data.cdo.CdoTemplate;
import org.eclipse.emf.cdo.common.model.CDOPackageRegistry;
import org.eclipse.emf.ecore.EPackage;

import static org.bigraphs.framework.core.factory.BigraphFactory.createOrGetBigraphMetaModel;
import static org.bigraphs.framework.core.factory.BigraphFactory.pureSignatureBuilder;

/**
 * The bigraphical signature of the vending machine system.
 *
 * @author Dominik Grzelak
 */
public class VMSyntax {
    public final static String NSURI = "org.bigraphs";
    public final static EMetaModelData eMetaModelData = EMetaModelData.builder()
            .setName("vm").setNsPrefix("bigraphMetaModel").setNsUri(VMSyntax.NSURI).create();
    public final static EPackage BIGRAPH_META_MODEL;

    static {
        EPackage ePackage = createOrGetBigraphMetaModel(new VMSyntax().sig());
        BIGRAPH_META_MODEL = ePackage;
    }

    public DefaultDynamicSignature sig() {
        DynamicSignatureBuilder sb = pureSignatureBuilder();
        DefaultDynamicSignature sig = sb
                .addControl("Coin", 0)
                .addControl("VM", 0)
                .addControl("Button1", 0)
                .addControl("Button2", 0)
                .addControl("Pressed", 0)
                .addControl("Coffee", 0)
                .addControl("Container", 0)
                .addControl("Tea", 0)
                .addControl("PHD", 0)
                .addControl("Wallet", 0)
                .addControl("Tresor", 0)
                .create();
        return sig;
    }

    public void initPackageRepositories(CdoTemplate template) {

        // (!) loading from FS prodces some errors later when storing in CDO because of some proxy issue or CDO bug
        //java.lang.IllegalStateException: Unresolvable proxy
//        String metaModelFilename = "metamodel/vm.ecore";
//        EPackage bigraphMetaModel = BigraphArtifacts.loadBigraphMetaModel(ResourceLoader.getResourceURL(metaModelFilename).getPath());

        EPackage bigraphMetaModel = createOrGetBigraphMetaModel(sig(), eMetaModelData);
        EPackage.Registry.INSTANCE.put(VMSyntax.NSURI, bigraphMetaModel);
        CDOPackageRegistry.INSTANCE.put(VMSyntax.NSURI, bigraphMetaModel);
        template.getCDOPackageRegistry().put(VMSyntax.NSURI, bigraphMetaModel);
    }

    public EPackage getBigraphMetaModel() {
        return (EPackage) EPackage.Registry.INSTANCE.get(VMSyntax.NSURI);
    }
}
