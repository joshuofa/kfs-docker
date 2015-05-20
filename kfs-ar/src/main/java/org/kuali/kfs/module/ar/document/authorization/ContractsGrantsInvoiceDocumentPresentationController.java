/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2014 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.module.ar.document.authorization;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.ar.ArAuthorizationConstants;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.document.ContractsGrantsInvoiceDocument;
import org.kuali.kfs.sys.document.FinancialSystemTransactionalDocument;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;
import org.kuali.rice.kew.api.WorkflowDocument;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.ObjectUtils;

/**
 * Contracts & Grants Invoice Document Presentation Controller class.
 */
public class ContractsGrantsInvoiceDocumentPresentationController extends CustomerInvoiceDocumentPresentationController {

    /**
     * @see org.kuali.kfs.module.ar.document.authorization.ContractsGrantsInvoiceDocumentPresentationController#canErrorCorrect(org.kuali.kfs.sys.document.FinancialSystemTransactionalDocument)
     */
    @Override
    public boolean canErrorCorrect(FinancialSystemTransactionalDocument document) {
        if (StringUtils.isNotBlank(document.getFinancialSystemDocumentHeader().getCorrectedByDocumentId())) {
            return false;
        }
        if (((ContractsGrantsInvoiceDocument) document).isInvoiceReversal()) {
            return false;
        }
        else {
            // a normal invoice can only be error corrected if document is in a final state
            // and no amounts have been applied (excluding discounts)
            return isDocFinalWithNoAppliedAmountsExceptDiscounts((ContractsGrantsInvoiceDocument) document);
        }
    }

    /**
     * CINVs created by the Letter of Credit process should be forever read only
     * @see org.kuali.rice.krad.document.DocumentPresentationControllerBase#canEdit(org.kuali.rice.krad.document.Document)
     */
    @Override
    public boolean canEdit(Document document) {
        final boolean canEdit = super.canEdit(document);
        if (canEdit) {
            final ContractsGrantsInvoiceDocument contractsGrantsInvoice = (ContractsGrantsInvoiceDocument)document;
            if (StringUtils.equalsIgnoreCase(contractsGrantsInvoice.getInvoiceGeneralDetail().getAward().getBillingFrequencyCode(), ArConstants.LOC_BILLING_SCHEDULE_CODE)) {
                return false;
            }
        }

        return canEdit;
    }



    public boolean canProrate(ContractsGrantsInvoiceDocument document) {
        return canEdit(document) &&
                getParameterService().getParameterValueAsBoolean(KfsParameterConstants.ACCOUNTS_RECEIVABLE_ALL.class, ArConstants.CG_PRORATE_BILL_IND) &&
                !StringUtils.equals(ArConstants.MILESTONE_BILLING_SCHEDULE_CODE, document.getInvoiceGeneralDetail().getBillingFrequencyCode()) &&
                !StringUtils.equals(ArConstants.PREDETERMINED_BILLING_SCHEDULE_CODE, document.getInvoiceGeneralDetail().getBillingFrequencyCode());
    }

    public boolean canModifyTransmissionDate(ContractsGrantsInvoiceDocument document) {
        if (document.hasInvoiceBeenCorrected()) {
            return false;
        }

        if (document.isInvoiceReversal()) {
            return false;
        }

        if (StringUtils.equals(ArConstants.LOC_BILLING_SCHEDULE_CODE, document.getInvoiceGeneralDetail().getBillingFrequencyCode())) {
            return false;
        }

        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        return (ObjectUtils.isNotNull(workflowDocument) && (workflowDocument.isProcessed() || workflowDocument.isFinal()));
    }

    @Override
    public Set<String> getEditModes(Document document) {
        Set<String> editModes = super.getEditModes(document);

        WorkflowDocument workflowDocument = document.getDocumentHeader().getWorkflowDocument();
        if (canModifyTransmissionDate((ContractsGrantsInvoiceDocument) document)) {
            editModes.add(ArAuthorizationConstants.ContractsGrantsInvoiceDocumentEditMode.MODIFY_TRANSMISSION_DATE);
        }

        return editModes;
    }

    @Override
    public Set<String> getDocumentActions(Document document) {
        Set<String> documentActions = super.getDocumentActions(document);
        documentActions.remove(KRADConstants.KUALI_ACTION_CAN_COPY);
        return documentActions;
    }

}
