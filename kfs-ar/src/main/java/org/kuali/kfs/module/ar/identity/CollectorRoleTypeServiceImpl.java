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
package org.kuali.kfs.module.ar.identity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.coa.identity.OrganizationHierarchyAwareRoleTypeServiceBase;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.rice.core.api.uif.RemotableAttributeError;
import org.kuali.rice.core.api.uif.RemotableAttributeError.Builder;

/**
 * Role Type Service for the CGB Collector role, used to perform validation and matching of role qualifiers.
 */
public class CollectorRoleTypeServiceImpl extends OrganizationHierarchyAwareRoleTypeServiceBase {

    /**
     * qualification matches roleQualifiers if customer name matches,
     * and if chart/org match (only if they are included - they're optional)
     *
     * @see org.kuali.rice.kns.kim.type.DataDictionaryTypeServiceBase#performMatch(java.util.Map, java.util.Map)
     */
    @Override
    public boolean performMatch(Map<String,String> qualification, Map<String,String> roleQualifier) {
        boolean matches = false;

        matches = doesCustomerMatch(qualification, roleQualifier);
        String billingChartOfAccountsCode = qualification.get(ArKimAttributes.BILLING_CHART_OF_ACCOUNTS_CODE);
        String billingOrganizationCode = qualification.get(ArKimAttributes.BILLING_ORGANIZATION_CODE);
        String processingChartOfAccountsCode = qualification.get(ArKimAttributes.PROCESSING_CHART_OF_ACCOUNTS_CODE);
        String processingOrganizationCode = qualification.get(ArKimAttributes.PROCESSING_ORGANIZATION_CODE);

        // only test chart/org if either billing chart/org or processing chart/org are populated
        // otherwise we only care if customer matches
        if ((StringUtils.isNotBlank(billingChartOfAccountsCode) && StringUtils.isNotBlank(billingOrganizationCode) ||
                (StringUtils.isNotBlank(processingChartOfAccountsCode) && StringUtils.isNotBlank(processingOrganizationCode)))) {
            matches &= (doesOrgMatch(billingChartOfAccountsCode, billingOrganizationCode, ArKimAttributes.BILLING_CHART_OF_ACCOUNTS_CODE, ArKimAttributes.BILLING_ORGANIZATION_CODE, roleQualifier)
                    || doesOrgMatch(processingChartOfAccountsCode, processingOrganizationCode, ArKimAttributes.PROCESSING_CHART_OF_ACCOUNTS_CODE, ArKimAttributes.PROCESSING_ORGANIZATION_CODE, roleQualifier));
        }

        return matches;
    }

    /**
     * Does a check to see if the chart/org passed in match either the billing chart/org or processing chart/org
     * on the role qualifier. Is org hierarchy aware and descends the org hierarchy to do this check.
     *
     * @param chartOfAccountsCode chart code to check against billing or processing chart in role qualifiers
     * @param organizationCode org code to check against billing or processing org in role qualifiers
     * @param roleQualifier role qualifier containing either billing chart/org or processing chart/org
     * @return true if the passed in chart/org match, false otherwise
     */
    protected boolean doesOrgMatch(String chartOfAccountsCode, String organizationCode, String chartOfAccountsCodeKey, String organizationCodeKey, Map<String, String> roleQualifier) {
        boolean orgMatches = false;

        String chart = roleQualifier.get(chartOfAccountsCodeKey);
        String org = roleQualifier.get(organizationCodeKey);

        // only billing chart/org or processing chart/org will be populated, and we don't want to call isParentOrg
        // with null values, so we need to check for empty values first before calling isParentOrg
        if (StringUtils.isNotEmpty(chart) && StringUtils.isNotEmpty(org) &&
                StringUtils.isNotEmpty(chartOfAccountsCode) && StringUtils.isNotEmpty(organizationCode) &&
                isParentOrg(chartOfAccountsCode, organizationCode, chart, org, true)) {
            orgMatches = true;
        }

        return orgMatches;
    }

    /**
     * Check if customer name matches customer name starting letter and ending letter
     *
     * If customer name isn't passed in the role qualification or customer name starting letter and
     * customer name ending letter qualifiers aren't in the roleQualifier, return true because we want
     * to match all customers in that case.
     *
     * If the customer name and starting/ending letter qualifiers are passed in, check to see if the first letter
     * of the customer name falls between the starting and ending letter qualifiers - if not, return false as the
     * name doesn't match the qualifiers, otherwise return true for a match.
     *
     * @param qualification
     * @param roleQualifier
     * @return
     */
    protected boolean doesCustomerMatch(Map<String, String> qualification, Map<String, String> roleQualifier) {
        boolean customerMatches = true;

        if (qualification != null && !qualification.isEmpty() && roleQualifier != null && !roleQualifier.isEmpty()) {
            String customerName = qualification.get(ArKimAttributes.CUSTOMER_NAME);
            if (StringUtils.isNotEmpty(customerName)) {
                char startingQualificationLetter = customerName.charAt(0);
                char endingQualificationLetter = customerName.charAt(0);
                String startingQualifierLetter = roleQualifier.get(ArKimAttributes.CUSTOMER_NAME_STARTING_LETTER);
                String endingQualifierLetter = roleQualifier.get(ArKimAttributes.CUSTOMER_NAME_ENDING_LETTER);

                if (StringUtils.isNotEmpty(startingQualifierLetter) && StringUtils.isNotEmpty(endingQualifierLetter)) {
                    if (startingQualificationLetter < startingQualifierLetter.charAt(0) ||
                            endingQualificationLetter > endingQualifierLetter.charAt(0)) {
                        customerMatches = false;
                    }
                }
            }
        }

        return customerMatches;
    }

    /**
     * note: for validating Collector role - either billing chart/org OR processing chart/org are required
     * and starting and ending letters are all or nothing: they aren't required, but if either one is entered, both need to be entered
     *
     * @see org.kuali.rice.kns.kim.type.DataDictionaryTypeServiceBase#validateAttributes(java.lang.String, java.util.Map)
     */
    @Override
    public List<RemotableAttributeError> validateAttributes(String kimTypeId, Map<String,String> attributes) {
        List<RemotableAttributeError> errors = new ArrayList<RemotableAttributeError>();
        errors.addAll(super.validateAttributes(kimTypeId, attributes));

        Builder errorBuilder = null;

        String billingChartCode = attributes.get(ArKimAttributes.BILLING_CHART_OF_ACCOUNTS_CODE);
        String billingOrganizationCode = attributes.get(ArKimAttributes.BILLING_ORGANIZATION_CODE);
        String processingChartCode = attributes.get(ArKimAttributes.PROCESSING_CHART_OF_ACCOUNTS_CODE);
        String processingOrganizationCode = attributes.get(ArKimAttributes.PROCESSING_ORGANIZATION_CODE);

        // either billing chart/org OR processing chart/org are required
        if (StringUtils.isNotEmpty(billingChartCode)) {
            if (StringUtils.isEmpty(billingOrganizationCode)) {
                errorBuilder = RemotableAttributeError.Builder.create(ArKimAttributes.BILLING_ORGANIZATION_CODE, ArKeyConstants.ERROR_BILLINGCHART_OR_BILLINGORG_NOTEMPTY_ALL_REQUIRED);
                errors.add(errorBuilder.build());
            }

            if (StringUtils.isNotEmpty(processingChartCode)) {
                errorBuilder = RemotableAttributeError.Builder.create(ArKimAttributes.PROCESSING_CHART_OF_ACCOUNTS_CODE, ArKeyConstants.ERROR_EITHER_BILLINGCHART_OR_PROCESSCHART_REQUIRED_NOT_BOTH);
                errors.add(errorBuilder.build());
            }
            if (StringUtils.isNotEmpty(processingOrganizationCode)) {
                errorBuilder = RemotableAttributeError.Builder.create(ArKimAttributes.PROCESSING_ORGANIZATION_CODE, ArKeyConstants.ERROR_EITHER_BILLINGCHART_OR_PROCESSCHART_REQUIRED_NOT_BOTH);
                errors.add(errorBuilder.build());
            }
        } else {
            if (StringUtils.isNotEmpty(billingOrganizationCode)) {
                errorBuilder = RemotableAttributeError.Builder.create(ArKimAttributes.BILLING_CHART_OF_ACCOUNTS_CODE, ArKeyConstants.ERROR_BILLINGCHART_OR_BILLINGORG_NOTEMPTY_ALL_REQUIRED);
                errors.add(errorBuilder.build());

                if (StringUtils.isNotEmpty(processingChartCode)) {
                    errorBuilder = RemotableAttributeError.Builder.create(ArKimAttributes.PROCESSING_CHART_OF_ACCOUNTS_CODE, ArKeyConstants.ERROR_EITHER_BILLINGCHART_OR_PROCESSCHART_REQUIRED_NOT_BOTH);
                    errors.add(errorBuilder.build());
                }
                if (StringUtils.isNotEmpty(processingOrganizationCode)) {
                    errorBuilder = RemotableAttributeError.Builder.create(ArKimAttributes.PROCESSING_ORGANIZATION_CODE, ArKeyConstants.ERROR_EITHER_BILLINGCHART_OR_PROCESSCHART_REQUIRED_NOT_BOTH);
                    errors.add(errorBuilder.build());
                }
            } else {
                if (StringUtils.isEmpty(processingChartCode)) {
                    if (StringUtils.isNotEmpty(processingOrganizationCode)) {
                        errorBuilder = RemotableAttributeError.Builder.create(ArKimAttributes.PROCESSING_ORGANIZATION_CODE, ArKeyConstants.ERROR_PROCESSCHART_OR_PROCESSORG_NOTEMPTY_ALL_REQUIRED);
                        errors.add(errorBuilder.build());
                    } else {
                        errorBuilder = RemotableAttributeError.Builder.create(ArKimAttributes.BILLING_CHART_OF_ACCOUNTS_CODE, ArKeyConstants.ERROR_EITHER_BILLINGCHART_OR_PROCESSCHART_REQUIRED_NOT_BOTH);
                        errors.add(errorBuilder.build());
                    }
                } else {
                    if (StringUtils.isEmpty(processingOrganizationCode)) {
                        errorBuilder = RemotableAttributeError.Builder.create(ArKimAttributes.BILLING_CHART_OF_ACCOUNTS_CODE, ArKeyConstants.ERROR_EITHER_BILLINGCHART_OR_PROCESSCHART_REQUIRED_NOT_BOTH);
                        errors.add(errorBuilder.build());
                    }
                }
            }
        }

        String startingLetter = attributes.get(ArKimAttributes.CUSTOMER_NAME_STARTING_LETTER);
        String endingLetter = attributes.get(ArKimAttributes.CUSTOMER_NAME_ENDING_LETTER);

        // starting and ending letters are all or nothing:
        // they aren't required, but if either one is entered, both need to be entered
        if (StringUtils.isNotEmpty(startingLetter)) {
            if (StringUtils.isNotEmpty(endingLetter)) {
                char customerNameStartingLetter = attributes.get(ArKimAttributes.CUSTOMER_NAME_STARTING_LETTER).charAt(0);
                char customerNameEndingLetter = attributes.get(ArKimAttributes.CUSTOMER_NAME_ENDING_LETTER).charAt(0);

                if (customerNameStartingLetter > customerNameEndingLetter) {
                    errorBuilder = RemotableAttributeError.Builder.create(ArKimAttributes.CUSTOMER_NAME_STARTING_LETTER, ArKeyConstants.ERROR_STARTLETTER_AFTER_ENDLETTER);
                    errors.add(errorBuilder.build());
                }
            } else {
                errorBuilder = RemotableAttributeError.Builder.create(ArKimAttributes.CUSTOMER_NAME_STARTING_LETTER, ArKeyConstants.ERROR_STARTLETTER_OR_ENDLETTER_NOTEMPTY_ALL_REQUIRED);
                errors.add(errorBuilder.build());
            }
        } else if (StringUtils.isNotEmpty(endingLetter)) {
            errorBuilder = RemotableAttributeError.Builder.create(ArKimAttributes.CUSTOMER_NAME_STARTING_LETTER, ArKeyConstants.ERROR_STARTLETTER_OR_ENDLETTER_NOTEMPTY_ALL_REQUIRED);
            errors.add(errorBuilder.build());
        }

        return errors;
    }

}
