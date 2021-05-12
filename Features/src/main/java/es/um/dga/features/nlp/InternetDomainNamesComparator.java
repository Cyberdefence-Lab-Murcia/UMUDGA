/*
 * Project: DGA Features
 * Copyright (c) 2018 University of Murcia
 *
 * @author Mattia Zago - mattia.zago@um.es
 */

package es.um.dga.features.nlp;

import com.google.common.net.InternetDomainName;

import java.util.Comparator;

public class InternetDomainNamesComparator implements Comparator<InternetDomainName> {
    @Override public int compare(InternetDomainName o1, InternetDomainName o2) {
        return o1.toString().compareTo(o2.toString());
    }
}
