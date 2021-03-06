/*
 * Created on 09 set 2015
 * Copyright 2015 by Andrea Vacondio (andrea.vacondio@gmail.com).
 * This file is part of Sejda.
 *
 * Sejda is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Sejda is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Sejda.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.sejda.impl.sambox.component;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.sejda.impl.sambox.component.SignatureClipper.clipSignature;
import static org.sejda.sambox.cos.COSName.A;
import static org.sejda.sambox.cos.COSName.AF;
import static org.sejda.sambox.cos.COSName.AP;
import static org.sejda.sambox.cos.COSName.AS;
import static org.sejda.sambox.cos.COSName.BM;
import static org.sejda.sambox.cos.COSName.BORDER;
import static org.sejda.sambox.cos.COSName.BS;
import static org.sejda.sambox.cos.COSName.C;
import static org.sejda.sambox.cos.COSName.CONTENTS;
import static org.sejda.sambox.cos.COSName.DA;
import static org.sejda.sambox.cos.COSName.DATAPREP;
import static org.sejda.sambox.cos.COSName.DS;
import static org.sejda.sambox.cos.COSName.DV;
import static org.sejda.sambox.cos.COSName.F;
import static org.sejda.sambox.cos.COSName.FF;
import static org.sejda.sambox.cos.COSName.FT;
import static org.sejda.sambox.cos.COSName.H;
import static org.sejda.sambox.cos.COSName.I;
import static org.sejda.sambox.cos.COSName.KIDS;
import static org.sejda.sambox.cos.COSName.LOCK;
import static org.sejda.sambox.cos.COSName.M;
import static org.sejda.sambox.cos.COSName.MAX_LEN;
import static org.sejda.sambox.cos.COSName.MK;
import static org.sejda.sambox.cos.COSName.NM;
import static org.sejda.sambox.cos.COSName.OC;
import static org.sejda.sambox.cos.COSName.OPT;
import static org.sejda.sambox.cos.COSName.P;
import static org.sejda.sambox.cos.COSName.PARENT;
import static org.sejda.sambox.cos.COSName.PMD;
import static org.sejda.sambox.cos.COSName.Q;
import static org.sejda.sambox.cos.COSName.RECT;
import static org.sejda.sambox.cos.COSName.RV;
import static org.sejda.sambox.cos.COSName.STRUCT_PARENT;
import static org.sejda.sambox.cos.COSName.SUBTYPE;
import static org.sejda.sambox.cos.COSName.SV;
import static org.sejda.sambox.cos.COSName.T;
import static org.sejda.sambox.cos.COSName.TI;
import static org.sejda.sambox.cos.COSName.TM;
import static org.sejda.sambox.cos.COSName.TU;
import static org.sejda.sambox.cos.COSName.TYPE;
import static org.sejda.sambox.cos.COSName.V;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.sejda.common.LookupTable;
import org.sejda.model.pdf.form.AcroFormPolicy;
import org.sejda.sambox.cos.COSArray;
import org.sejda.sambox.cos.COSBase;
import org.sejda.sambox.cos.COSDictionary;
import org.sejda.sambox.cos.COSName;
import org.sejda.sambox.pdmodel.PDDocument;
import org.sejda.sambox.pdmodel.interactive.annotation.PDAnnotation;
import org.sejda.sambox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.sejda.sambox.pdmodel.interactive.form.PDAcroForm;
import org.sejda.sambox.pdmodel.interactive.form.PDField;
import org.sejda.sambox.pdmodel.interactive.form.PDFieldFactory;
import org.sejda.sambox.pdmodel.interactive.form.PDNonTerminalField;
import org.sejda.sambox.pdmodel.interactive.form.PDTerminalField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Component providing methods to merge multiple acroforms together using different strategies.
 * 
 * @author Andrea Vacondio
 */
public class AcroFormsMerger {
    private static final Logger LOG = LoggerFactory.getLogger(AcroFormsMerger.class);

    private static final COSName[] FIELD_KEYS = { FT, PARENT, KIDS, T, TU, TM, FF, V, DV, DA, Q, DS, RV, OPT, MAX_LEN,
            TI, I, LOCK, SV, DATAPREP };

    private static final COSName[] WIDGET_KEYS = { TYPE, SUBTYPE, RECT, CONTENTS, P, NM, M, F, AP, AS, BORDER, C,
            STRUCT_PARENT, OC, AF, BM, H, MK, A, BS, PMD };

    private AcroFormPolicy policy;
    private PDAcroForm form;
    private String random = Long.toString(UUID.randomUUID().getMostSignificantBits(), 36);
    private Long counter = 0L;

    private final BiFunction<PDTerminalField, LookupTable<PDField>, PDTerminalField> createOrReuseTerminalField = (
            PDTerminalField existing, LookupTable<PDField> fieldsLookup) -> {
        PDField previouslyCreated = ofNullable(getField(existing.getFullyQualifiedName()))
                .orElseGet(() -> fieldsLookup.lookup(existing));
        if (previouslyCreated == null) {
            previouslyCreated = PDFieldFactory.createFielAddingChildToParent(this.form,
                    existing.getCOSObject().duplicate(),
                    (PDNonTerminalField) fieldsLookup.lookup(existing.getParent()));
            previouslyCreated.getCOSObject().removeItem(COSName.KIDS);
            fieldsLookup.addLookupEntry(existing, previouslyCreated);
        }
        if (!previouslyCreated.isTerminal()) {
            LOG.warn("Cannot merge terminal field because a non terminal field with the same name already exsts: "
                    + existing.getFullyQualifiedName());
            return null;
        }
        return (PDTerminalField) previouslyCreated;
    };

    private final BiFunction<PDTerminalField, LookupTable<PDField>, PDTerminalField> createRenamingTerminalField = (
            PDTerminalField existing, LookupTable<PDField> fieldsLookup) -> {
        PDTerminalField newField = (PDTerminalField) PDFieldFactory.createFielAddingChildToParent(this.form,
                existing.getCOSObject().duplicate(), (PDNonTerminalField) fieldsLookup.lookup(existing.getParent()));
        if (getField(existing.getFullyQualifiedName()) != null || fieldsLookup.hasLookupFor(existing)) {
            newField.setPartialName(String.format("%s%s%d", existing.getPartialName(), random, ++counter));
            LOG.info("Existing terminal field renamed from {} to {}", existing.getPartialName(),
                    newField.getPartialName());
        }
        newField.getCOSObject().removeItem(COSName.KIDS);
        fieldsLookup.addLookupEntry(existing, newField);
        return newField;
    };

    private final BiConsumer<PDField, LookupTable<PDField>> createOrReuseNonTerminalField = (PDField field,
            LookupTable<PDField> fieldsLookup) -> {
        if (getField(field.getFullyQualifiedName()) == null && !fieldsLookup.hasLookupFor(field)) {
            fieldsLookup.addLookupEntry(field, PDFieldFactory.createFielAddingChildToParent(this.form,
                    field.getCOSObject().duplicate(), (PDNonTerminalField) fieldsLookup.lookup(field.getParent())));
        }
    };

    private PDField getField(String fullyQualifiedName) {
        if(fullyQualifiedName == null) {
            return null;
        }

        return form.getField(fullyQualifiedName);
    }

    private final BiConsumer<PDField, LookupTable<PDField>> createRenamingNonTerminalField = (PDField field,
            LookupTable<PDField> fieldsLookup) -> {
        PDField newField = PDFieldFactory.createFielAddingChildToParent(this.form, field.getCOSObject().duplicate(),
                (PDNonTerminalField) fieldsLookup.lookup(field.getParent()));
        if (getField(field.getFullyQualifiedName()) != null || fieldsLookup.hasLookupFor(field)) {
            newField.setPartialName(String.format("%s%s%d", field.getPartialName(), random, ++counter));
            LOG.info("Existing non terminal field renamed from {} to {}", field.getPartialName(),
                    newField.getPartialName());
        }
        fieldsLookup.addLookupEntry(field, newField);
    };

    public AcroFormsMerger(AcroFormPolicy policy, PDDocument destination) {
        this.policy = policy;
        this.form = new PDAcroForm(destination);
    }

    /**
     * Merge the original form to the current one, considering only fields whose widgets are available in the given lookup table.
     * 
     * @param originalForm
     *            the form to merge
     * @param annotationsLookup
     *            lookup for relevant annotations
     */
    public void mergeForm(PDAcroForm originalForm, LookupTable<PDAnnotation> annotationsLookup) {
        if (originalForm != null) {
            if (originalForm.hasXFA()) {
                LOG.warn("Merge of XFA forms is not supported");
            } else {
                LOG.debug("Merging acroforms with policy {}", policy);
                switch (policy) {
                case MERGE_RENAMING_EXISTING_FIELDS:
                    filterNonWidgetsFields(annotationsLookup);
                    updateForm(originalForm, annotationsLookup, createRenamingTerminalField,
                            createRenamingNonTerminalField);
                    break;
                case MERGE:
                    filterNonWidgetsFields(annotationsLookup);
                    updateForm(originalForm, annotationsLookup, createOrReuseTerminalField,
                            createOrReuseNonTerminalField);
                    break;
                case FLATTEN:
                    filterNonWidgetsFields(annotationsLookup);
                    updateForm(originalForm, annotationsLookup, createRenamingTerminalField,
                            createRenamingNonTerminalField);
                    flatten();
                    break;
                default:
                    LOG.debug("Discarding acroform");
                }
            }
        } else {
            LOG.debug("Skipped acroform merge, nothing to merge");
        }
    }

    /**
     * For each new widget annotation in the lookup table removes all the Field keys.
     * 
     * @param annotationsLookup
     */
    private void filterNonWidgetsFields(LookupTable<PDAnnotation> annotationsLookup) {
        for (PDAnnotation current : annotationsLookup.values()) {
            if (current instanceof PDAnnotationWidget) {
                current.getCOSObject().removeItems(FIELD_KEYS);
            }
        }
        LOG.debug("Removed fields keys from widget annotations");
    }

    private void updateForm(PDAcroForm originalForm, LookupTable<PDAnnotation> widgets,
            BiFunction<PDTerminalField, LookupTable<PDField>, PDTerminalField> getTerminalField,
            BiConsumer<PDField, LookupTable<PDField>> createNonTerminalField) {
        mergeFormDictionary(originalForm);
        LookupTable<PDField> fieldsLookup = new LookupTable<>();
        for (PDField field : originalForm.getFieldTree()) {
            if (!field.isTerminal()) {
                createNonTerminalField.accept(field, fieldsLookup);
            } else {
                List<PDAnnotationWidget> relevantWidgets = findMappedWidgetsFor((PDTerminalField) field, widgets);
                if (!relevantWidgets.isEmpty()) {
                    PDTerminalField terminalField = getTerminalField.apply((PDTerminalField) field, fieldsLookup);
                    if (nonNull(terminalField)) {
                        for (PDAnnotationWidget widget : relevantWidgets) {
                            terminalField.addWidgetIfMissing(widget);
                        }
                        terminalField.getCOSObject().removeItems(WIDGET_KEYS);
                    }
                } else {
                    LOG.info("Discarded not relevant field {}", field.getPartialName());
                }
            }
        }
        this.form.addFields(originalForm.getFields().stream().map(fieldsLookup::lookup).filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    private void mergeFormDictionary(PDAcroForm originalForm) {
        if (!form.isNeedAppearances() && originalForm.isNeedAppearances()) {
            form.setNeedAppearances(true);
        }
        String da = originalForm.getDefaultAppearance();
        if (isBlank(form.getDefaultAppearance()) && !isBlank(da)) {
            form.setDefaultAppearance(da);
        }
        int quadding = originalForm.getCOSObject().getInt(COSName.Q);
        if (quadding != -1 && !form.getCOSObject().containsKey(COSName.Q)) {
            form.setQuadding(quadding);
        }
        final COSDictionary formResources = ofNullable(form.getCOSObject().getDictionaryObject(COSName.DR))
                .map(r -> (COSDictionary) r).orElseGet(COSDictionary::new);
        ofNullable(originalForm.getCOSObject().getDictionaryObject(COSName.DR)).map(r -> (COSDictionary) r)
                .ifPresent(dr -> {
                    for (COSName currentKey : dr.keySet()) {
                        ofNullable(dr.getDictionaryObject(currentKey)).ifPresent(value -> {
                            if (value instanceof COSDictionary) {
                                mergeResourceDictionaryValue(formResources, (COSDictionary) value, currentKey);
                            } else if (value instanceof COSArray) {
                                mergeResourceArrayValue(formResources, (COSArray) value, currentKey);
                            } else {
                                LOG.warn("Unsupported resource dictionary type {}", value);
                            }
                        });
                    }
                });
        form.getCOSObject().setItem(COSName.DR, formResources);
        LOG.debug("Merged AcroForm dictionary");
    }

    private void mergeResourceArrayValue(COSDictionary formResources, COSArray value, COSName currentKey) {
        COSArray currentItem = ofNullable(formResources.getDictionaryObject(currentKey)).map(i -> (COSArray) i)
                .orElseGet(COSArray::new);
        for (COSBase item : value) {
            if (!currentItem.contains(item)) {
                currentItem.add(item);
            }
        }
        formResources.setItem(currentKey, currentItem);
    }

    private void mergeResourceDictionaryValue(final COSDictionary formResources, COSDictionary value,
            COSName currentKey) {
        COSDictionary currentItem = ofNullable(formResources.getDictionaryObject(currentKey))
                .map(i -> (COSDictionary) i).orElseGet(COSDictionary::new);
        currentItem.mergeWithoutOverwriting(value);
        formResources.setItem(currentKey, currentItem);
    }

    /**
     * @param field
     * @param widgets
     * @return the list of relevant widgets for the given field.
     */
    private List<PDAnnotationWidget> findMappedWidgetsFor(PDTerminalField field, LookupTable<PDAnnotation> widgets) {
        return field.getWidgets().stream().map(widgets::lookup).filter(w -> w instanceof PDAnnotationWidget)
                .map(w -> (PDAnnotationWidget) w).collect(Collectors.toList());

    }

    public boolean hasForm() {
        return !form.getFields().isEmpty();
    }

    private void flatten() {
        try {
            form.flatten();
        } catch(IOException ex) {
            LOG.warn("Failed to flatten form", ex);
        }
    }

    /**
     * Performs some cleanup task on the resulting {@link PDAcroForm} and then returns it.
     * 
     * @return
     */
    public PDAcroForm getForm() {
        for (PDField current : form.getFieldTree()) {
            if (!current.isTerminal() && !((PDNonTerminalField) current).hasChildren()) {
                LOG.info("Removing non terminal field with no child {}", current.getPartialName());
                if (nonNull(current.getParent())) {
                    current.getParent().removeChild(current);
                } else {
                    // it's a root field
                    form.removeField(current);
                }
            } else if (clipSignature(current)) {
                form.setSignaturesExist(true);
            }
        }
        return form;
    }
}
