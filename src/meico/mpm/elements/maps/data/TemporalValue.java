package meico.mpm.elements.maps.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

/**
 * This class interfaces a temporal value with its domain.
 * TemporalValues can be processed with each other.
 * Thereby, e.g. the resulting absolute value can be derived
 * by applying a relative TemporalValue to a TemporalValue
 * @author Lars Engeln
 */
public class TemporalValue {
    /**
     * Enumeration of supported domains
     */
    public enum Domain {
        Unknown,
        Relative,                                                   // % - e.g. 80% (of something, e.g. of a TemporalValue with absolute domain)
        Milliseconds,                                               // ms - e.g. 1000ms
        Ticks,                                                      // ticks - e.g. 720ticks (i.e., corresponding to a quarter note, depending on the PPQ)
        //Notelength // 8th, 16th, ..
    }
    private static final Map<Domain, String> domainStrings;         // Map with 'unit' strings of the Domains meant for displaying
    static {
        Map<Domain, String> map = new HashMap<>();                  // filling in the Domain unit strings
        map.put(Domain.Milliseconds, "ms");
        map.put(Domain.Relative, "%");
        map.put(Domain.Ticks, "ticks");
        //map.put(Domain.Notelength, "th");
        map.put(Domain.Unknown, "?");
        domainStrings = Collections.unmodifiableMap(map);
    }
    private static final Map<Domain, String> domainNameStrings;     // Map with strings of the Domain names
    static {
        Map<Domain, String> map = new HashMap<>();                  // filling in the Domain names
        map.put(Domain.Milliseconds, "milliseconds");
        map.put(Domain.Relative, "relative");
        map.put(Domain.Ticks, "ticks");
        //map.put(Domain.Notelength, "note length");
        map.put(Domain.Unknown, "unknown");
        domainNameStrings = Collections.unmodifiableMap(map);
    }

    private double value = 0.0;                                     // the value
    private Domain domain = Domain.Unknown;                         // the domain of the value

    private TemporalValue relationTo = null;                        // the TemporalValue to what this is in relation to

    /**
     * constructor, generates an instance with initial values
     * @param value
     * @param domain
     */
    private TemporalValue(double value, Domain domain) {
        setDomain(domain);
        setValue(value);
    }

    /**
     * TemporalValue factory
     * @param value
     * @param domain
     * @return a new TemporalValue
     */
    public static TemporalValue create(double value, Domain domain) {
        return new TemporalValue(value, domain);
    }

    /**
     * creates a new TemporalValue that is in relation to relativeTo
     * @param relativeTo
     * @return the relative TemporalValue to relativeTo
     */
    public static TemporalValue createInRelationTo(TemporalValue relativeTo) {
        TemporalValue temporal = new TemporalValue(relativeTo.value, Domain.Relative);
        temporal.setRelation(relativeTo);
        return temporal;
    }

    /**
     * creates a new TemporalValue as clone of this
     * @return the cloned TemporalValue
     */
    public TemporalValue clone() {
        TemporalValue temporal = TemporalValue.create(this.value, this.domain);
        if(hasRelation())
            temporal.setRelation(this.relationTo);
        return temporal;
    }

    /**
     * return the temporal value
     * @return value
     */
    public double getValue() {
        return value;
    }

    /**
     * sets value as new temporal value
     * @param value
     */
    public void setValue(double value) {
        this.value = value;
    }
    /**
     * sets value of temporal as new temporal value
     * @param temporal
     */
    public void setValue(TemporalValue temporal) {
        setValue(temporal.getValue());
        setDomain(temporal.getDomain());
    }
    /**
     * tries to set value from string, if it contains domain string, domain is set as well!
     * @param valueString
     */
    public void setValue(String valueString) {
        fromString(valueString);
    }

    /**
     * return the temporal domain
     * @return domain
     */
    public Domain getDomain() {
        return domain;
    }

    /**
     * sets domain as new temporal domain, temporal value remains untouched
     * @param domain
     */
    public void setDomain(Domain domain) {
        if(domain == null)
            return;
        this.domain = domain;
    }

    /**
     * tries to set domain from string
     * @param domainString
     */
    public void setDomain(String domainString) {
        setDomain(fromDomainString(domainString));
    }

    /**
     * return the TempralValue to what this is in relation to
     * @return the TemporalValue that this TemporalValue is in relation to
     */
    public TemporalValue getRelation() {
        return relationTo;
    }

    /**
     * sets relation to what this is relative to. relation stack cannot be pure relative, it needs somewhen an absolute value for calculation
     * @param relation
     */
    public void setRelation(TemporalValue relation) {
        if(!relation.hasAbsoluteRoot())
            return;
        relationTo = relation;
    }

    /**
     * removes the relation
     */
    public void removeRelation() {
        relationTo = null;
    }

    /**
     * returns if this has a relation
     * @return true if this has a relation
     */
    public boolean hasRelation() {
        return relationTo != null;
    }
    /**
     * returns if the relation stack has an absolute root
     * @return true if the relation stack has an absolute root
     */
    public boolean hasAbsoluteRoot() {
        if(!isRelative())
            return true;
        if(relationTo == null)
            return false;
        return relationTo.hasAbsoluteRoot();
    }

    /**
     * returns a TemporalValue object that is relative in its value to the given value.
     * @param value
     * @return
     */
    public TemporalValue getRelativeTo(double value) {
        TemporalValue relative = create(value, Domain.Relative);

        if(getValue() == value) {
            relative.setValue(100);
            return relative;
        }

        double greaterValue     = Math.max(getValue(), value);
        double lesserValue      = Math.min(getValue(), value);
        double relativeValue    = (lesserValue * 100) / greaterValue;
        //double relativeValue      = (getValue() * 100) / value;
        relative.setValue(relativeValue);

        return relative;
    }
    /**
     * returns a TemporalValue object that is relative regarding its value to temporal's value.
     * @param temporal
     * @return the relative TemporalValue
     */
    public TemporalValue getRelativeTo(TemporalValue temporal) {
        if(temporal == null)
            return null;
        if(hasSameDomain(temporal))                         // if we have the same Domain (absolute or relative),
            return getRelativeTo(temporal.getValue());      // directly get the relative of us
                                                            // otherwise we need to process
        if(!isRelative() && !temporal.isRelative())         // if non of us is relative,
            return null;                                    // we cannot get a relative value of us,
                                                            // as no conversion e.g. from ticks to milliseconds can be done here (at this very moment)
        TemporalValue relative;
        TemporalValue absolute;

        if (temporal.isRelative()) {                        // get the relative one of us
            absolute = this;
            relative = temporal.clone();
        }
        else {
            absolute = temporal;
            relative = this.clone();
        }

        relative.setValue(absolute.getValue() * (relative.getValue() / 100));
        return relative;
    }

    /**
     * returns a TemporalValue object that is relative regarding its value to temporal's value.
     * @return relative TemporalValue
     */
    public TemporalValue getRelativeTo() {
        TemporalValue absolute = relationTo.getAbsoluteTo();        // solve relation stack first
        return getRelativeTo(absolute);                             // to receive the relative
    }

    /**
     * returns a TemporalValue object with absolute value derived from this and temporal where one is relative
     * @param temporal
     * @return absolute TemporalValue, it is this if this and temporal are both absolute
     */
    public TemporalValue getAbsoluteTo(TemporalValue temporal) {
        if (temporal == null) {
            return null;
        }
        if(isRelative() && temporal.isRelative()) {         // if we both are relative,
            return null;                                    // we cannot get an absolute one
        }
        if(!isRelative() && !temporal.isRelative()) {       // if we both are absolute,
            return this;                                    // return this as we cannot give an absolute value of us regarding the other one
        }

        TemporalValue absolute;
        TemporalValue relative;

        if (temporal.isRelative()) {                        // get the relative one of us
            absolute = this.clone();
            relative = temporal;
        }
        else {
            absolute = temporal.clone();
            relative = this;
        }

        absolute.setValue((relative.getValue() * absolute.getValue()) / 100);

        return absolute;
    }

    /**
     * returns a TemporalValue object with absolute value derived from this.
     * This solves a hierarchy of relative relations, if an absolute root TemporalValue exists.
     * @return absolute TemporalValue
     */
    public TemporalValue getAbsoluteTo() {
        if(!hasAbsoluteRoot())                      // if no absolute root exists (so if only a stack of relative values exists)
            return null;                            // we cannot solve
        if(!hasRelation())  // != isRelative        // if we do not have any relations
            return this;                            // then we are already the result

        return getAbsoluteTo(relationTo);           // otherwise solve
    }

    /**
     * set value after applying the relativValue to it
     * @param relativValue
     * @return the resulting value (in its domain)
     */
    public double applyRelative(double relativValue) {
        setValue(getValue() * (relativValue / 100));
        return getValue();
    }
    /**
     * set value after applying the value of relativeTemporal to it
     * @param relativeTemporal
     * @return the resulting value (in its domain)
     */
    public double applyRelative(TemporalValue relativeTemporal) {
        if(!relativeTemporal.isRelative())
            return getValue();
        return applyRelative(relativeTemporal.getValue());
    }

    /**
     * adds value to this value
     * @param value
     * @return the resulting value (in its domain)
     */
    public double add(double value) {
        setValue(getValue() + value);
        return getValue();
    }

    /**
     * adds temporal's value to this value
     * @param temporal
     * @return the resulting value (in its domain)
     */
    public double add(TemporalValue temporal) {
        if(hasSameDomain(temporal))
            return add(temporal.getValue());
        return getValue();
    }

    /**
     * substracts value from this value
     * @param value
     * @return the resulting value (in its domain)
     */
    public double subtract(double value) {
        setValue(getValue() - value);
        return getValue();
    }

    /**
     * substracts temporal's value from this value
     * @param temporal
     * @return the resulting value (in its domain)
     */
    public double subtract(TemporalValue temporal) {
        if(hasSameDomain(temporal))
            return subtract(temporal.getValue());
        return getValue();
    }

    /**
     * compares this value with the given value
     * @param value
     * @return true if this value is greater than the given value
     */
    public boolean isGreater(double value) {
        return getValue() > value;
    }
    /**
     * compares this value with the given value. If temporal is not in the same Domain the result is false
     * @param temporal
     * @return true if this value is greater than temporal's value
     */
    public boolean isGreater(TemporalValue temporal) {
        if(hasSameDomain(temporal))
            return isGreater(temporal.getValue());
        return false;
    }
    /**
     * compares this value with the given value
     * @param value
     * @return true if this value is less than the given value
     */
    public boolean isLess(double value) {
        return getValue() < value;
    }
    /**
     * compares this value with the given value. If temporal is not in the same Domain the result is false
     * @param temporal
     * @return true if this value is less than temporal's value
     */
    public boolean isLess(TemporalValue temporal) {
        if(hasSameDomain(temporal))
            return isLess(temporal.getValue());
        return false;
    }

    /**
     * returns the TemporalValue with greater value of a and b. If equal, a is returned.
     * @param a
     * @param b
     * @return TemporalValue that is greater
     */
    public static TemporalValue getGreater(TemporalValue a, TemporalValue b) {
        if(b.isGreater(a))
            return b;
        return a;
    }
    /**
     * returns the TemporalValue a or b which is less in value. If equal, b is returned.
     * @param a
     * @param b
     * @return TemporalValue that is less
     */
    public static TemporalValue getLess(TemporalValue a, TemporalValue b) {
        if(a.isLess(b))
            return a;
        return b;
    }

    /**
     * returns if this domain is equal to temporal's domain
     * @param temporal
     * @return true if same domain
     */
    public boolean hasSameDomain(TemporalValue temporal) {
        return getDomain() == temporal.getDomain();
    }

    /**
     * returns if this value is equal to temporsl's value
     * @param temporal
     * @return true if same value
     */
    public boolean hasSameValue(TemporalValue temporal) {
        return getValue() == temporal.getValue();
    }

    /**
     * returns if equal, both value and domain
     * @param temporal
     * @return true if domain and value is equal
     */
    public boolean equals(TemporalValue temporal) {
        return hasSameDomain(temporal) && hasSameValue(temporal);
    }

    /**
     * return if it is a relative value
     * @return true if the domain is Relative
     */
    public boolean isRelative() {
        return getDomain() == Domain.Relative;
    }

    /**
     * returns if it is in milliseconds
     * @return true if the domain is Milliseconds
     */
    public boolean isMilliseconds() {
        return getDomain() == Domain.Milliseconds;
    }

    /**
     * returns if it is in ticks
     * @return true if the domain is Ticks
     */
    public boolean isTicks() {
        return getDomain() == Domain.Ticks;
    }

    /**
     * returns if the domain is unknown
     * @return true if the domain is Unknown
     */
    public boolean isUnknown() {
        return getDomain() == Domain.Unknown;
    }

    /**
     * stringifies as value + unit
     * @return string representation of the temporal value
     */
    public String toString() {
        return Double.toString(value) + getDomainString();
    }

    /**
     * sets value and domain from string with value + unit
     * @param valueDomain
     */
    public void fromString(String valueDomain) {
        Pattern pattern = Pattern.compile("^([+-]?\\d+(?:\\.\\d+)?)(ms|th|%|ticks|\\?)$");  // checks string if it is a valid value + unit string
        Matcher m = pattern.matcher(valueDomain.trim());
        if (m.matches()) {
            setValue(Double.parseDouble(m.group(1)));
            setDomain(fromDomainString(m.group(2)));
            return;
        }
        try {
            setValue(Double.parseDouble(valueDomain));
        } catch (NumberFormatException e) {
            // do nothing, value remains unchanged
        }
    }

    /**
     * return this domain unit string
     * @return domain as string
     */
    public String getDomainString() {
        return toDomainString(domain);
    }

    /**
     * return the unit string of domain
     * @param domain
     * @return domain as string
     */
    public static String toDomainString(Domain domain) {
        return domainStrings.get(domain);
    }

    /**
     * returns the Domain regarding the unit in domainString
     * @param domainString
     * @return domain
     */
    public static Domain fromDomainString(String domainString) {
        for (Map.Entry<Domain, String> entry : domainStrings.entrySet()) {
            if (entry.getValue().equals(domainString)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * returns the name of domain, e.g. for displaying
     * @param domain
     * @return domain name
     */
    public static String toDomainName(Domain domain) {
        return domainNameStrings.get(domain);
    }

    /**
     * returns the Domain regarding the corresponding domainName
     * @param domainName
     * @return domain
     */
    public static Domain fromDomainName(String domainName) {
        for (Map.Entry<Domain, String> entry : domainNameStrings.entrySet()) {
            if (entry.getValue().equals(domainName)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
