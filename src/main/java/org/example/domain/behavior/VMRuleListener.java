package org.example.domain.behavior;

import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraph;
import de.tudresden.inf.st.bigraphs.core.impl.pure.PureBigraphBuilder;
import de.tudresden.inf.st.bigraphs.core.impl.signature.DefaultDynamicSignature;
import de.tudresden.inf.st.bigraphs.core.utils.emf.EMFUtils;
import de.tudresden.inf.st.bigraphs.dsl.bDSL.AbstractMainStatements;
import de.tudresden.inf.st.bigraphs.models.bigraphBaseModel.BBigraph;
import de.tudresden.inf.st.spring.data.cdo.CdoTemplate;
import de.tudresden.inf.st.spring.data.cdo.core.listener.CdoChangedObjectsActionDelegate;
import de.tudresden.inf.st.spring.data.cdo.core.listener.CdoNewObjectsActionDelegate;
import de.tudresden.inf.st.spring.data.cdo.core.listener.filter.FilterCriteria;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.cdo.common.model.EMFUtil;
import org.eclipse.emf.cdo.common.revision.CDOIDAndVersion;
import org.eclipse.emf.cdo.common.revision.CDORevisionKey;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.example.domain.VMSyntax;
import org.example.repository.VMRepository;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generic CDO listener for all rules of the VM system.
 *
 * @author Dominik Grzelak
 */
public class VMRuleListener implements CdoNewObjectsActionDelegate {

    CdoTemplate template;
    VMSyntax vmSyntax;

    private final List<PropertyChangeListener> listener = new ArrayList<PropertyChangeListener>();

    public VMRuleListener(VMSyntax vmSyntax, CdoTemplate template) {
        this.template = template;
        this.vmSyntax = vmSyntax;
    }

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
        if (arg.size() == 0) return;
        System.out.println("New objects received: " + arg);

        if (properties == null || properties.size() == 0) return;
        System.out.println("From repoPath: " + properties.get(FilterCriteria.Key.REPOSITORY_PATH));
        String actualRepoPath = (String) properties.get(FilterCriteria.Key.REPOSITORY_PATH);
        if (actualRepoPath == null || !actualRepoPath.contains("rules")) {
            System.out.println("Refusing rule listener because no rule objects were passed here.");
            return;
        }

        CDOIDAndVersion cdoidAndVersion = arg.get(0);
        EObject bBigraph = template.find(cdoidAndVersion.getID(), EObject.class, actualRepoPath);
        if (bBigraph != null) {
            // (!) no copy otherwise CDOID gets lost - we need to remain a connection to CDO
//            EObject containerOfType = EcoreUtil.copy(EMFUtils.getRootContainer(bBigraph));
            EObject containerOfType = (EMFUtils.getRootContainer(bBigraph));
            PureBigraphBuilder<DefaultDynamicSignature> b = PureBigraphBuilder.create(vmSyntax.sig(), vmSyntax.getBigraphMetaModel(), containerOfType);
            PureBigraph tmp = b.createBigraph();
            notifyListeners(actualRepoPath, null, tmp);
        }
    }

    private String tryGetRepositoryPath(CDOID cdoid) {
        String actualRepoPath = null;
        BBigraph bBigraph = template.find(cdoid, BBigraph.class, null);
        if (bBigraph != null) {
            String resPath = bBigraph.cdoResource().getPath();
            List<EObject> all = template.findAll(EObject.class, resPath);
            if (all.stream().findFirst().isPresent())
                actualRepoPath = resPath;
        }
        return actualRepoPath;
    }
}
