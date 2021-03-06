package metridoc.ezproxy.services

import groovy.transform.ToString

/**
 * Created with IntelliJ IDEA on 10/15/13
 * @author Tommy Barker
 */
@ToString(ignoreNulls = true, includeNames = true, includePackage = false)
class CrossRefResponse {
    boolean loginFailure = false
    boolean malformedDoi = false
    boolean unresolved = false
    CrossRefResponseException statusException
    String status
    String doi
    String articleTitle
    String journalTitle
    String givenName
    String surName
    String volume
    String issue
    String firstPage
    String lastPage
    Integer printYear
    Integer electronicYear
    Integer onlineYear
    Integer nullYear
    Integer otherYear
    String printIssn
    String electronicIssn
    String printIsbn
    String electronicIsbn
}
