//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.06.04 at 01:11:33 PM CEST 
//


package es.um.dga.features.utils.config;

import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://dga.um.es/xml/mongodb.xsd}user" minOccurs="0"/>
 *         &lt;element ref="{http://dga.um.es/xml/mongodb.xsd}collections"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "user",
    "collections"
})
@XmlRootElement(name = "database")
@Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2018-06-04T01:11:33+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
public class Database {

    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2018-06-04T01:11:33+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected User user;
    @XmlElement(required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2018-06-04T01:11:33+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected Collections collections;
    @XmlAttribute(name = "id", required = true)
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2018-06-04T01:11:33+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    protected String id;

    /**
     * Gets the value of the user property.
     * 
     * @return
     *     possible object is
     *     {@link User }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2018-06-04T01:11:33+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public User getUser() {
        return user;
    }

    /**
     * Sets the value of the user property.
     * 
     * @param value
     *     allowed object is
     *     {@link User }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2018-06-04T01:11:33+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setUser(User value) {
        this.user = value;
    }

    /**
     * Gets the value of the collections property.
     * 
     * @return
     *     possible object is
     *     {@link Collections }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2018-06-04T01:11:33+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public Collections getCollections() {
        return collections;
    }

    /**
     * Sets the value of the collections property.
     * 
     * @param value
     *     allowed object is
     *     {@link Collections }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2018-06-04T01:11:33+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setCollections(Collections value) {
        this.collections = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2018-06-04T01:11:33+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.internal.xjc.Driver", date = "2018-06-04T01:11:33+02:00", comments = "JAXB RI v2.2.8-b130911.1802")
    public void setId(String value) {
        this.id = value;
    }

}
