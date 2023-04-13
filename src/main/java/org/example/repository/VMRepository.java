package org.example.repository;

import de.tudresden.inf.st.spring.data.cdo.repository.CdoRepository;
import org.eclipse.emf.cdo.common.id.CDOID;
import org.eclipse.emf.ecore.EObject;
import org.example.domain.data.VendingMachineObject;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for objects of type {@link VendingMachineObject}.
 *
 * @author Dominik Grzelak
 */
@Repository
public interface VMRepository extends CdoRepository<VendingMachineObject, CDOID> {

}