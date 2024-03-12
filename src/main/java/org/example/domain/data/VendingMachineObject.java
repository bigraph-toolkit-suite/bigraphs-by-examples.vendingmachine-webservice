package org.example.domain.data;

import org.bigraphs.framework.core.Bigraph;
import org.bigraphs.framework.core.impl.elementary.Placings;
import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.model.bigraphBaseModel.BBigraph;
import org.bigraphs.spring.data.cdo.CdoTemplate;
import org.bigraphs.spring.data.cdo.annotation.CDO;
import org.bigraphs.spring.data.cdo.annotation.EObjectModel;
import org.bigraphs.spring.data.cdo.core.listener.filter.CdoListenerFilter;
import org.bigraphs.spring.data.cdo.core.listener.filter.FilterCriteria;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.session.CDOSessionInvalidationEvent;
import org.eclipse.emf.ecore.EObject;
import org.example.domain.VMSyntax;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Id;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.bigraphs.framework.core.factory.BigraphFactory.*;
import static org.example.domain.data.VendingMachineObject.REPOSITORY_PATH;

/**
 * This class represents a "storeable" object for a CDO repository.
 * Basically, it is just a wrapper for a Ecore instance model of a standard {@link PureBigraph} object,
 * additionally providing the necessary information for the {@link org.example.repository.VMRepository}.
 * <p>
 * It attaches a database listener of type {@link VendingMachineListener} to the CDO database to react on changes
 * from the database side.
 * The changes from the database are propagates back to the members of this class via the functionality of
 * the {@link PropertyChangeListener} interface that this class implements.
 *
 * @author Dominik Grzelak
 */
//nsUri must be the same value as when creating a bigraph by using "EMetaModelData"
@CDO(nsUri = VMSyntax.NSURI, path = REPOSITORY_PATH, packageName = "vm")
public class VendingMachineObject implements PropertyChangeListener {
    public static final String REPOSITORY_PATH = "/system/agent";
    public static final String BIGRAPH = "bigraph"; // variable name
    public static final String MODEL = "emodel"; // variable name
    public static final String ID = "cdoid"; // variable name

    //    @Autowired
//    public VMSyntax vmSyntax;
    @Autowired
    public VendingMachineListener vendingMachineListener;
    @Autowired
    private CdoTemplate template;

    /**
     * A factory method to dynamically create the initial agent of this application.
     *
     * @param numOfCoffee
     * @param numOfTea
     * @param numOfCoinsPhd
     * @param vmSyntax      the signature
     * @return a bigraph representing the entire VM system
     */
    public static PureBigraph createAgent(int numOfCoffee, int numOfTea, int numOfCoinsPhd, VMSyntax vmSyntax) {
        try {
            return agent(numOfCoffee, numOfTea, numOfCoinsPhd, vmSyntax.sig());
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }


    @Id
    public CDOID id;
    @EObjectModel(ofClass = BBigraph.class)
    public EObject eModel;
    public PureBigraph bigraph;


    public VendingMachineObject() {
    }

//    public VendingMachineObject(CdoTemplate template, PureBigraph bigraph, VMSyntax vmSyntax, VendingMachineListener vendingMachineListener) {
//        this.vmSyntax = vmSyntax;
//        this.bigraph = bigraph;
//        this.eModel = bigraph.getModel();
//        this.vendingMachineListener = vendingMachineListener;
//        this.template = template;
//    }

    public void init(PureBigraph bigraph) {
        this.bigraph = bigraph;
        this.eModel = bigraph.getInstanceModel();
        storeAgentInCdo();
        setupCdoListener();
    }

    private void storeAgentInCdo() {
        template.insert(eModel, REPOSITORY_PATH);
    }

    public void setupCdoListener() {
        CdoListenerFilter filter = CdoListenerFilter
                .filter(
                        new FilterCriteria().byRepositoryPath(REPOSITORY_PATH)
                )
                .restrict(CDOSessionInvalidationEvent.class);
        template.addListener(filter, vendingMachineListener);
        vendingMachineListener.addChangeListener(this);
    }

    /**
     * A helper method for the factory method to dynamically create the initial agent of this application.
     *
     * @param numOfCoffee
     * @param numOfTea
     * @param numOfCoinsPhd
     * @param sig           the signature
     * @return a pure bigraph representing the entire vending machine system
     * @throws Exception
     */
    static PureBigraph agent(int numOfCoffee, int numOfTea, int numOfCoinsPhd, DefaultDynamicSignature sig)
            throws Exception {
        PureBigraphBuilder<DefaultDynamicSignature> vmB = pureBuilder(sig);
        PureBigraphBuilder<DefaultDynamicSignature> phdB = pureBuilder(sig);

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy containerCoffee = vmB.hierarchy("Container");
        for (int i = 0; i < numOfCoffee; i++) {
            containerCoffee = containerCoffee.addChild("Coffee");
        }
        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy containerTea = vmB.hierarchy("Container");
        for (int i = 0; i < numOfTea; i++) {
            containerTea = containerTea.addChild("Tea");
        }
        vmB.createRoot()
                .addChild("VM")
                .down()
                .addChild(containerCoffee.top())
                .addChild(containerTea.top())
                .addChild("Button1")
                .addChild("Button2")
                .addChild("Tresor")
        ;

        PureBigraphBuilder<DefaultDynamicSignature>.Hierarchy wallet = vmB.hierarchy("Wallet");
        for (int i = 0; i < numOfCoinsPhd; i++) {
            wallet = wallet.addChild("Coin");
        }
        phdB.createRoot().addChild("PHD")
                .down()
                .addChild(wallet.top());


        Placings<DefaultDynamicSignature> placings = purePlacings(sig);
        Placings<DefaultDynamicSignature>.Merge merge2 = placings.merge(2);
        PureBigraph vm = vmB.createBigraph();
        PureBigraph phd = phdB.createBigraph();
        Bigraph<DefaultDynamicSignature> both = ops(vm).parallelProduct(phd).getOuterBigraph();
        Bigraph<DefaultDynamicSignature> result = ops(merge2).compose(both).getOuterBigraph();
        return (PureBigraph) result;
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        System.out.println("Changed property: " + event.getPropertyName() + " [old -> "
                + event.getOldValue() + "] | [new -> " + event.getNewValue() + "]");
        if (event.getPropertyName().equals(BIGRAPH)) {
            this.bigraph = (PureBigraph) event.getNewValue();
        }
        if (event.getPropertyName().equals(MODEL)) {
            this.eModel = (EObject) event.getNewValue();
        }
        if (event.getPropertyName().equals(ID)) {
            this.id = (CDOID) event.getNewValue();
        }
    }
}
