package org.example.domain.data;


import org.bigraphs.framework.core.impl.pure.PureBigraph;
import org.bigraphs.framework.core.impl.pure.PureBigraphBuilder;
import org.bigraphs.framework.core.impl.signature.DefaultDynamicSignature;
import org.bigraphs.framework.core.utils.BigraphUtil;
import org.bigraphs.framework.core.utils.emf.EMFUtils;
import org.bigraphs.spring.data.cdo.CdoTemplate;
import org.bigraphs.spring.data.cdo.core.listener.CdoNewObjectsActionDelegate;
import org.bigraphs.spring.data.cdo.core.listener.filter.FilterCriteria;
import org.eclipse.emf.cdo.common.revision.CDOIDAndVersion;
import org.eclipse.emf.cdo.util.CDOUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.example.domain.VMSyntax;
import org.example.repository.VMRepository;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.bigraphs.framework.core.utils.BigraphUtil.toBigraph;

/**
 * CDO listener for the vending machine object, i.e., the agent of the VM system.
 *
 * @author Dominik Grzelak
 */
public class VendingMachineListener implements CdoNewObjectsActionDelegate {

    CdoTemplate template;
    //    VMRepository repository;
    VMSyntax vmSyntax;

    private final List<PropertyChangeListener> listener = new ArrayList<>();

    public VendingMachineListener(VMSyntax vmSyntax, CdoTemplate template) { //}, VMRepository repository) {
        this.template = template;
//        this.repository = repository;
        this.vmSyntax = vmSyntax;
    }

    /**
     * Add a standard Java property listener to publish the results from the CDO server back to the
     * original object.
     * <p>
     * The method {@link #notifyListeners} is called then, when on the CDO server side a change is
     * detected.
     *
     * @param newListener a listener
     */
    public void addChangeListener(PropertyChangeListener newListener) {
        listener.add(newListener);
    }

    private void notifyListeners(String property, Object oldValue, Object newValue) {
        for (PropertyChangeListener name : listener) {
            name.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
        }
    }

    @Override
    public void perform(List<CDOIDAndVersion> arg, Map<String, Object> properties) {
        if (arg.isEmpty()) return;
        System.out.println("New objects received: " + arg);

        if (properties == null || properties.size() == 0) return;
        System.out.println("From repoPath: " + properties.get(FilterCriteria.Key.REPOSITORY_PATH));
        String actualRepoPath = (String) properties.get(FilterCriteria.Key.REPOSITORY_PATH);
        if (actualRepoPath == null || !actualRepoPath.contains("agent")) {
            System.out.println("Refusing agent listener because no agent object was passed here.");
            return;
        }

        CDOIDAndVersion cdoidAndVersion = arg.get(0);
        System.out.println("cdoidAndVersion: " + cdoidAndVersion);
//        template.find(cdoidAndVersion.getID(), VendingMachineObject.class, actualRepoPath);
//        EObject eObject = template.find(cdoidAndVersion.getID(), EObject.class, actualRepoPath);
        Optional<EObject> byId = Optional.of(template.find(cdoidAndVersion.getID(), EObject.class, actualRepoPath)); // = repository.findById(cdoidAndVersion.getID());
        if (byId.isPresent()) {
            EObject anotherInstance = byId.get();
            System.out.println("Instance could be loaded: " + anotherInstance);
            try {
                // (!) no copy otherwise CDOID gets lost - we need to remain a connection to CDO
//                EObject copy = EcoreUtil.copy(EMFUtils.getRootContainer(anotherInstance));
                EObject copy = (EMFUtils.getRootContainer(anotherInstance));
                PureBigraph tmp = BigraphUtil.toBigraph(copy.eClass().getEPackage(), copy, vmSyntax.sig());
                notifyListeners(VendingMachineObject.ID, null, CDOUtil.getCDOObject(copy).cdoID());
                notifyListeners(VendingMachineObject.BIGRAPH, null, tmp);
                System.out.println("\tPureBigraph in Listener: " + tmp + " with CDOID: " + cdoidAndVersion.getID() + "/ " + CDOUtil.getCDOObject(anotherInstance).cdoID());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
