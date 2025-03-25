/*
* This file is part of the Aurora Enricher.
*
* (c) ZHAW HSB <apps.hsb@zhaw.ch>
*
* For the full copyright and license information, please view the LICENSE
* file that was distributed with this source code.
*/
package ch.zhaw.hsb.aurora.enricher.Model.Enrichment;

/**
 * This class serves as the model for the data that will enrich a field.
 * 
 * @author Dana Ghousson ZHAW
 * @author Iris Hausmann ZHAW
 */
public class EnrichmentModel {

    String version;
    int embargoAmount = -1;
    String embargoUnit;
    String license;
    String uri;

    // nur als Kriterium
    String location;
    String additionalOAfee;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getEmbargoAmount() {
        return embargoAmount;
    }

    public void setEmbargoAmount(int embargoAmount) {
        this.embargoAmount = embargoAmount;
    }

    public String getEmbargoUnit() {
        return embargoUnit;
    }

    public void setEmbargoUnit(String embargoUnit) {
        this.embargoUnit = embargoUnit;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAdditionalOAfee() {
        return additionalOAfee;
    }

    public void setAdditionalOAfee(String additionalOAfee) {
        this.additionalOAfee = additionalOAfee;
    }
}
