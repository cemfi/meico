package meico.mei;

import meico.supplementary.KeyValue;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class gives access to elements that reference other elements,
 * e.g. by attributes startid and endid. The elements are accessed via
 * the reference IDs they have. This focusses only on internal references
 * starting with '#'.
 * (key=referenceID, value=list of elements that refer to the referenceID)
 */
class ElementsByReferenceIds extends HashMap<String, ArrayList<ElementsByReferenceIds.ElementWithReferences>> {
    /**
     * constructor
     */
    public ElementsByReferenceIds(Document document) {
        super();

        Nodes allElements = document.getRootElement().query("descendant-or-self::*"); // get all element in the document
        allElements.forEach(node -> {                           // for each element
            Element element = (Element) node;                       // cast to Element
            this.put(element);                                      // try creating an entry to this from the element
        });
    }

    /**
     * add an entry to the HashMap
     * @param element
     * @return
     */
    public boolean put(Element element) {
        ElementWithReferences ewr = ElementWithReferences.create(element);  // try to create an instance of ElementWithReferences
        if (ewr == null)
            return false;

        // get all the element's references and add the corresponding HashMap entry
        boolean success = false;
        for (String key : ewr.getValue().keySet()) {            // for each reference ID create an entry in this
            ArrayList<ElementWithReferences> value = this.computeIfAbsent(key, k -> new ArrayList<>()); // if the entry does not exist so far, create it
            success = success || value.add(ewr);                // add ewr to the entry's list
        }

        return success;
    }

    /**
     * This class represents an element and the reference IDs to other
     * elements that it has in its attributes.
     *
     */
    protected static class ElementWithReferences extends KeyValue<Element, ElementWithReferences.AttributesByReferenceId> {
        /**
         * constructor
         * @param element
         */
        private ElementWithReferences(Element element, AttributesByReferenceId referencesWithAttributes) {
            super(element, referencesWithAttributes);
        }

        /**
         * factory
         * @param element
         * @return
         */
        public static ElementWithReferences create(Element element) {
            AttributesByReferenceId abr = AttributesByReferenceId.create(element);
            if (abr == null)
                return null;
            return new ElementWithReferences(element, abr);
        }

        /**
         * This class is a HashMap with all references that the provided element has
         * and a list of the attributes where each reference is the value.
         */
        protected static class AttributesByReferenceId extends HashMap<String, ArrayList<Attribute>> {
            /**
             * constructor
             * @param element
             */
            private AttributesByReferenceId(Element element) {
                super();

                for (int a = 0; a < element.getAttributeCount(); ++a) {     // for each attribute
                    Attribute attribute = element.getAttribute(a);          // get the attribute
                    String key = attribute.getValue();                      // get its value
                    if (!key.startsWith("#"))                               // if it does not start with '#', it is no reference
                        continue;                                           // done

                    key = key.substring(1);                                 // get the ID without '#'
                    ArrayList<Attribute> value = this.computeIfAbsent(key, k -> new ArrayList<>()); // if the entry does not yet exist, it is created
                    value.add(attribute);                                   // add the attribute to the list of attributes that refer to the ID
                }
            }

            /**
             * factory
             * @param element
             * @return
             */
            public static AttributesByReferenceId create(Element element) {
                if (element == null)
                    return null;

                AttributesByReferenceId rwa = new AttributesByReferenceId(element);
                if (rwa.isEmpty())
                    return null;

                return rwa;
            }
        }
    }
}
