package org.example.domain;

import org.bigraphs.framework.core.datatypes.EMetaModelData;
import org.bigraphs.framework.core.impl.signature.DynamicSignature;
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

    public DynamicSignature sig() {
        DynamicSignatureBuilder sb = pureSignatureBuilder();
        DynamicSignature sig = sb
                .add("Coin", 0)
                .add("VM", 0)
                .add("Button1", 0)
                .add("Button2", 0)
                .add("Pressed", 0)
                .add("Coffee", 0)
                .add("Container", 0)
                .add("Tea", 0)
                .add("PHD", 0)
                .add("Wallet", 0)
                .add("Tresor", 0)
                .create();
        return sig;
    }

    public void initPackageRepositories(CdoTemplate template) {

        // (!) loading from FS produces some errors later when storing in CDO because of some proxy issue or CDO bug
        //java.lang.IllegalStateException: Unresolvable proxy
//        String metaModelFilename = "metamodel/vm.ecore";
//        EPackage bigraphMetaModel = BigraphArtifacts.loadBigraphMetaModel(ResourceLoader.getResourceURL(metaModelFilename).getPath());

        EPackage bigraphMetaModel = createOrGetBigraphMetaModel(sig(), eMetaModelData);
        String nsURI = bigraphMetaModel.getNsURI();
        EPackage.Registry.INSTANCE.put(nsURI, bigraphMetaModel);
        CDOPackageRegistry.INSTANCE.put(nsURI, bigraphMetaModel);
        template.getCDOPackageRegistry().put(nsURI, bigraphMetaModel);
    }

    public EPackage getBigraphMetaModel() {
        return (EPackage) EPackage.Registry.INSTANCE.get(VMSyntax.NSURI);
    }
}
